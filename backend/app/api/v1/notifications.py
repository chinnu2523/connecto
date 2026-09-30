from typing import List, Optional
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, ConfigDict
from sqlalchemy import select, func, update, delete
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user
from app.db.models.user import User
from app.db.models.notification import Notification
from app.core.ws import ws_manager
from app.core.push import send_push_notification

router = APIRouter(prefix="/notifications", tags=["Notifications"])

class NotificationResponse(BaseModel):
    id: str
    user_id: str
    type: str
    title: str
    content: str
    sender_username: Optional[str] = None
    sender_avatar: Optional[str] = None
    reference_id: Optional[str] = None
    is_read: bool
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class NotificationCountResponse(BaseModel):
    unread_count: int

async def _get_user_identifiers(db: AsyncSession, current_user: User, username: Optional[str] = None) -> List[str]:
    target_user = current_user
    if username and username.strip().lower() != current_user.username.lower():
        if getattr(current_user, "is_admin", False):
            clean_user = username.strip()
            stmt = select(User).where(
                (func.lower(User.username) == clean_user.lower()) | (User.id == clean_user)
            )
            res = await db.execute(stmt)
            found = res.scalar_one_or_none()
            if found:
                target_user = found
        else:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Access denied. You cannot access notifications for other users."
            )

    identifiers = {str(target_user.id)}
    if target_user.username:
        identifiers.add(target_user.username)
        identifiers.add(target_user.username.lower())
    return [i for i in identifiers if i]

async def create_user_notification(
    db: AsyncSession,
    user_id: str,
    type: str,
    title: str,
    content: str,
    sender_username: Optional[str] = None,
    sender_avatar: Optional[str] = None,
    reference_id: Optional[str] = None
) -> Notification:
    """Helper to persist a notification and emit real-time WebSocket event."""
    notification = Notification(
        user_id=user_id,
        type=type,
        title=title,
        content=content,
        sender_username=sender_username,
        sender_avatar=sender_avatar,
        reference_id=reference_id,
        is_read=False
    )
    db.add(notification)
    await db.commit()
    await db.refresh(notification)

    # Real-time WebSocket delivery
    event_data = {
        "type": "notification_received",
        "notification": {
            "id": notification.id,
            "type": notification.type,
            "title": notification.title,
            "content": notification.content,
            "sender_username": notification.sender_username,
            "sender_avatar": notification.sender_avatar,
            "reference_id": notification.reference_id,
            "is_read": False,
            "created_at": notification.created_at.isoformat()
        }
    }
    await ws_manager.send_personal_event(user_id, event_data)

    # Send FCM push notification to the recipient's Android device (if registered)
    try:
        stmt = select(User).where((User.id == user_id) | (User.username == user_id))
        target_user = (await db.execute(stmt)).scalar_one_or_none()
        if target_user and target_user.fcm_token:
            import asyncio as _aio
            _aio.create_task(send_push_notification(
                fcm_token=target_user.fcm_token,
                title=title,
                body=content,
                data={
                    "type": type,
                    "sender": sender_username or "",
                    "sender_username": sender_username or "",
                    "caller_name": sender_username or "",
                    "title": title,
                    "body": content,
                    "reference_id": reference_id or "",
                    "room_id": reference_id or ""
                },
                notification_type=type
            ))
    except Exception as _push_e:
        import logging
        logging.getLogger(__name__).warning(f"[PUSH] FCM send failed: {_push_e}")

    return notification

@router.get("", response_model=List[NotificationResponse])
async def get_notifications(
    unread_only: bool = Query(False),
    limit: int = Query(50, ge=1, le=100),
    username: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Retrieves notifications for the user with optional unread filter."""
    identifiers = await _get_user_identifiers(db, current_user, username)
    if not identifiers:
        return []

    stmt = select(Notification).where(Notification.user_id.in_(identifiers))
    if unread_only:
        stmt = stmt.where(Notification.is_read == False)
    stmt = stmt.order_by(Notification.created_at.desc()).limit(limit)

    res = await db.execute(stmt)
    return res.scalars().all()

@router.get("/count", response_model=NotificationCountResponse)
async def get_unread_count(
    username: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Returns the unread notifications count for the user."""
    identifiers = await _get_user_identifiers(db, current_user, username)
    if not identifiers:
        return NotificationCountResponse(unread_count=0)

    stmt = select(func.count(Notification.id)).where(
        Notification.user_id.in_(identifiers),
        Notification.is_read == False
    )
    res = await db.execute(stmt)
    count = res.scalar() or 0
    return NotificationCountResponse(unread_count=count)

@router.post("/{notification_id}/read")
async def mark_notification_read(
    notification_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Marks a single notification as read if owned by current user."""
    identifiers = await _get_user_identifiers(db, current_user)
    stmt = update(Notification).where(
        Notification.id == notification_id,
        Notification.user_id.in_(identifiers)
    ).values(is_read=True)
    res = await db.execute(stmt)
    await db.commit()
    if res.rowcount == 0 and not getattr(current_user, "is_admin", False):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Notification not found")
    return {"status": "ok", "id": notification_id}

@router.post("/read-all")
async def mark_all_notifications_read(
    username: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Marks all notifications for the user as read."""
    identifiers = await _get_user_identifiers(db, current_user, username)
    stmt = update(Notification).where(
        Notification.user_id.in_(identifiers),
        Notification.is_read == False
    ).values(is_read=True)
    await db.execute(stmt)
    await db.commit()
    return {"status": "ok"}

@router.delete("/{notification_id}")
async def delete_notification(
    notification_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Deletes a specific notification if owned by current user."""
    identifiers = await _get_user_identifiers(db, current_user)
    stmt = delete(Notification).where(
        Notification.id == notification_id,
        Notification.user_id.in_(identifiers)
    )
    res = await db.execute(stmt)
    await db.commit()
    if res.rowcount == 0 and not getattr(current_user, "is_admin", False):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Notification not found")
    return {"status": "ok", "deleted": notification_id}

@router.delete("")
async def clear_notifications(
    username: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Clears all notifications for the user."""
    identifiers = await _get_user_identifiers(db, current_user, username)
    stmt = delete(Notification).where(Notification.user_id.in_(identifiers))
    await db.execute(stmt)
    await db.commit()
    return {"status": "ok"}
