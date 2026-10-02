import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, DateTime, ForeignKey, Enum, Text, JSON, PrimaryKeyConstraint, Index
from sqlalchemy.orm import relationship
from app.db.session import Base
from app.db.models.user import User

def generate_uuid():
    return str(uuid.uuid4())

def utc_now():
    return datetime.now(timezone.utc)

class Server(Base):
    __tablename__ = "servers"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    name = Column(String(64), nullable=False)
    icon_url = Column(String(512), nullable=True)
    owner_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    channels = relationship("Channel", back_populates="server", cascade="all, delete-orphan")
    members = relationship("ServerMember", back_populates="server", cascade="all, delete-orphan")

class Channel(Base):
    __tablename__ = "channels"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    server_id = Column(String(36), ForeignKey("servers.id", ondelete="CASCADE"), nullable=True, index=True) # Null for DMs
    name = Column(String(64), nullable=False, index=True)
    type = Column(String(16), nullable=False, default="text") # 'text' or 'voice'
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    server = relationship("Server", back_populates="channels")
    messages = relationship("Message", back_populates="channel", cascade="all, delete-orphan")
    dm_participants = relationship("DMParticipant", back_populates="channel", cascade="all, delete-orphan")

class ServerMember(Base):
    __tablename__ = "server_members"

    server_id = Column(String(36), ForeignKey("servers.id", ondelete="CASCADE"), primary_key=True)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    role = Column(String(16), nullable=False, default="member") # 'owner', 'admin', 'member'
    joined_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    server = relationship("Server", back_populates="members")
    user = relationship("User")

class DMParticipant(Base):
    __tablename__ = "dm_participants"

    channel_id = Column(String(36), ForeignKey("channels.id", ondelete="CASCADE"), primary_key=True)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    channel = relationship("Channel", back_populates="dm_participants")
    user = relationship("User")

class Message(Base):
    __tablename__ = "messages"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    channel_id = Column(String(36), ForeignKey("channels.id", ondelete="CASCADE"), nullable=False, index=True)
    sender_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    content = Column(Text, nullable=False)
    attachments = Column(JSON, nullable=True, default=list)
    nonce = Column(String(64), nullable=True, index=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now, index=True)

    channel = relationship("Channel", back_populates="messages")
    sender = relationship("User")

Index("idx_messages_channel_created", Message.channel_id, Message.created_at.desc())

class Friendship(Base):
    __tablename__ = "friendships"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    friend_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    status = Column(String(16), nullable=False, default="pending", index=True) # 'pending', 'accepted', 'blocked'
    created_at = Column(DateTime(timezone=True), nullable=False, default=utc_now)

    user = relationship("User", foreign_keys=[user_id])
    friend = relationship("User", foreign_keys=[friend_id])

Index("idx_friendship_pair", Friendship.user_id, Friendship.friend_id, unique=True)
Index("idx_friendship_user_status", Friendship.user_id, Friendship.status)
Index("idx_friendship_friend_status", Friendship.friend_id, Friendship.status)


class MessageReadReceipt(Base):
    """Tracks per-user read receipts for messages (enables ✓✓ delivered state)."""
    __tablename__ = "message_read_receipts"

    message_id = Column(String(36), ForeignKey("messages.id", ondelete="CASCADE"), primary_key=True)
    user_id    = Column(String(36), ForeignKey("users.id",    ondelete="CASCADE"), primary_key=True)
    read_at    = Column(DateTime(timezone=True), nullable=False, default=utc_now)

Index("idx_read_receipts_user", MessageReadReceipt.user_id)

