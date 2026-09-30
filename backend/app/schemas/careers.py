from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, EmailStr, Field

class JobOpeningResponse(BaseModel):
    id: str
    title: str
    department: str
    location: str
    description: str
    required_keywords: List[str]
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class JobApplicationResponse(BaseModel):
    id: str
    job_id: str
    job_title: str
    applicant_id: str
    full_name: str
    email: EmailStr
    cv_file_url: str
    ai_score: int
    detected_skills: List[str]
    summary: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
