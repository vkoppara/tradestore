from __future__ import annotations

import argparse
import json
from pathlib import Path

import numpy as np
import pypdfium2 as pdfium
from PIL import Image
from rapidocr import RapidOCR

from docling.datamodel.base_models import InputFormat
from docling.datamodel.document import TableItem, TextItem
from docling.datamodel.pipeline_options import PdfPipelineOptions, RapidOcrOptions
from docling.document_converter import DocumentConverter


def build_converter() -> DocumentConverter:
    opts = PdfPipelineOptions()
    opts.do_ocr = True
    opts.ocr_options = RapidOcrOptions()
    opts.do_table_structure = True
    opts.table_structure_options.do_cell_matching = True

    try:
        from docling.document_converter import PdfFormatOption

        fmt = {InputFormat.PDF: PdfFormatOption(pipeline_options=opts)}
    except ImportError:
        fmt = {InputFormat.PDF: {"pipeline_options": opts}}

    return DocumentConverter(allowed_formats=[InputFormat.PDF], format_options=fmt)


def run_docling(pdf_path: Path) -> dict:
    converter = build_converter()
    result = converter.convert(str(pdf_path))
    doc = result.document

    page_texts: dict[int, list[str]] = {}
    page_tables: dict[int, list[dict]] = {}
    table_count = 0

    for element, _level in doc.iterate_items():
        prov_list = getattr(element, "prov", None) or []
        if not prov_list:
            continue

        page_no = prov_list[0].page_no
        if isinstance(element, TableItem):
            df = element.export_to_dataframe()
            page_tables.setdefault(page_no, []).append(
                {
                    "table_index": table_count,
                    "headers": list(df.columns.astype(str)),
                    "rows": df.astype(str).values.tolist(),
                    "markdown": element.export_to_markdown(),
                }
            )
            table_count += 1
        elif isinstance(element, TextItem):
            page_texts.setdefault(page_no, []).append(element.text)

    all_pages = sorted(set(list(page_texts.keys()) + list(page_tables.keys())))
    return {
        "pages": [
            {
                "page_number": p,
                "text": "\n".join(page_texts.get(p, [])),
                "tables": page_tables.get(p, []),
            }
            for p in all_pages
        ],
        "full_text": doc.export_to_text(),
        "full_markdown": doc.export_to_markdown(),
        "metadata": {"total_pages": len(all_pages), "table_count": table_count},
    }


def render_first_page(pdf_path: Path) -> Image.Image:
    pdf = pdfium.PdfDocument(str(pdf_path))
    page = pdf[0]
    bitmap = page.render(scale=2.5)
    pil_image = bitmap.to_pil()
    page.close()
    pdf.close()
    return pil_image


def crop_non_white(image: Image.Image, threshold: int = 245, pad: int = 20) -> Image.Image:
    rgb = np.asarray(image.convert("RGB"))
    mask = np.any(rgb < threshold, axis=2)
    coords = np.argwhere(mask)
    if coords.size == 0:
        return image

    y0, x0 = coords.min(axis=0)
    y1, x1 = coords.max(axis=0)
    x0 = max(0, x0 - pad)
    y0 = max(0, y0 - pad)
    x1 = min(image.width, x1 + pad)
    y1 = min(image.height, y1 + pad)
    return image.crop((x0, y0, x1, y1))


def run_rapidocr(image: Image.Image) -> dict:
    engine = RapidOCR()
    output = engine(np.asarray(image))
    result = getattr(output, "txts", None) or output
    boxes = getattr(output, "boxes", None)
    scores = getattr(output, "scores", None)

    if not result:
        return {"lines": [], "text": ""}

    lines = []
    if boxes is not None and scores is not None:
        for idx, text in enumerate(result):
            lines.append(
                {
                    "text": text,
                    "score": scores[idx] if idx < len(scores) else None,
                    "points": boxes[idx].tolist() if idx < len(boxes) and hasattr(boxes[idx], "tolist") else (boxes[idx] if idx < len(boxes) else None),
                }
            )
    else:
        for item in result:
            if isinstance(item, (list, tuple)) and len(item) == 3:
                points, text, score = item
                lines.append({"text": text, "score": score, "points": points})
            else:
                lines.append({"text": str(item), "score": None, "points": None})
    return {"lines": lines, "text": "\n".join(line["text"] for line in lines)}


def main() -> None:
    parser = argparse.ArgumentParser(description="Debug OCR for PAN PDF with Docling and RapidOCR")
    parser.add_argument("pdf_path", help="Path to the input PDF")
    parser.add_argument("--output-dir", default="/tmp/pan-ocr-debug", help="Directory for debug artifacts")
    args = parser.parse_args()

    pdf_path = Path(args.pdf_path)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    raw_docling = run_docling(pdf_path)
    (output_dir / "docling_raw.json").write_text(json.dumps(raw_docling, indent=2, ensure_ascii=False), encoding="utf-8")

    first_page = render_first_page(pdf_path)
    first_page.save(output_dir / "page1_full.png")

    cropped = crop_non_white(first_page)
    cropped.save(output_dir / "page1_cropped.png")

    cropped_ocr = run_rapidocr(cropped)
    (output_dir / "rapidocr_cropped.json").write_text(json.dumps(cropped_ocr, indent=2, ensure_ascii=False), encoding="utf-8")

    summary = {
        "docling_full_text": raw_docling.get("full_text", ""),
        "docling_pages": raw_docling.get("pages", []),
        "rapidocr_cropped_text": cropped_ocr.get("text", ""),
        "artifacts_dir": str(output_dir),
    }
    print(json.dumps(summary, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
