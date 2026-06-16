from __future__ import annotations

import json
import re

from llm_client import LLMClient

_SYSTEM = """\
You are a document classifier for Indian financial documents.
Given the first-page content of a PDF (text and tables), identify the document type
and extract key fields. Respond with valid JSON only — no markdown, no explanation.

Document types and their fields:
- pan_card        : pan_number, name, father_name, dob
- aadhaar_card    : name, dob, gender, aadhaar_last4
- payslip         : company_name, employee_name, month, year, net_salary
- it_return       : itr_type (ITR-1/2/3/4/5/6/7), assessment_year, taxpayer_name, pan
- gst_statement   : gstin, legal_name, financial_year
- balance_sheet   : company_name, financial_year, auditor
- bank_statement  : bank_name, account_holder, account_number_last4, period_from, period_to
- unknown         : reason

Required JSON format:
{
  "document_type": "<one of the types above>",
  "confidence": "<high|medium|low>",
  "fields": { <extracted fields for that type, omit fields you cannot find> }
}"""

_VALID_TYPES = {
    "pan_card", "aadhaar_card", "payslip", "it_return",
    "gst_statement", "balance_sheet", "bank_statement", "unknown",
}


def _flatten_page(page: dict) -> str:
    parts: list[str] = []
    for section in page.get("sections", []):
        if not isinstance(section, dict):
            continue
        if section.get("type") == "table":
            headers = section.get("headers", [])
            for row in section.get("rows", []):
                row_str = " | ".join(f"{h}: {row.get(h, '')}" for h in headers)
                if row_str.strip():
                    parts.append(row_str)
        else:
            text = (section.get("text") or "").strip()
            if text:
                parts.append(text)
    return "\n".join(parts)


async def classify(pages: list[dict], full_text: str, llm: LLMClient) -> dict:
    page1_text = _flatten_page(pages[0]) if pages else ""
    content = (page1_text or full_text or "")[:3000]

    print(f"[classifier] pages={len(pages)} content_len={len(content)}")

    if not content.strip():
        print("[classifier] no extractable text — returning unknown")
        return {"document_type": "unknown", "confidence": "low", "fields": {"reason": "no extractable text"}}

    print(f"[classifier] sending to LLM ({type(llm).__name__}):\n{content[:400]}")

    try:
        raw = await llm.complete(_SYSTEM, content, max_tokens=512)
    except Exception as exc:
        print(f"[classifier] LLM call failed: {exc}")
        return {"document_type": "unknown", "confidence": "low", "fields": {"reason": f"llm_error: {exc}"}}

    print(f"[classifier] raw response ({len(raw)} chars): {raw[:400]}")

    raw = re.sub(r"<think>[\s\S]*?</think>", "", raw, flags=re.IGNORECASE).strip()
    json_match = re.search(r"\{[\s\S]*\}", raw)
    if not json_match:
        print(f"[classifier] no JSON found: {raw[:200]}")
        return {"document_type": "unknown", "confidence": "low", "fields": {"reason": f"no_json_in_response: {raw[:200]}"}}

    raw = json_match.group(0)
    try:
        result = json.loads(raw)
    except json.JSONDecodeError as exc:
        print(f"[classifier] JSON parse failed: {exc}")
        return {"document_type": "unknown", "confidence": "low", "fields": {"reason": f"parse_error: {raw[:200]}"}}

    if result.get("document_type") not in _VALID_TYPES:
        result["document_type"] = "unknown"
    if result.get("confidence") not in {"high", "medium", "low"}:
        result["confidence"] = "medium"
    result.setdefault("fields", {})

    print(f"[classifier] result: type={result['document_type']} confidence={result['confidence']} fields={list(result['fields'].keys())}")
    return result
