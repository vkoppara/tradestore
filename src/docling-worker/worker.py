"""
Docling PDF extraction worker — one-shot, Cloud Run Jobs style.

Spawned per job by pdf-service via Docker SDK. Processes exactly one job,
writes results to postgres, fires pg_notify for SSE subscribers, sends
webhook callback if requested, then exits.

Usage:
    python worker.py --job-id <uuid>

Manual trigger (same as gcloud run jobs execute):
    docker run --rm \\
      --network loan-market_default \\
      --mount source=backend-ms-py_pdf-uploads,target=/uploads \\
      -e DATABASE_URL=postgresql://loan_market:loan_market@postgres:5432/loan_market \\
      backend-ms-py-docling-worker \\
      python worker.py --job-id <uuid>
"""

from __future__ import annotations

import argparse
import asyncio
import json
import io
import os
import time
from pathlib import Path

import asyncpg
import httpx
import numpy as np
import pypdfium2 as pdfium
from PIL import Image
from rapidocr import RapidOCR

import classifier
from llm_client import get_llm_client

DATABASE_URL = os.environ["DATABASE_URL"]
DO_CELL_MATCHING = os.environ.get("DO_CELL_MATCHING", "true").lower() == "true"
UPLOAD_DIR = Path(os.environ.get("UPLOAD_DIR", "/uploads"))
DO_OCR = os.environ.get("DO_OCR", "true").lower() == "true"

# ── Docling setup (module-level: loaded once when the container starts) ──────

# Ensure models are present in HF_HOME (the shared docling-models volume).
# download_models_hf(force=False) is a no-op when the cache is already populated.
# After this call we know models are local, so we lock HF to offline mode — no
# further network calls will be made during PDF processing.
def _ensure_models() -> None:
    print("[worker] verifying Docling models...")

    downloaded = False
    try:
        from docling.pipeline.standard_pdf_pipeline import StandardPdfPipeline
        if hasattr(StandardPdfPipeline, "download_models_hf"):
            StandardPdfPipeline.download_models_hf(force=False, progress=True)
            downloaded = True
    except Exception:
        pass

    if not downloaded:
        from huggingface_hub import snapshot_download
        print("[worker] falling back to snapshot_download...")
        snapshot_download(repo_id="ds4sd/docling-models")

    os.environ["HF_HUB_OFFLINE"] = "1"
    os.environ["TRANSFORMERS_OFFLINE"] = "1"
    print("[worker] models ready, HF locked to offline")

_ensure_models()

from docling.datamodel.base_models import InputFormat
from docling.datamodel.pipeline_options import PdfPipelineOptions, RapidOcrOptions, TableFormerMode
from docling.document_converter import DocumentConverter

# RapidOCR is wired into Docling's pipeline via ocr_options — Docling calls it
# internally during convert(); no separate OCR pass needed.
_opts = PdfPipelineOptions()
_opts.do_ocr = True
# force_full_page_ocr: ignore the PDF text layer and OCR every page as an image.
# Without this, Docling skips OCR for pages that already have an embedded text
# layer — even if that layer is corrupt or incomplete.
_opts.ocr_options = RapidOcrOptions(force_full_page_ocr=True)
_opts.do_table_structure = True
_opts.table_structure_options.mode = TableFormerMode.ACCURATE
_opts.table_structure_options.do_cell_matching = DO_CELL_MATCHING

try:
    from docling.document_converter import PdfFormatOption
    _fmt = {InputFormat.PDF: PdfFormatOption(pipeline_options=_opts)}
except ImportError:
    _fmt = {InputFormat.PDF: {"pipeline_options": _opts}}

_converter = DocumentConverter(allowed_formats=[InputFormat.PDF], format_options=_fmt)
_rapidocr = RapidOCR() if DO_OCR else None


# ── PDF processing ────────────────────────────────────────────────────────────

def _extract_table_cells(element) -> tuple[list[str], list[dict]]:
    """Extract a table using the raw cell grid, always with integer column keys.

    Returns (headers, rows) where:
      - headers is always ["0", "1", "2", ...] regardless of what Docling detects
      - rows includes ALL grid rows, so rows[0] is always the first physical row
        (which may be column label text or real transaction data)

    This avoids two problems with export_to_dataframe():
      1. Docling sometimes picks a data row as the header, other times uses
         integer indices — no consistent behaviour across pages.
      2. pandas drops columns whose header text is an empty string.
    """
    data = element.data
    num_rows = data.num_rows
    num_cols = data.num_cols
    print(f"[worker] processing table with {num_rows} rows and {num_cols} columns")
    if num_rows == 0 or num_cols == 0:
        return [], []

    grid: list[list[str]] = [[""] * num_cols for _ in range(num_rows)]
    col_l: dict[int, float] = {}   # col index → min left across all rows
    col_r: dict[int, float] = {}   # col index → max right across all rows
    for cell in data.table_cells:
        r = cell.start_row_offset_idx
        c = cell.start_col_offset_idx
        if 0 <= r < num_rows and 0 <= c < num_cols:
            grid[r][c] = (cell.text or "").strip()
            if cell.bbox is not None:
                col_l[c] = min(col_l.get(c, cell.bbox.l), cell.bbox.l)
                col_r[c] = max(col_r.get(c, cell.bbox.r), cell.bbox.r)

    headers = [str(i) for i in range(num_cols)]
    rows = [
        {str(c): grid[r][c] for c in range(num_cols)}
        for r in range(num_rows)
    ]
    col_bbox = {
        str(c): {"l": col_l[c], "r": col_r[c]}
        for c in range(num_cols) if c in col_l
    }
    return headers, rows, col_bbox


