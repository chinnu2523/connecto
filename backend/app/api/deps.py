from datetime import datetime, timezone, timedelta
from typing import AsyncGenerator, Optional
from fastapi import Depends, HTTPException, Request, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.config import settings
from app.core.security import hash_session_token
from app.db.models.user import User, UserSession, TerminatedSession
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
        token_hash = hash_session_token(raw_token)

        # 1. First check if token is blacklisted in terminated_sessions
        term_stmt = select(TerminatedSession).where(
            (TerminatedSession.session_token_hash == token_hash) |
            (TerminatedSession.session_token_hash == raw_token)
        )
        if (await db.execute(term_stmt)).scalar_one_or_none():
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Session has been terminated. Please log in again."
            )

        if raw_token.startswith("cf_edge_"):
            try:
                import hmac
                import hashlib
                import os
                from sqlalchemy import func, or_
                dot_idx = raw_token.rfind(".")
                # SEC-B4-01 FIX: Require valid dot delimiter and non-empty signature part
                if dot_idx == -1:
                    raise HTTPException(
                        status_code=status.HTTP_401_UNAUTHORIZED,
                        detail="Invalid edge token format: missing signature."
                    )
                payload_part = raw_token[8:dot_idx]
                sig_part = raw_token[dot_idx + 1:]
                if not sig_part:
                    raise HTTPException(
                        status_code=status.HTTP_401_UNAUTHORIZED,
                        detail="Invalid edge token format: empty signature."
                    )
                last_us = payload_part.rfind("_")
                if last_us != -1:
                    edge_uid = payload_part[:last_us]
                    ts_part = payload_part[last_us + 1:]
                    token_ts_ms = int(ts_part) if ts_part.isdigit() else 0
                    
                    # SEC-B4-01 FIX: Enforce cryptographic HMAC verification; remove insecure hardcoded fallback
                    valid_sig = False
                    for sec in [os.getenv("SECRET_KEY", ""), getattr(settings, "SECRET_KEY", "")]:
                        if not sec:
                            continue
                        expected = hmac.new(sec.encode("utf-8"), payload_part.encode("utf-8"), hashlib.sha256).hexdigest()[:32]
                        if hmac.compare_digest(sig_part, expected):
                            valid_sig = True
                            break
                    if not valid_sig:
                        raise HTTPException(
                            status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Invalid edge token signature."
                        )
                    clean_uname = edge_uid.lower().removeprefix("usr_")
                    u_stmt = select(User).where(
                        or_(
                            User.id == edge_uid,
                            func.lower(User.username) == edge_uid.lower(),
                            func.lower(User.username) == clean_uname
                        )
                    )
                    edge_user = (await db.execute(u_stmt)).scalar_one_or_none()
                    if edge_user:
                        # Check all_before revocation
                        rev_check = select(TerminatedSession).where(
                            TerminatedSession.session_token_hash == f"all_before_{edge_user.id}"
                        )
                        rev_row = (await db.execute(rev_check)).scalar_one_or_none()
                        if rev_row and rev_row.terminated_at:
                            rev_epoch_ms = int(rev_row.terminated_at.timestamp() * 1000)
                            if token_ts_ms > 0 and token_ts_ms <= rev_epoch_ms:
                                raise HTTPException(
                                    status_code=status.HTTP_401_UNAUTHORIZED,
                                    detail="Session has been terminated from another device. Please log in again."
                                )

                        # Verify that session is active in user_sessions if present
                        sess_stmt = select(UserSession).where(
                            (UserSession.session_token_hash == token_hash) &
                            (UserSession.expires_at > now)
                        )
                        active_sess = (await db.execute(sess_stmt)).scalar_one_or_none()
                        if active_sess:
                            if getattr(edge_user, 'is_banned', False) or getattr(edge_user, 'status', '') == 'banned' or not getattr(edge_user, 'is_active', True):
                                raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account has been suspended or deactivated.")
                            return edge_user

                        # If user has other sessions in user_sessions, but this specific token_hash is NOT among them,
                        # it was explicitly terminated/deleted!
                        user_has_sessions = (await db.execute(select(UserSession).where(UserSession.user_id == edge_user.id))).scalars().first()
                        if user_has_sessions:
                            raise HTTPException(
                                status_code=status.HTTP_401_UNAUTHORIZED,
                                detail="Session has been terminated from another device. Please log in again."
                            )

                        if getattr(edge_user, 'is_banned', False) or getattr(edge_user, 'status', '') == 'banned' or not getattr(edge_user, 'is_active', True):
                            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account has been suspended or deactivated.")
                        return edge_user
            except HTTPException:
                raise
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

    if getattr(user, 'is_banned', False) or getattr(user, 'status', '') == 'banned' or not getattr(user, 'is_active', True):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account has been suspended or deactivated."
        )

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
