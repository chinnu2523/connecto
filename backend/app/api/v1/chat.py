from datetime import datetime, timezone, timedelta
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, Request, status
from sqlalchemy import select, or_, and_, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user, get_current_user_optional
from app.core.moderation import check_auto_mod, check_rate_limit
from app.core.ws import ws_manager
from app.db.models.user import User
from app.db.models.chat import Server, Channel, ServerMember, DMParticipant, Message, Friendship
from app.schemas.chat import (
    ServerCreate, ServerResponse, ChannelCreate, ChannelResponse,
    MessageCreate, MessageResponse, FriendRequestCreate, FriendshipResponse,
    FriendRequestItem
)

router = APIRouter(prefix="/chat", tags=["Chat & Real-Time Messaging"])

# --- SERVERS & CHANNELS ---

@router.post("/servers", response_model=ServerResponse, status_code=status.HTTP_201_CREATED)
async def create_server(
    data: ServerCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    server = Server(
        name=data.name,
        icon_url=data.icon_url,
        owner_id=current_user.id
    )
    db.add(server)
    await db.commit()
    await db.refresh(server)

    # Add owner as server member
    member = ServerMember(
        server_id=server.id,
        user_id=current_user.id,
        role="owner"
    )
    db.add(member)

    # Create default 'general' text channel
    default_channel = Channel(
        server_id=server.id,
        name="general",
        type="text"
    )
    db.add(default_channel)
    await db.commit()

    return server

@router.get("/servers", response_model=List[ServerResponse])
async def list_my_servers(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = (
        select(Server)
        .join(ServerMember, ServerMember.server_id == Server.id)
        .where(ServerMember.user_id == current_user.id)
    )
    res = await db.execute(stmt)
    return res.scalars().all()

@router.post("/servers/{server_id}/channels", response_model=ChannelResponse, status_code=status.HTTP_201_CREATED)
async def create_channel(
    server_id: str,
    data: ChannelCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Authorization check
    member_stmt = select(ServerMember).where(
        ServerMember.server_id == server_id,
        ServerMember.user_id == current_user.id
    )
    member = (await db.execute(member_stmt)).scalar_one_or_none()
    if not member or member.role not in ("owner", "admin"):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Permission denied. Only server owners/admins can create channels."
        )

    channel = Channel(
        server_id=server_id,
        name=data.name.lower().replace(" ", "-"),
        type=data.type
    )
    db.add(channel)
    await db.commit()
    await db.refresh(channel)

    try:
        from app.core.cache import app_cache, CACHE_KEY_CHANNELS
        from app.core.ws import ws_manager
        await app_cache.delete(CACHE_KEY_CHANNELS)
        await ws_manager.broadcast_to_all({
            'type': 'channels_updated',
            'server_id': server_id,
            'channel_id': channel.id,
            'channel_name': channel.name
        })
    except Exception as e:
        print(f'[WS_CHANNEL_BROADCAST_ERROR] {e}')

    return channel

@router.get("/servers/{server_id}/channels", response_model=List[ChannelResponse])
async def list_server_channels(
    server_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Authorization check
    member_stmt = select(ServerMember).where(
        ServerMember.server_id == server_id,
        ServerMember.user_id == current_user.id
    )
    member = (await db.execute(member_stmt)).scalar_one_or_none()
    if not member:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not a member of this server.")

    stmt = select(Channel).where(Channel.server_id == server_id).order_by(Channel.created_at.asc())
    res = await db.execute(stmt)
    return res.scalars().all()

@router.get("/channels", response_model=List[ChannelResponse])
async def list_public_channels(
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: AsyncSession = Depends(get_db)
):
    stmt = (
        select(Channel)
        .where(Channel.server_id.is_(None), ~Channel.name.startswith("dm-"))
        .order_by(Channel.created_at.asc())
    )
    res = await db.execute(stmt)
    channels = res.scalars().all()
    # Filter out any UUID-named temporary channels
    filtered = [ch for ch in channels if not (len(ch.name) == 36 and ch.name.count("-") == 4)]

    if not filtered:
        # Seed all 8 default public community channels
        default_channels = [
            {"name": "general", "type": "text"},
            {"name": "announcements", "type": "text"},
            {"name": "dev-chat", "type": "text"},
            {"name": "gaming", "type": "text"},
            {"name": "war-room", "type": "text"},
            {"name": "tournaments", "type": "text"},
            {"name": "clips", "type": "text"},
            {"name": "voice-lounge", "type": "voice"},
        ]
        created_channels = []
        for ch_def in default_channels:
            ch = Channel(server_id=None, name=ch_def["name"], type=ch_def["type"])
            db.add(ch)
            created_channels.append(ch)
        await db.commit()
        for ch in created_channels:
            await db.refresh(ch)
        return created_channels

    return filtered

# --- DIRECT MESSAGES ---

@router.post("/dm/start", response_model=ChannelResponse)
async def start_direct_message(
    target_username: str,
    request: Request = None,
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: AsyncSession = Depends(get_db)
):
    caller = current_user
    if not caller:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication required to start direct messages."
        )
    current_user = caller
    clean_target = target_username.strip().lower().removeprefix("@")
    target_stmt = select(User).where(func.lower(User.username) == clean_target)
    target_user = (await db.execute(target_stmt)).scalar_one_or_none()
    if not target_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Target user not found.")

    if target_user.id == current_user.id:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cannot start a DM with yourself.")

    # Check if DM channel already exists between these 2 users by participant lookup
    dm_stmt = (
        select(Channel.id)
        .join(DMParticipant, DMParticipant.channel_id == Channel.id)
        .where(
            Channel.server_id.is_(None),
            DMParticipant.user_id.in_([current_user.id, target_user.id])
        )
        .group_by(Channel.id)
        .having(func.count(DMParticipant.user_id) == 2)
    )
    existing_channel_id = (await db.execute(dm_stmt)).scalar_one_or_none()

    canonical_dm_name = f"dm-{min(current_user.username, target_user.username)}-{max(current_user.username, target_user.username)}"

    if existing_channel_id:
        existing_ch = (await db.execute(select(Channel).where(Channel.id == existing_channel_id))).scalar_one()
        return existing_ch

    # Also check if channel exists by canonical name
    name_stmt = select(Channel).where(Channel.name == canonical_dm_name)
    existing_name_ch = (await db.execute(name_stmt)).scalar_one_or_none()
    if existing_name_ch:
        # Ensure participants are recorded
        p1 = (await db.execute(select(DMParticipant).where(DMParticipant.channel_id == existing_name_ch.id, DMParticipant.user_id == current_user.id))).scalar_one_or_none()
        if not p1:
            db.add(DMParticipant(channel_id=existing_name_ch.id, user_id=current_user.id))
        p2 = (await db.execute(select(DMParticipant).where(DMParticipant.channel_id == existing_name_ch.id, DMParticipant.user_id == target_user.id))).scalar_one_or_none()
        if not p2:
            db.add(DMParticipant(channel_id=existing_name_ch.id, user_id=target_user.id))
        await db.commit()
        return existing_name_ch

    # Create new canonical DM channel
    dm_channel = Channel(
        server_id=None,
        name=canonical_dm_name,
        type="text"
    )
    db.add(dm_channel)
    await db.commit()
    await db.refresh(dm_channel)

    # Add both participants
    db.add(DMParticipant(channel_id=dm_channel.id, user_id=current_user.id))
    db.add(DMParticipant(channel_id=dm_channel.id, user_id=target_user.id))
    await db.commit()

    return dm_channel

# --- MESSAGES ---

@router.get("/channels/{channel_id}/messages", response_model=List[MessageResponse])
async def list_channel_messages(
    channel_id: str,
    request: Request = None,
    limit: int = Query(50, ge=1, le=100),
    before: Optional[str] = Query(None, description="ISO timestamp cursor to fetch messages before"),
    username: Optional[str] = Query(None),
    user: Optional[str] = Query(None),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: AsyncSession = Depends(get_db)
):
    clean_ch = channel_id.strip().removeprefix("#")
    # Resolve channel by ID or name
    stmt_ch = select(Channel).where(or_(Channel.id == clean_ch, Channel.name == clean_ch.lower()))
    ch = (await db.execute(stmt_ch)).scalars().first()
    target_id = ch.id if ch else clean_ch
    ch_name = ch.name if ch else clean_ch.lower()

    # Security: Strict BOLA / Authorization enforcement for Direct Messages
    target_clean = (ch_name or clean_ch).lower()
    if (ch and ch.type == "dm") or target_clean.startswith("dm-") or target_clean.startswith("dm_") or target_clean.startswith("dm:"):
        caller_user = current_user
        if not caller_user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Authentication required to view direct messages."
            )
        is_participant = False
        if ch:
            p_stmt = select(DMParticipant.user_id).where(DMParticipant.channel_id == ch.id)
            p_ids = set((await db.execute(p_stmt)).scalars().all())
            if caller_user.id in p_ids:
                is_participant = True
        if not is_participant:
            parts = target_clean.replace("dm_", "dm-").replace("dm:", "dm-").split("-")
            if len(parts) >= 3:
                u1, u2 = parts[1].lower(), parts[2].lower()
                if caller_user.username.lower() in (u1, u2):
                    is_participant = True
                    if ch:
                        try:
                            db.add(DMParticipant(channel_id=ch.id, user_id=caller_user.id))
                            await db.commit()
                        except Exception:
                            pass
        if not is_participant and not getattr(caller_user, "is_admin", False):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Access denied. You are not a participant in this direct conversation."
            )

    # Permanent message history retention across channel UUID and all canonical/alt/reverse aliases
    possible_channel_ids = [target_id, clean_ch, ch_name]
    if (ch and ch.type == "dm") or target_clean.startswith("dm-") or target_clean.startswith("dm_") or target_clean.startswith("dm:"):
        parts = target_clean.replace("dm_", "dm-").replace("dm:", "dm-").split("-")
        if len(parts) >= 3:
            u1, u2 = parts[1].lower(), parts[2].lower()
            canon1 = f"dm-{min(u1, u2)}-{max(u1, u2)}"
            canon2 = f"dm_{min(u1, u2)}_{max(u1, u2)}"
            rev1 = f"dm-{max(u1, u2)}-{min(u1, u2)}"
            rev2 = f"dm_{max(u1, u2)}_{min(u1, u2)}"
            possible_channel_ids.extend([canon1, canon2, rev1, rev2])
            ch_by_name = (await db.execute(select(Channel).where(Channel.name.in_([canon1, canon2, rev1, rev2])))).scalars().all()
            for c in ch_by_name:
                possible_channel_ids.extend([c.id, c.name])

    possible_channel_ids = list(set(filter(None, possible_channel_ids)))

    stmt = (
        select(Message, User)
        .join(User, Message.sender_id == User.id, isouter=True)
        .where(Message.channel_id.in_(possible_channel_ids))
    )

    if before:
        try:
            before_clean = before.replace("Z", "+00:00")
            before_dt = datetime.fromisoformat(before_clean)
            stmt = stmt.where(Message.created_at < before_dt)
        except Exception:
            pass

    # Order by created_at desc to get most recent messages within limit, then reverse for chronological order
    stmt = stmt.order_by(Message.created_at.desc()).limit(limit)
    res = await db.execute(stmt)
    rows = list(res.all())
    rows.reverse()

    output = []
    for msg, sender in rows:
        att = msg.attachments
        if isinstance(att, str) and att.strip().startswith("{"):
            try:
                import json
                att = json.loads(att)
            except Exception:
                att = {}
        elif not isinstance(att, dict):
            att = {}

        msg_type = att.get("type", "text") if isinstance(att, dict) else "text"
        poll_id = att.get("poll_id") if isinstance(att, dict) else None
        poll_obj = att.get("poll") if isinstance(att, dict) else None
        timer_seconds = att.get("timer_seconds") if isinstance(att, dict) else None
        expires_at = att.get("expires_at") if isinstance(att, dict) else None

        output.append(MessageResponse(
            id=msg.id,
            channel_id=msg.channel_id,
            channel_name=ch_name,
            sender_id=msg.sender_id or (sender.id if sender else "guest"),
            sender_username=sender.username if sender else "gamer",
            sender_display_name=(sender.display_name or sender.username) if sender else "Gamer",
            sender_avatar_url=sender.avatar_url if sender else None,
            content=msg.content,
            text=msg.content,
            type=msg_type,
            poll_id=poll_id,
            poll=poll_obj,
            timer_seconds=timer_seconds,
            expires_at=expires_at,
            attachments=msg.attachments or [],
            nonce=msg.nonce,
            created_at=msg.created_at
        ))
    return output