_LABEL_TYPE: dict[str, str] = {
    "title":          "heading",
    "section_header": "heading",
    "page_header":    "heading",
    "text":           "paragraph",
    "paragraph":      "paragraph",
    "list_item":      "list_item",
    "caption":        "caption",
    "footnote":       "footnote",
    "code":           "code",
    "formula":        "paragraph",
    "page_footer":    "footnote",
}

_HEADING_LEVEL: dict[str, int] = {
    "title":          1,
    "section_header": 2,
    "page_header":    3,
}


def _process_pdf(file_path: str) -> dict:
    """Convert PDF with Docling (OCR + table extraction handled internally)."""
    from docling.datamodel.document import PictureItem, TableItem, TextItem

    result = _converter.convert(file_path)
    doc = result.document

    page_sections: dict[int, list] = {}
    picture_regions: list[dict] = []
    table_count = 0
    section_count = 0

    for element, _level in doc.iterate_items():
        prov_list = getattr(element, "prov", None) or []
        if not prov_list:
            continue

        page_no = prov_list[0].page_no
        if page_no not in page_sections:
            page_sections[page_no] = []

        if isinstance(element, TableItem):
            headers, rows, col_bbox = _extract_table_cells(element)
            print(f"[worker] table on page {page_no}: {len(headers)} cols, {len(rows)} rows, headers={headers[:6]}")
            caption = None
            if hasattr(element, "caption_text"):
                try:
                    caption = element.caption_text(doc) or None
                except Exception:
                    pass
            section: dict = {
                "type": "table",
                "caption": caption,
                "headers": headers,
                "rows": rows,
            }
            if col_bbox:
                section["col_bbox"] = col_bbox
            page_sections[page_no].append(section)
            table_count += 1

        elif isinstance(element, PictureItem):
            bbox = prov_list[0].bbox
            picture_regions.append(
                {
                    "page_no": page_no,
                    "bbox": {
                        "l": float(bbox.l),
                        "t": float(bbox.t),
                        "r": float(bbox.r),
                        "b": float(bbox.b),
                    },
                }
            )

        elif isinstance(element, TextItem):
            text = (element.text or "").strip()
            if not text:
                continue

            raw_label = str(getattr(element, "label", "text")).lower().split(".")[-1]
            section_type = _LABEL_TYPE.get(raw_label, "paragraph")
            section: dict = {"type": section_type, "text": text}
            if section_type == "heading":
                section["level"] = _HEADING_LEVEL.get(raw_label, 2)

            page_sections[page_no].append(section)
            section_count += 1

    text_like_sections = sum(
        1
        for sections in page_sections.values()
        for section in sections
        if section.get("type") != "table"
    )

    # Some image-only PDFs are classified by Docling as PictureItem only, which
    # means Docling's export path returns no text even with RapidOCR enabled.
    # In that case, keep Docling's page/layout ordering and OCR each Docling
    # picture region in that same order.
    if DO_OCR and text_like_sections == 0 and picture_regions and _rapidocr is not None:
        pdf = pdfium.PdfDocument(file_path)
        rendered_pages: dict[int, tuple[np.ndarray, float, float]] = {}
        try:
            scale = 2.5
            for region in picture_regions:
                page_no = int(region["page_no"])
                if page_no not in rendered_pages:
                    page = pdf[page_no - 1]
                    try:
                        page_height = float(page.get_height())
                        bitmap = page.render(scale=scale)
                        rendered_pages[page_no] = (np.asarray(bitmap.to_pil()), scale, page_height)
                    finally:
                        page.close()

                image, render_scale, page_height = rendered_pages[page_no]
                bbox = region["bbox"]
                left = max(0, int(round(bbox["l"] * render_scale)))
                right = min(image.shape[1], int(round(bbox["r"] * render_scale)))
                top = max(0, int(round((page_height - bbox["t"]) * render_scale)))
                bottom = min(image.shape[0], int(round((page_height - bbox["b"]) * render_scale)))
                if right <= left or bottom <= top:
                    continue

                crop = image[top:bottom, left:right]
                output = _rapidocr(crop)
                texts = getattr(output, "txts", None) or output or []
                ocr_text = "\n".join(str(text).strip() for text in texts if str(text).strip())
                if not ocr_text:
                    continue
                if page_no not in page_sections:
                    page_sections[page_no] = []
                page_sections[page_no].append({
                    "type": "ocr_text",
                    "text": ocr_text,
                    "source": "docling_picture_region_rapidocr",
                    "bbox": bbox,
                })
                section_count += 1
        finally:
            pdf.close()

    page_list = [
        {"page_number": p, "sections": page_sections[p]}
        for p in sorted(page_sections.keys())
    ]

    full_text_parts: list[str] = []
    for page in page_list:
        texts = [
            str(section.get("text", "")).strip()
            for section in page["sections"]
            if isinstance(section, dict) and section.get("type") != "table"
        ]
        texts = [text for text in texts if text]
        if texts:
            full_text_parts.append("\n".join(texts))

    full_text = "\n\n".join(full_text_parts).strip() or doc.export_to_text()

    return {
        "pages": page_list,
        "full_text": full_text,
        "metadata": {
            "total_pages": len(page_list),
            "table_count": table_count,
            "section_count": section_count,
            "ocr_engine": "rapidocr" if DO_OCR else "none",
            "table_model": "accurate",
        },
    }


