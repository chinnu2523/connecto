from typing import List, Optional
from fastapi import APIRouter, Depends, File, HTTPException, Query, UploadFile, status
from sqlalchemy import select, or_, and_, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user, get_current_user_optional
from app.core.storage import process_and_save_avatar, process_and_save_banner
from app.core.otp import send_unified_otp, verify_unified_otp, clean_phone_number
from app.db.models.user import User
from app.db.models.chat import Friendship
from app.schemas.user import UserResponse, ProfileUpdate, ContactChangeRequest, ContactChangeVerify
from app.schemas.chat import UserSearchResult

router = APIRouter(prefix="/users", tags=["Users & Profiles"])

@router.get("/search", response_model=List[UserSearchResult])
async def search_users(
    q: str = Query("", max_length=64),
    limit: int = Query(30, ge=1, le=100),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: AsyncSession = Depends(get_db)
):
    """Searches registered community users by username or display name."""
    clean_q = q.strip().lower()
    if not clean_q:
        if current_user:
            stmt = select(User).where(User.id != current_user.id).order_by(User.created_at.desc()).limit(limit)
        else:
            stmt = select(User).order_by(User.created_at.desc()).limit(limit)
    else:
        query_pattern = f"%{clean_q}%"
        if current_user:
            stmt = (
                select(User)
                .where(
                    User.id != current_user.id,
                    or_(
                        func.lower(User.username).like(query_pattern),
                        func.lower(User.display_name).like(query_pattern)
                    )
                )
                .order_by(User.username.asc())
                .limit(limit)
            )
        else:
            stmt = (
                select(User)
                .where(
                    or_(
                        func.lower(User.username).like(query_pattern),
                        func.lower(User.display_name).like(query_pattern)
                    )
                )
                .order_by(User.username.asc())
                .limit(limit)
            )
    users = (await db.execute(stmt)).scalars().all()
    if not users:
        return []

    status_map = {}
    if current_user:
        user_ids = [u.id for u in users]
        friendships_stmt = select(Friendship).where(
            or_(
                and_(Friendship.user_id == current_user.id, Friendship.friend_id.in_(user_ids)),
                and_(Friendship.friend_id == current_user.id, Friendship.user_id.in_(user_ids))
            )
        )
        friendships = (await db.execute(friendships_stmt)).scalars().all()
        for f in friendships:
            if f.status == "accepted":
                other_id = f.friend_id if f.user_id == current_user.id else f.user_id
                status_map[other_id] = "friends"
            elif f.status == "pending":
                if f.user_id == current_user.id:
                    status_map[f.friend_id] = "pending_sent"
                else:
                    status_map[f.user_id] = "pending_received"

    results = []
    for u in users:
        results.append(UserSearchResult(
            id=u.id,
            username=u.username,
            display_name=u.display_name,
            avatar_url=u.avatar_url,
            relation_status=status_map.get(u.id, "none"),
            created_at=u.created_at
        ))
    return results

