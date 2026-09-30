from datetime import datetime
from typing import List, Optional, Any
from pydantic import BaseModel, ConfigDict, Field

class ServerCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=64)
    icon_url: Optional[str] = None

class ServerResponse(BaseModel):
    id: str
    name: str
    icon_url: Optional[str] = None
    owner_id: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class ChannelCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=64)
    type: str = Field("text", pattern="^(text|voice)$")

class ChannelResponse(BaseModel):
    id: str
    server_id: Optional[str] = None
    name: str
    type: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class MessageCreate(BaseModel):
    content: str = Field(..., min_length=1, max_length=4096)
    nonce: Optional[str] = Field(None, description="Client optimistic UI temp ID")
    attachments: Optional[Any] = Field(default_factory=list)

class MessageResponse(BaseModel):
    id: str
    channel_id: str
    channel_name: Optional[str] = None
    sender_id: str
    sender_username: str
    sender_display_name: str
    sender_avatar_url: Optional[str] = None
    content: str
    text: Optional[str] = None
    type: Optional[str] = "text"
    poll_id: Optional[str] = None
    poll: Optional[Any] = None
    timer_seconds: Optional[int] = None
    expires_at: Optional[str] = None
    attachments: Optional[Any] = None
    nonce: Optional[str] = None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class FriendRequestCreate(BaseModel):
    friend_username: str

class FriendshipResponse(BaseModel):
    id: str
    user_id: str
    friend_id: str
    friend_username: str
    friend_display_name: str
    friend_avatar_url: Optional[str] = None
    status: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class UserSearchResult(BaseModel):
    id: str
    username: str
    display_name: str
    avatar_url: Optional[str] = None
    relation_status: str = "none" # "none", "pending_sent", "pending_received", "friends"
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class FriendRequestItem(BaseModel):
    id: str
    sender_id: str
    sender_username: str
    sender_display_name: str
    sender_avatar_url: Optional[str] = None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