# ── Callback ──────────────────────────────────────────────────────────────────

async def _send_callback(url: str, payload: dict) -> None:
    async with httpx.AsyncClient(timeout=15.0) as client:
        try:
            await client.post(url, json=payload)
        except Exception as exc:
            print(f"[worker] callback to {url} failed: {exc}")


# ── Main ──────────────────────────────────────────────────────────────────────

async def main() -> None:
    parser = argparse.ArgumentParser(description="Docling one-shot PDF extraction worker")
    parser.add_argument("--job-id", required=True, help="UUID of the pdf.jobs row to process")
    args = parser.parse_args()
    job_id = args.job_id

    conn = await asyncpg.connect(DATABASE_URL)
    try:
        job = await conn.fetchrow(
            """
            UPDATE pdf.jobs
               SET status = 'processing', started_at = now()
             WHERE job_id = $1 AND status = 'queued'
            RETURNING *
            """,
            job_id,
        )
        if not job:
            print(f"[worker] job {job_id} not claimable (not found or not in queued state)")
            return

        file_path = UPLOAD_DIR / f"{job_id}.pdf"
        if not file_path.exists():
            raise FileNotFoundError(f"Upload file not found: {file_path}")

        t0 = time.time()
        extraction = _process_pdf(str(file_path))
        elapsed = round(time.time() - t0, 2)

        llm = get_llm_client()
        try:
            classification = await classifier.classify(
                extraction["pages"], extraction.get("full_text", ""), llm
            )
        except Exception as exc:
            print(f"[worker] classification failed (non-fatal): {exc}")
            classification = {"document_type": "unknown", "confidence": "low",
                              "fields": {"reason": f"classification_error: {exc}"}}
        extraction["classification"] = classification

        payload = {
            "job_id": job_id,
            "status": "completed",
            "processing_time_seconds": elapsed,
            "pages": extraction["pages"],
            "metadata": extraction["metadata"],
            "classification": classification,
        }

        await conn.execute(
            """
            UPDATE pdf.jobs
               SET status = 'completed',
                   result = $1::jsonb,
                   completed_at = now(),
                   processing_time_s = $2
             WHERE job_id = $3
            """,
            json.dumps(extraction), elapsed, job_id,
        )

        # Notify SSE subscribers — signal only, SSE fetches full result from DB
        signal = json.dumps({"job_id": job_id, "status": "completed"})
        await conn.execute("SELECT pg_notify($1, $2)", f"pdf_job_{job_id}", signal)

        if job["callback_url"]:
            await _send_callback(job["callback_url"], payload)

        print(f"[worker] job {job_id} completed in {elapsed}s")        
    except Exception as exc:
        error_str = str(exc)
        try:
            await conn.execute(
                "UPDATE pdf.jobs SET status = 'failed', error = $1, completed_at = now() WHERE job_id = $2",
                error_str, job_id,
            )
            signal = json.dumps({"job_id": job_id, "status": "failed"})
            await conn.execute("SELECT pg_notify($1, $2)", f"pdf_job_{job_id}", signal)
            callback_row = await conn.fetchrow(
                "SELECT callback_url FROM pdf.jobs WHERE job_id = $1", job_id
            )
            if callback_row and callback_row["callback_url"]:
                await _send_callback(callback_row["callback_url"],
                                     {"job_id": job_id, "status": "failed", "error": error_str})
        except Exception as inner:
            print(f"[worker] failed to persist error state: {inner}")

        print(f"[worker] job {job_id} failed: {exc}")
        raise

    finally:
        (UPLOAD_DIR / f"{job_id}.pdf").unlink(missing_ok=True)
        await conn.close()


if __name__ == "__main__":
    asyncio.run(main())
