from typing import List, Optional
from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user, get_current_user_optional
from app.core.careers_rate_limit import check_careers_submission_rate_limit
from app.core.cv_screening import validate_and_extract_cv_text, score_cv_evidence_pipeline
from app.db.models.user import User
from app.db.models.careers import JobOpening, JobApplication
from app.schemas.careers import JobOpeningResponse, JobApplicationResponse

router = APIRouter(prefix="/careers", tags=["Careers & AI CV Screening"])

async def ensure_default_job_openings_seeded(db: AsyncSession):
    """Seeds default job openings if database is empty."""
    stmt = select(JobOpening)
    existing = (await db.execute(stmt)).scalars().first()
    if existing:
        return

    job1 = JobOpening(
        title="Application Security (AppSec) Engineer",
        department="Cybersecurity",
        location="Remote",
        description="Lead application security audits, penetration testing, code reviews, and OWASP vulnerability mitigations.",
        required_keywords=["python", "owasp", "sql", "docker", "pentesting", "linux"]
    )
    db.add(job1)

    job2 = JobOpening(
        title="Threat Hunter & Incident Responder",
        department="Defensive Operations",
        location="Hybrid / On-site",
        description="Monitor SOC telemetry, perform packet inspection, analyze malware payloads, and conduct incident response.",
        required_keywords=["nmap", "wireshark", "python", "linux", "splunk", "malware"]
    )
    db.add(job2)
    await db.commit()

@router.get("/jobs", response_model=List[JobOpeningResponse])
async def list_job_openings(
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: AsyncSession = Depends(get_db)
):
    await ensure_default_job_openings_seeded(db)
    stmt = select(JobOpening).order_by(JobOpening.created_at.asc())
    res = await db.execute(stmt)
    return res.scalars().all()

@router.post("/jobs/{job_id}/apply", response_model=JobApplicationResponse, status_code=status.HTTP_201_CREATED)
async def submit_job_application(
    job_id: str,
    full_name: str = Form(...),
    email: str = Form(...),
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Enforce rate limit (max 3 submissions per 10 mins per user)
    check_careers_submission_rate_limit(current_user.id)

    stmt = select(JobOpening).where(JobOpening.id == job_id)
    job = (await db.execute(stmt)).scalar_one_or_none()
    if not job:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Job opening not found.")

    file_bytes = await file.read()
    cv_url, parsed_text = validate_and_extract_cv_text(file_bytes, file.filename or "resume.pdf")

    # Score CV using OCR + AI keyword pipeline
    score, detected_skills, summary = score_cv_evidence_pipeline(parsed_text, job.required_keywords or [])

    application = JobApplication(
        job_id=job.id,
        applicant_id=current_user.id,
        full_name=full_name,
        email=email,
        cv_file_url=cv_url,
        parsed_text=parsed_text,
        ai_score=score,
        detected_skills=detected_skills,
        summary=summary
    )
    db.add(application)
    await db.commit()
    await db.refresh(application)

    return JobApplicationResponse(
        id=application.id,
        job_id=application.job_id,
        job_title=job.title,
        applicant_id=application.applicant_id,
        full_name=application.full_name,
        email=application.email,
        cv_file_url=application.cv_file_url,
        ai_score=application.ai_score,
        detected_skills=application.detected_skills,
        summary=application.summary,
        created_at=application.created_at
    )

@router.get("/recruiter/applications", response_model=List[JobApplicationResponse])
async def list_recruiter_applications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Recruiter Dashboard Endpoint.
    Strictly checks authorization: Requires real recruiter role (`is_recruiter == True`).
    """
    if not current_user.is_recruiter:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. Recruiter role required to access candidate screening portal."
        )

    stmt = select(JobApplication).options(selectinload(JobApplication.job)).order_by(JobApplication.created_at.desc())
    res = await db.execute(stmt)

    output = []
    for app_item in res.scalars().all():
        output.append(JobApplicationResponse(
            id=app_item.id,
            job_id=app_item.job_id,
            job_title=app_item.job.title if app_item.job else "Position",
            applicant_id=app_item.applicant_id,
            full_name=app_item.full_name,
            email=app_item.email,
            cv_file_url=app_item.cv_file_url,
            ai_score=app_item.ai_score,
            detected_skills=app_item.detected_skills,
            summary=app_item.summary,
            created_at=app_item.created_at
        ))
    return output
