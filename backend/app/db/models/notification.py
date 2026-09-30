import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, Index, Text
from sqlalchemy.orm import relationship
from app.db.session import Base

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class Notification(Base):
    __tablename__ = "notifications"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    type = Column(String(32), nullable=False) # 'call_invite', 'friend_request', 'chat_message', 'system'
    title = Column(String(128), nullable=False)
    content = Column(Text, nullable=False)
    sender_username = Column(String(64), nullable=True)
    sender_avatar = Column(String(512), nullable=True)
    reference_id = Column(String(128), nullable=True) # e.g. room_id, channel_id, request_id
    is_read = Column(Boolean, nullable=False, default=False, index=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    user = relationship("User")

    __table_args__ = (
        Index("ix_notifications_user_unread", "user_id", "is_read"),
    )
