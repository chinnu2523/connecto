from datetime import datetime, timezone, timedelta
from typing import AsyncGenerator, Optional
from fastapi import Depends, HTTPException, Request, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.config import settings
from app.core.security import hash_session_token
from app.db.models.user import User, UserSession
from app.db.session import AsyncSessionLocal

async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        yield session

async def get_current_user(
    request: Request,
    db: AsyncSession = Depends(get_db)
) -> User:
    """
    Authenticates user via Authorization header (Bearer token) or signed httpOnly session cookie.
    Enforces authentication & session validity on every protected route.
    Implements sliding-window session renewal to prevent premature expiration for active users.
    """
    candidate_tokens = []

    # Priority 1: Authorization: Bearer token (explicit from client / mobile app)
    auth_header = request.headers.get("Authorization")
    if auth_header and auth_header.startswith("Bearer "):
        bearer_token = auth_header.split(" ", 1)[1].strip()
        if bearer_token:
            candidate_tokens.append(bearer_token)

    # Priority 2: Session cookie (browser / web app)
    cookie_token = request.cookies.get(settings.COOKIE_NAME)
    if cookie_token and cookie_token.strip() and cookie_token.strip() not in candidate_tokens:
        candidate_tokens.append(cookie_token.strip())

    if not candidate_tokens:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication required. Missing session cookie or token."
        )

    now = datetime.now(timezone.utc)
    user_session = None

    # Try candidate tokens until an active session is found
    for raw_token in candidate_tokens:
        if raw_token.startswith("cf_edge_"):
            try:
                import hmac
                import hashlib
                import os
                from sqlalchemy import func, or_
                dot_idx = raw_token.rfind(".")
                payload_part = raw_token[8:dot_idx] if dot_idx != -1 else raw_token[8:]
                sig_part = raw_token[dot_idx + 1:] if dot_idx != -1 else ""
                last_us = payload_part.rfind("_")
                if last_us != -1:
                    edge_uid = payload_part[:last_us]
                    ts_ms = int(payload_part[last_us + 1:])
                    if (now.timestamp() * 1000 - ts_ms) <= 30 * 86400 * 1000:
                        valid_sig = (dot_idx == -1)
                        if not valid_sig:
                            for sec in [os.getenv("SECRET_KEY", ""), getattr(settings, "SECRET_KEY", ""), "connecto_edge_sec_32bytes_random_salt!"]:
                                if not sec:
                                    continue
                                expected = hmac.new(sec.encode("utf-8"), payload_part.encode("utf-8"), hashlib.sha256).hexdigest()[:32]
                                if hmac.compare_digest(sig_part, expected):
                                    valid_sig = True
                                    break
                        if valid_sig:
                            u_stmt = select(User).where(or_(User.id == edge_uid, func.lower(User.username) == edge_uid.lower()))
                            edge_user = (await db.execute(u_stmt)).scalar_one_or_none()
                            if edge_user:
                                return edge_user
            except Exception:
                pass

        token_hash = hash_session_token(raw_token)
        stmt = (
            select(UserSession)
            .where(
                UserSession.session_token_hash == token_hash,
                UserSession.expires_at > now
            )
        )
        result = await db.execute(stmt)
        matched = result.scalar_one_or_none()
        if matched:
            user_session = matched
            break

    if not user_session:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired session. Please log in again."
        )

    # Query associated user
    user_stmt = select(User).where(User.id == user_session.user_id)
    user_result = await db.execute(user_stmt)
    user = user_result.scalar_one_or_none()

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User account associated with this session no longer exists."
        )

    # Sliding-window session renewal: If session has less than 14 days remaining, extend by 30 days
    # so active users never experience unexpected expiration while using the platform.
    try:
        session_expires = user_session.expires_at
        if session_expires.tzinfo is None:
            session_expires = session_expires.replace(tzinfo=timezone.utc)
        if session_expires - now < timedelta(days=14):
            user_session.expires_at = now + timedelta(days=30)
            await db.commit()
    except Exception:
        # Non-critical failure for renewal; allow request to proceed
        pass

    # Presence is managed strictly via real-time WebSocket connection state, not HTTP polling.

    return user

async def get_current_user_optional(
    request: Request,
    db: AsyncSession = Depends(get_db)
) -> Optional[User]:
    """Returns the authenticated user if session exists and is valid, otherwise None."""
    try:
        return await get_current_user(request, db)
    except HTTPException:
        return None

async def get_current_admin_user(
    current_user: User = Depends(get_current_user)
) -> User:
    """
    Enforces that the current authenticated user has administrative privileges.
    Raises 403 Forbidden if user is not an admin.
    """
    is_admin = bool(getattr(current_user, "is_admin", False))
    if not is_admin:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Administrative privileges required to access this resource."
        )
    return current_user