@router.get("", response_model=List[UserResponse])
async def list_users(
    limit: int = Query(50, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Returns list of registered community users."""
    stmt = select(User).where(User.id != current_user.id).order_by(User.created_at.desc()).limit(limit)
    res = await db.execute(stmt)
    return res.scalars().all()

@router.get("/me", response_model=UserResponse)
async def get_my_profile(
    current_user: User = Depends(get_current_user)
):
    """Returns profile information for the authenticated user."""
    return current_user

@router.patch("/me", response_model=UserResponse)
@router.put("/me", response_model=UserResponse)
async def update_my_profile(
    profile_data: ProfileUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Updates user personal details according to system policy:
    - full_name: EDITABLE
    - display_name: EDITABLE
    - bio: EDITABLE
    - gender: EDITABLE
    - location: EDITABLE
    - date_of_birth: EDITABLE ONCE (locked if already set)
    - username: EDITABLE ONCE (enforced via username_changed)
    - email / phone: Requires separate verified OTP flow
    """
    if profile_data.full_name is not None:
        current_user.full_name = profile_data.full_name.strip()

    if profile_data.display_name is not None:
        cleaned_disp = profile_data.display_name.strip()
        if cleaned_disp:
            current_user.display_name = cleaned_disp

    if profile_data.bio is not None:
        current_user.bio = profile_data.bio.strip()

    if profile_data.gender is not None:
        current_user.gender = profile_data.gender.strip()

    if profile_data.location is not None:
        current_user.location = profile_data.location.strip()

    # Date of Birth: 1-Time Setup / Age Verification Integrity
    if profile_data.date_of_birth is not None:
        new_dob = profile_data.date_of_birth.strip()
        if new_dob:
            if current_user.date_of_birth and current_user.date_of_birth.strip() and current_user.date_of_birth != new_dob:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Date of Birth has already been verified and locked. It cannot be altered."
                )
            current_user.date_of_birth = new_dob

    # Username: 1-Time Change Policy
    if profile_data.username is not None:
        new_u = profile_data.username.strip().lower()
        if new_u and new_u != current_user.username.lower():
            if current_user.username_changed:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Username has already been customized once and is permanently locked."
                )
            existing_user = (await db.execute(select(User).where(User.username == new_u, User.id != current_user.id))).scalar_one_or_none()
            if existing_user:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Username '@{new_u}' is already claimed by another shinobi."
                )
            current_user.username = new_u
            current_user.username_changed = True

    if profile_data.is_stealth is not None:
        current_user.is_stealth = bool(profile_data.is_stealth)
        if current_user.is_stealth:
            current_user.is_online = False
        else:
            from app.core.ws import ws_manager
            uid = str(current_user.id)
            uname = (current_user.username or "").strip().lower()
            current_user.is_online = (
                (uid in ws_manager.active_connections and len(ws_manager.active_connections[uid]) > 0) or
                (uname in ws_manager.active_connections and len(ws_manager.active_connections[uname]) > 0)
            )
        import asyncio
        from app.core.ws import ws_manager
        is_live = not current_user.is_stealth and current_user.is_online
        asyncio.create_task(ws_manager.broadcast_global({
            "type": "presence_update",
            "user_id": current_user.id,
            "username": current_user.username,
            "is_online": is_live,
            "status": "online" if is_live else "offline"
        }))

    av = profile_data.avatar_url or profile_data.avatar or profile_data.picture
    if av is not None:
        clean_av = str(av).strip()
        # Security: only allow server-hosted relative paths or empty (SSRF prevention)
        is_safe = not clean_av or clean_av.startswith("/uploads/") or clean_av.startswith("/static/")
        if is_safe:
            current_user.avatar_url = clean_av[:512]

    if profile_data.banner_url is not None:
        ban = (profile_data.banner_url or "").strip()
        if not ban or ban.startswith("/uploads/") or ban.startswith("/static/"):
            current_user.banner_url = ban or None
    await db.commit()
    await db.refresh(current_user)
    return current_user

@router.post("/me/change-contact/request-otp")
async def request_contact_change_otp(
    req: ContactChangeRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Sends an OTP verification code to the proposed new email or phone number.
    Ensures the new destination is not already taken by another account.
    """
    target = req.new_contact.strip()
    method = req.method.lower().strip()

    if method == "email":
        target = target.lower()
        if "@" not in target or "." not in target:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Please provide a valid email address.")
        existing = (await db.execute(select(User).where(User.email == target, User.id != current_user.id))).scalar_one_or_none()
        if existing:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="This email address is already associated with another account.")
    else:
        target = clean_phone_number(target)
        if len(target) < 7:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Please enter a valid phone number with country code.")
        existing = (await db.execute(select(User).where(User.phone_number == target, User.id != current_user.id))).scalar_one_or_none()
        if existing:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="This phone number is already registered to another user.")

    return await send_unified_otp(
        db=db,
        identifier=target,
        method=method,
        purpose="change_contact",
        user_id=current_user.id
    )

@router.post("/me/change-contact/verify", response_model=UserResponse)
async def verify_contact_change_otp(
    req: ContactChangeVerify,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Verifies the OTP and permanently updates the user's registered Email or Phone number.
    """
    target = req.new_contact.strip()
    method = req.method.lower().strip()
    if method == "email":
        target = target.lower()
    else:
        target = clean_phone_number(target)

    await verify_unified_otp(
        db=db,
        identifier=target,
        otp_code=req.otp_code.strip(),
        purpose="change_contact"
    )

    if method == "email":
        current_user.email = target
    else:
        current_user.phone_number = target

    await db.commit()
    await db.refresh(current_user)
    return current_user

@router.post("/me/avatar", response_model=UserResponse)
async def upload_avatar(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Uploads a new profile avatar.
    Performs real content magic bytes validation, resizes/compresses server-side,
    and stores in object storage.
    """
    file_bytes = await file.read()
    if not file_bytes:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded file is empty."
        )

    # Validate content magic bytes, resize, compress, and save
    avatar_url = process_and_save_avatar(file_bytes)

    current_user.avatar_url = avatar_url
    await db.commit()
    await db.refresh(current_user)

    return current_user

@router.post("/me/banner", response_model=UserResponse)
async def upload_banner(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Uploads a new profile cover banner.
    Validates magic bytes, center-crops to 1200x400 px, compresses to WebP,
    and stores in /uploads/banners/.
    Max upload: 8 MB before server-side compression.
    """
    file_bytes = await file.read()
    if not file_bytes:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded file is empty."
        )

    if len(file_bytes) > 8 * 1024 * 1024:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail="Banner image must be under 8 MB."
        )

    # Validate magic bytes, crop to 1200x400, compress, and save
    banner_url = process_and_save_banner(file_bytes)

    current_user.banner_url = banner_url
    await db.commit()
    await db.refresh(current_user)

    return current_user