@router.post("/friends/unfriend")
@router.post("/friends/remove")
@router.delete("/friends/{friend_id}")
async def unfriend_chat_router_endpoint(
    friend_id: str = None,
    payload: dict = None,
    request: Request = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    from app.core.ws import ws_manager
    if payload is None and request:
        try:
            payload = await request.json()
        except Exception:
            payload = {}
    if payload is None:
        payload = {}

    target = (
        payload.get("friend_username") or
        payload.get("username") or
        payload.get("target_username") or
        payload.get("recipient") or
        payload.get("friend") or
        friend_id or
        ""
    ).strip().lower()

    if not target:
        raise HTTPException(status_code=400, detail="Missing target friend username or ID")

    target_user = (await db.execute(select(User).where(or_(User.username == target, User.id == target)))).scalar_one_or_none()
    if not target_user:
        raise HTTPException(status_code=404, detail="User not found")

    stmt = select(Friendship).where(
        or_(
            and_(Friendship.user_id == current_user.id, Friendship.friend_id == target_user.id),
            and_(Friendship.user_id == target_user.id, Friendship.friend_id == current_user.id)
        )
    )
    friendship = (await db.execute(stmt)).scalars().first()
    if friendship:
        await db.delete(friendship)
        await db.commit()
        await ws_manager.send_personal_event(current_user.id, {"type": "friend_removed", "friend_username": target_user.username, "friend_id": target_user.id})
        await ws_manager.send_personal_event(target_user.id, {"type": "friend_removed", "friend_username": current_user.username, "friend_id": current_user.id})
        return {"status": "ok", "message": f"Unfriended {target_user.username} successfully", "friend_username": target_user.username}

    return {"status": "ok", "message": "Friendship already removed"}


# ==================== MESSAGE FULL-TEXT SEARCH ====================

@router.get("/search/messages")
async def search_messages(
    q: str = Query(..., min_length=2, max_length=100, description="Search query"),
    channel_id: Optional[str] = Query(None, description="Limit search to a specific channel"),
    limit: int = Query(20, ge=1, le=50),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Full-text message search across channels the user has access to."""
    pattern = f"%{q.strip()}%"
    stmt = (
        select(Message, User)
        .join(User, Message.sender_id == User.id, isouter=True)
        .where(Message.content.ilike(pattern))
    )
    if channel_id:
        clean_ch = channel_id.strip().lower().removeprefix("#")
        stmt_ch = select(Channel).where(or_(Channel.id == clean_ch, Channel.name == clean_ch.lower()))
        ch = (await db.execute(stmt_ch)).scalar_one_or_none()
        ch_name = ch.name if ch else clean_ch
        target_clean = (ch_name or clean_ch).lower()
        if (ch and ch.type == "dm") or target_clean.startswith("dm-") or target_clean.startswith("dm_"):
            is_participant = False
            if ch:
                p_stmt = select(DMParticipant.user_id).where(DMParticipant.channel_id == ch.id)
                p_ids = set((await db.execute(p_stmt)).scalars().all())
                if current_user.id in p_ids:
                    is_participant = True
            if not is_participant:
                parts = target_clean.replace("dm_", "dm-").split("-")
                if len(parts) >= 3 and current_user.username.lower() in (parts[1].lower(), parts[2].lower()):
                    is_participant = True
            if not is_participant and not getattr(current_user, "is_admin", False):
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Access denied. You cannot search private direct messages you are not part of."
                )
        stmt = stmt.where(
            or_(Message.channel_id == clean_ch, Message.channel_id == channel_id, Message.channel_id == ch_name)
        )
    else:
        # Exclude unauthorized private direct messages from cross-channel search
        if not getattr(current_user, "is_admin", False):
            p_stmt = select(DMParticipant.channel_id).where(DMParticipant.user_id == current_user.id)
            user_dm_ids = set((await db.execute(p_stmt)).scalars().all())
            user_pattern1 = f"dm-{current_user.username.lower()}-%"
            user_pattern2 = f"dm-%-{current_user.username.lower()}"
            user_pattern3 = f"dm_{current_user.username.lower()}_%"
            user_pattern4 = f"dm_%_{current_user.username.lower()}"
            allowed_conditions = [
                and_(~Message.channel_id.ilike("dm-%"), ~Message.channel_id.ilike("dm_%")),
                Message.channel_id.ilike(user_pattern1),
                Message.channel_id.ilike(user_pattern2),
                Message.channel_id.ilike(user_pattern3),
                Message.channel_id.ilike(user_pattern4)
            ]
            if user_dm_ids:
                allowed_conditions.append(Message.channel_id.in_(user_dm_ids))
            stmt = stmt.where(or_(*allowed_conditions))
    stmt = stmt.order_by(Message.created_at.desc()).limit(limit)
    rows = (await db.execute(stmt)).all()

    results = []
    for msg, sender in rows:
        results.append({
            "id": msg.id,
            "channel_id": msg.channel_id,
            "sender_username": sender.username if sender else "unknown",
            "sender_display_name": (sender.display_name or sender.username) if sender else "Unknown",
            "sender_avatar_url": sender.avatar_url if sender else None,
            "content": msg.content,
            "created_at": msg.created_at.isoformat() if msg.created_at else None,
        })

    return {
        "status": "ok",
        "query": q,
        "total": len(results),
        "results": results,
    }

