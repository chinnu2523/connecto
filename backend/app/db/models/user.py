import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, Index, Integer
from sqlalchemy.orm import relationship
from app.db.session import Base

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class User(Base):
    __tablename__ = "users"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    username = Column(String(32), unique=True, nullable=False, index=True)
    display_name = Column(String(64), nullable=False)
    email = Column(String(255), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    avatar_url = Column(String(512), nullable=True, default=None)
    banner_url = Column(String(512), nullable=True, default=None)
    fcm_token = Column(String(512), nullable=True, default=None)
    bio = Column(String(255), nullable=True, default="")
    username_changed = Column(Boolean, nullable=False, default=False)
    is_recruiter = Column(Boolean, nullable=False, default=False) # Real recruiter role check
    is_admin = Column(Boolean, nullable=False, default=False) # Administrator role
    is_stealth = Column(Boolean, nullable=False, default=False)
    is_online = Column(Boolean, nullable=False, default=True)
    phone_number = Column(String(32), nullable=True, default=None, index=True)
    full_name = Column(String(128), nullable=True, default="")
    date_of_birth = Column(String(32), nullable=True, default=None)
    gender = Column(String(32), nullable=True, default=None)
    location = Column(String(128), nullable=True, default=None)
    two_factor_enabled = Column(Boolean, nullable=False, default=False)
    two_factor_method = Column(String(16), nullable=False, default="sms")
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    sessions = relationship("UserSession", back_populates="user", cascade="all, delete-orphan")

Index("idx_users_presence", User.is_online, User.is_stealth)

class UserSession(Base):
    __tablename__ = "user_sessions"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    session_token_hash = Column(String(255), unique=True, nullable=False, index=True)
    ip_address = Column(String(45), nullable=True)
    user_agent = Column(String(512), nullable=True)
    expires_at = Column(DateTime(timezone=True), nullable=False, index=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    user = relationship("User", back_populates="sessions")

class OTPVerification(Base):
    __tablename__ = "otp_verifications"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=True, index=True)
    identifier = Column(String(255), nullable=False, index=True)  # phone number or email
    otp_code = Column(String(255), nullable=False)  # HMAC-SHA256 hash of the 6-digit OTP
    purpose = Column(String(32), nullable=False, default="forgot_password")  # "forgot_password", "two_factor_login", "two_factor_setup"
    method = Column(String(16), nullable=False, default="sms")  # "sms", "email"
    attempts = Column(Integer, nullable=False, default=0)  # Failed verification attempts counter
    expires_at = Column(DateTime(timezone=True), nullable=False, index=True)
    is_verified = Column(Boolean, nullable=False, default=False)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

