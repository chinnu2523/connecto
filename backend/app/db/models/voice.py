import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, Boolean, DateTime, ForeignKey, Index
from sqlalchemy.orm import relationship
from app.db.session import Base

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class VoiceRoom(Base):
    __tablename__ = "voice_rooms"

    id = Column(String(64), primary_key=True, default=generate_uuid)
    code = Column(String(32), unique=True, nullable=False, index=True)
    name = Column(String(128), nullable=False)
    topic = Column(String(256), nullable=False, default="Tactical voice comms")
    icon = Column(String(16), nullable=False, default="⚔️")
    creator_id = Column(String(64), nullable=True)
    creator_username = Column(String(64), nullable=False, default="Gamer")
    max_participants = Column(Integer, nullable=False, default=8)
    is_active = Column(Boolean, nullable=False, default=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    participants = relationship("VoiceRoomParticipant", back_populates="room", cascade="all, delete-orphan", lazy="selectin")

class VoiceRoomParticipant(Base):
    __tablename__ = "voice_room_participants"

    id = Column(String(64), primary_key=True, default=generate_uuid)
    room_id = Column(String(64), ForeignKey("voice_rooms.id", ondelete="CASCADE"), nullable=False, index=True)
    user_id = Column(String(64), nullable=True)
    username = Column(String(64), nullable=False)
    nickname = Column(String(64), nullable=True)
    avatar = Column(String(512), nullable=True)
    rank = Column(String(32), nullable=False, default="Shinobi")
    muted = Column(Boolean, nullable=False, default=False)
    speaking = Column(Boolean, nullable=False, default=False)
    joined_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    room = relationship("VoiceRoom", back_populates="participants")

class CallLog(Base):
    __tablename__ = "call_logs"

    id = Column(String(64), primary_key=True, default=generate_uuid)
    user_id = Column(String(64), nullable=True)
    username = Column(String(64), nullable=False, index=True)
    caller_name = Column(String(64), nullable=False)
    call_type = Column(String(64), nullable=False, default="Voice Call")
    room_name = Column(String(128), nullable=True)
    room_code = Column(String(32), nullable=True)
    duration_seconds = Column(Integer, nullable=False, default=0)
    is_missed = Column(Boolean, nullable=False, default=False)
    is_outgoing = Column(Boolean, nullable=False, default=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, index=True)
