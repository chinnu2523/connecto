import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, Boolean, Text, JSON, DateTime, ForeignKey, Index
from sqlalchemy.orm import relationship
from app.db.session import Base

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class CourseTrack(Base):
    __tablename__ = "course_tracks"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    title = Column(String(128), nullable=False)
    description = Column(Text, nullable=False)
    icon = Column(String(64), nullable=False, default="Shield")
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    lessons = relationship("Lesson", back_populates="track", cascade="all, delete-orphan")

class Lesson(Base):
    __tablename__ = "lessons"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    track_id = Column(String(36), ForeignKey("course_tracks.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String(128), nullable=False)
    content_markdown = Column(Text, nullable=False)
    xp_reward = Column(Integer, nullable=False, default=100)
    order_index = Column(Integer, nullable=False, default=1)

    track = relationship("CourseTrack", back_populates="lessons")
    questions = relationship("QuizQuestion", back_populates="lesson", cascade="all, delete-orphan")

class QuizQuestion(Base):
    __tablename__ = "quiz_questions"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    lesson_id = Column(String(36), ForeignKey("lessons.id", ondelete="CASCADE"), nullable=False, index=True)
    question_text = Column(Text, nullable=False)
    options = Column(JSON, nullable=False) # List of option strings
    correct_option_index = Column(Integer, nullable=False)
    explanation = Column(Text, nullable=True)

    lesson = relationship("Lesson", back_populates="questions")

class UserProgress(Base):
    __tablename__ = "user_progress"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    lesson_id = Column(String(36), ForeignKey("lessons.id", ondelete="CASCADE"), nullable=False, index=True)
    completed = Column(Boolean, nullable=False, default=True)
    score_percentage = Column(Integer, nullable=False, default=100)
    xp_earned = Column(Integer, nullable=False, default=100)
    completed_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    user = relationship("User")
    lesson = relationship("Lesson")

Index("idx_user_lesson_progress", UserProgress.user_id, UserProgress.lesson_id, unique=True)

class UserAcademyProfile(Base):
    __tablename__ = "user_academy_profiles"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    total_xp = Column(Integer, nullable=False, default=0)
    current_rank = Column(String(64), nullable=False, default="Security Apprentice")
    updated_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    user = relationship("User")

def calculate_rank_title(total_xp: int) -> str:
    """Calculates user rank title based on accumulated total XP."""
    if total_xp >= 1000:
        return "Cyber Security Architect"
    elif total_xp >= 600:
        return "Senior Penetration Tester"
    elif total_xp >= 300:
        return "AppSec Analyst"
    elif total_xp >= 100:
        return "Junior Threat Hunter"
    else:
        return "Security Apprentice"
