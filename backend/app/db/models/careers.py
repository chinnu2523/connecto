import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, Text, JSON, DateTime, ForeignKey, Index
from sqlalchemy.orm import relationship
from app.db.session import Base

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class JobOpening(Base):
    __tablename__ = "job_openings"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    title = Column(String(128), nullable=False)
    department = Column(String(64), nullable=False, default="Cybersecurity")
    location = Column(String(64), nullable=False, default="Remote / On-site")
    description = Column(Text, nullable=False)
    required_keywords = Column(JSON, nullable=False, default=list) # e.g. ["python", "sql", "owasp", "nmap", "docker"]
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    applications = relationship("JobApplication", back_populates="job", cascade="all, delete-orphan")

class JobApplication(Base):
    __tablename__ = "job_applications"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    job_id = Column(String(36), ForeignKey("job_openings.id", ondelete="CASCADE"), nullable=False, index=True)
    applicant_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    full_name = Column(String(128), nullable=False)
    email = Column(String(255), nullable=False)
    cv_file_url = Column(String(512), nullable=False)
    parsed_text = Column(Text, nullable=False)
    ai_score = Column(Integer, nullable=False, default=0) # Match score percentage (0-100)
    detected_skills = Column(JSON, nullable=False, default=list)
    summary = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    job = relationship("JobOpening", back_populates="applications")
    applicant = relationship("User")
