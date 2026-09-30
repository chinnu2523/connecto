import os
import re
import uuid
from io import BytesIO
from typing import Dict, List, Tuple
from pypdf import PdfReader
from fastapi import HTTPException, status
from app.core.config import settings

ALLOWED_CV_MIME_TYPES = {"application/pdf", "text/plain"}

def validate_and_extract_cv_text(file_bytes: bytes, filename: str) -> Tuple[str, str]:
    """
    Validates magic bytes for PDF or text file, extracts full text content,
    saves file to storage, and returns (relative_file_url, extracted_text).
    """
    if not file_bytes:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Uploaded CV file is empty.")

    # Enforce strict 10MB file size limit
    max_cv_size = getattr(settings, 'MAX_CV_SIZE_MB', 10) * 1024 * 1024
    if len(file_bytes) > max_cv_size:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"CV document exceeds maximum allowed size ({settings.MAX_CV_SIZE_MB} MB)."
        )

    extracted_text = ""
    ext = os.path.splitext(filename)[1].lower()

    # Check magic bytes for PDF
    if file_bytes.startswith(b"%PDF-"):
        try:
            reader = PdfReader(BytesIO(file_bytes))
            for page in reader.pages:
                text = page.extract_text()
                if text:
                    extracted_text += text + "\n"
        except Exception as e:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Corrupted PDF file: {str(e)}")
        save_ext = "pdf"
    elif ext in (".txt", ".md") or all(b < 128 or b in (9, 10, 13) for b in file_bytes[:512]):
        try:
            extracted_text = file_bytes.decode("utf-8", errors="ignore")
        except Exception:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid text encoding in CV.")
        save_ext = "txt"
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid CV format. Only PDF and plain text (.txt/.md) documents are accepted."
        )

    if not extracted_text.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Could not extract readable text from CV document."
        )

    # Save file to storage
    cv_dir = os.path.join(settings.UPLOAD_DIR, "cv_resumes")
    os.makedirs(cv_dir, exist_ok=True)
    saved_filename = f"{uuid.uuid4().hex}.{save_ext}"
    filepath = os.path.join(cv_dir, saved_filename)

    with open(filepath, "wb") as f:
        f.write(file_bytes)

    return f"/uploads/cv_resumes/{saved_filename}", extracted_text.strip()

def score_cv_evidence_pipeline(
    extracted_text: str,
    required_keywords: List[str]
) -> Tuple[int, List[str], str]:
    """
    Evaluates extracted CV text against position requirements,
    calculates match percentage score (0-100), detects skills, and generates summary.
    """
    lower_text = extracted_text.lower()
    detected_skills = []

    for kw in required_keywords:
        pattern = r"\b" + re.escape(kw.lower()) + r"\b"
        if re.search(pattern, lower_text):
            detected_skills.append(kw)

    if not required_keywords:
        score = 80
    else:
        score = int((len(detected_skills) / len(required_keywords)) * 100)

    # Cap score between 10 and 100
    score = max(10, min(100, score))

    summary = (
        f"Candidate matched {len(detected_skills)} of {len(required_keywords)} required technical skill keywords "
        f"({score}% compatibility index). Detected technical proficiencies: {', '.join(detected_skills) if detected_skills else 'None'}."
    )

    return score, detected_skills, summary
