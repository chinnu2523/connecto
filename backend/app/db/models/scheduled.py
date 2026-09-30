import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Float, Integer, DateTime, Index, UniqueConstraint
from app.db.session import Base

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class ScheduledMessage(Base):
    __tablename__ = "scheduled_messages"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    channel_id = Column(String(64), nullable=False, index=True, default="general")
    author = Column(String(64), nullable=False, index=True)
    avatar = Column(String(32), nullable=True, default="👤")
    content = Column(String(4000), nullable=False)
    delivery_time_epoch = Column(Float, nullable=False, index=True)
    delivery_time_str = Column(String(64), nullable=False)
    status = Column(String(32), nullable=False, default="pending", index=True)  # pending, sent, cancelled
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

Index("idx_scheduled_channel_delivery", ScheduledMessage.channel_id, ScheduledMessage.delivery_time_epoch, ScheduledMessage.status)
Index("idx_scheduled_author_status", ScheduledMessage.author, ScheduledMessage.status)

class AuditLog(Base):
    __tablename__ = "audit_logs"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    action = Column(String(64), nullable=False, index=True)
    actor = Column(String(64), nullable=False, index=True)
    target = Column(String(128), nullable=True, index=True, default="")
    details = Column(String(1000), nullable=True, default="")
    ip_address = Column(String(64), nullable=True, default="")
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, index=True)

Index("idx_audit_action_time", AuditLog.action, AuditLog.created_at)
Index("idx_audit_actor_time", AuditLog.actor, AuditLog.created_at)

class Bookmark(Base):
    __tablename__ = "bookmarks"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), nullable=False, index=True)
    message_id = Column(String(36), nullable=False, index=True)
    channel_id = Column(String(64), nullable=True, default="general")
    notes = Column(String(500), nullable=True, default="")
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, index=True)

    __table_args__ = (
        UniqueConstraint("user_id", "message_id", name="uq_user_message_bookmark"),
        Index("idx_bookmark_user_time", "user_id", "created_at"),
    )

class Referral(Base):
    __tablename__ = "referrals"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    referrer_handle = Column(String(64), nullable=False, index=True)
    candidate_id = Column(String(64), nullable=False, index=True)
    milestone = Column(String(32), nullable=False, default="applied")
    xp_awarded = Column(Integer, nullable=False, default=100)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, index=True)

    __table_args__ = (
        UniqueConstraint("referrer_handle", "candidate_id", "milestone", name="uq_referral_milestone"),
        Index("idx_referral_referrer_time", "referrer_handle", "created_at"),
    )

class ModerationReport(Base):
    __tablename__ = "moderation_reports"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    message_id = Column(String(36), nullable=True, index=True)
    channel_id = Column(String(64), nullable=True, default="general", index=True)
    message_author = Column(String(64), nullable=True, default="unknown")
    message_content = Column(String(4000), nullable=True, default="")
    reporter = Column(String(64), nullable=False, index=True, default="guest")
    reason = Column(String(64), nullable=False, default="other")
    notes = Column(String(1000), nullable=True, default="")
    status = Column(String(32), nullable=False, default="pending", index=True)  # pending, resolved, dismissed
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, index=True)
