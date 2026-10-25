import asyncio
import re
import secrets
import logging
logger = logging.getLogger("connecto.auth")
from datetime import datetime, timedelta, timezone
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Request, Response, status
from sqlalchemy import select, or_, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user, get_current_user_optional
from app.core.config import settings
from app.core.security import hash_password, verify_password, needs_rehash, generate_session_token, hash_session_token
from app.core.otp import send_unified_otp, verify_unified_otp, clean_phone_number, mask_phone_number, mask_email
from app.db.models.user import User, UserSession, OTPVerification, TerminatedSession
from app.schemas.user import (
    UserSignup, UserLogin, UserResponse,
    SignupRequestOtp, SignupVerifyOtp,
    ForgotPasswordRequest, ForgotPasswordVerify, ForgotPasswordReset,
    TwoFactorToggleRequest, TwoFactorRequestOtp, TwoFactorVerifyOtp, TwoFactorLoginVerify
)

router = APIRouter(prefix="/auth", tags=["Authentication"])

def generate_numeric_otp(length: int = 6) -> str:
    """Generates a secure numeric OTP string of specified length."""
    return "".join(secrets.choice("0123456789") for _ in range(length))

def is_otp_expired(dt: Optional[datetime], margin_minutes: int = 0) -> bool:
    """Safely checks if an OTP expiration datetime is in the past, handling naive/aware datetimes."""
    if dt is None:
        return True
    naive_dt = dt.replace(tzinfo=None) if dt.tzinfo else dt
    cutoff = datetime.utcnow() - timedelta(minutes=margin_minutes)
    return naive_dt < cutoff

async def find_user_by_identifier(db: AsyncSession, identifier: str) -> Optional[User]:
    """Finds a user by username, email, or phone number."""
    clean = identifier.strip()
    clean_lower = clean.lower()
    clean_no_at = clean_lower.lstrip("@")
    clean_digits = clean_phone_number(clean)

    conditions = [
        func.lower(User.username) == clean_lower,
        func.lower(User.username) == clean_no_at,
        func.lower(User.email) == clean_lower,
        func.lower(User.email) == clean_no_at
    ]
    if clean_digits and len(clean_digits) >= 7:
        conditions.append(User.phone_number == clean_digits)
        conditions.append(User.phone_number == clean)

    stmt = select(User).where(or_(*conditions))
    result = await db.execute(stmt)
    return result.scalar_one_or_none()

def extract_request_user_agent(request: Request) -> str:
    ua = request.headers.get("User-Agent") or ""
    dev_model = request.headers.get("X-Phone-Type") or request.headers.get("X-Device-Model") or request.headers.get("X-Device-Name")
    dev_ver = request.headers.get("X-Phone-Version") or request.headers.get("X-Device-Version") or request.headers.get("X-Device-OS")
    dev_type = request.headers.get("X-Device-Type")

    parts = []
    if dev_model and "model:" not in ua.lower():
        parts.append(f"Model: {dev_model}")
    if dev_ver and "version:" not in ua.lower():
        parts.append(f"Version: {dev_ver}")
    if dev_type and "type:" not in ua.lower():
        parts.append(f"Type: {dev_type}")

    if parts:
        combined = f"{ua} | {' | '.join(parts)}"
        return combined[:500]
    return ua[:500]

def parse_session_device_details(ua: str, headers: Optional[dict] = None) -> dict:
    device_model = "Unknown Device"
    phone_version = "Unknown OS"
    device_type = "Desktop"
    display_name = "Web Browser"

    ua = ua or ""
    lower_ua = ua.lower()

    h_model = (headers.get("x-phone-type") or headers.get("x-device-model") or headers.get("x-device-name") or "").strip() if headers else ""
    h_ver = (headers.get("x-phone-version") or headers.get("x-device-version") or headers.get("x-device-os") or "").strip() if headers else ""
    h_type = (headers.get("x-device-type") or "").strip() if headers else ""

    if "model:" in lower_ua:
        m = re.search(r"model:\s*([^|;]+)", ua, re.IGNORECASE)
        if m:
            h_model = h_model or m.group(1).strip()
    if "version:" in lower_ua:
        m = re.search(r"version:\s*([^|;]+)", ua, re.IGNORECASE)
        if m:
            h_ver = h_ver or m.group(1).strip()
    if "type:" in lower_ua:
        m = re.search(r"type:\s*([^|;]+)", ua, re.IGNORECASE)
        if m:
            h_type = h_type or m.group(1).strip()

    if "android" in lower_ua:
        device_type = "Smartphone"
        android_ver_match = re.search(r"android\s+([0-9\.]+)", ua, re.IGNORECASE)
        android_ver_num = android_ver_match.group(1) if android_ver_match else ""

        api_match = re.search(r"api\s+([0-9]+)", ua, re.IGNORECASE)
        api_level = api_match.group(1) if api_match else ""

        if android_ver_num:
            phone_version = f"Android {android_ver_num} (API {api_level})" if api_level else f"Android {android_ver_num}"
        else:
            phone_version = "Android"

        model_candidate = ""
        paren_match = re.search(r"\(([^)]+)\)", ua)
        if paren_match:
            parts = [p.strip() for p in paren_match.group(1).split(";")]
            filtered = [
                p for p in parts
                if not re.search(r"^(linux|u|wv|[a-z]{2}-[a-z]{2}|android\s+[0-9\.]+|api\s+[0-9]+|k|mobile)$", p.strip(), re.IGNORECASE)
            ]
            if filtered:
                raw_model = filtered[-1]
                raw_model = re.sub(r"\s*Build/.*$", "", raw_model, flags=re.IGNORECASE).strip()
                if raw_model:
                    model_candidate = raw_model

        if h_model:
            device_model = h_model
        elif model_candidate:
            device_model = model_candidate
        else:
            device_model = "Android Device"

        if h_ver:
            phone_version = h_ver
        if h_type:
            device_type = h_type
        elif "tablet" in lower_ua:
            device_type = "Tablet"
        elif any(k in lower_ua for k in ("sdk_gphone", "emulator", "google_sdk")):
            device_type = "Smartphone (Emulator)"

        display_name = device_model

    elif "iphone" in lower_ua:
        device_type = "Smartphone"
        device_model = "Apple iPhone"
        ios_match = re.search(r"os\s+([0-9_]+)", ua, re.IGNORECASE)
        phone_version = f"iOS {ios_match.group(1).replace('_', '.')}" if ios_match else "iOS"
        display_name = "Apple iPhone"

    elif "ipad" in lower_ua:
        device_type = "Tablet"
        device_model = "Apple iPad"
        ios_match = re.search(r"os\s+([0-9_]+)", ua, re.IGNORECASE)
        phone_version = f"iPadOS {ios_match.group(1).replace('_', '.')}" if ios_match else "iPadOS"
        display_name = "Apple iPad"

    elif "macintosh" in lower_ua or "mac os" in lower_ua:
        device_type = "Computer"
        device_model = "Apple Mac"
        mac_match = re.search(r"mac os x\s+([0-9_]+)", ua, re.IGNORECASE)
        phone_version = f"macOS {mac_match.group(1).replace('_', '.')}" if mac_match else "macOS"
        if "chrome" in lower_ua:
            display_name = "Chrome on macOS"
        elif "safari" in lower_ua:
            display_name = "Safari on macOS"
        elif "firefox" in lower_ua:
            display_name = "Firefox on macOS"
        else:
            display_name = "macOS Browser"

    elif "windows" in lower_ua:
        device_type = "Computer"
        device_model = "Windows PC"
        phone_version = "Windows"
        if "edg" in lower_ua:
            display_name = "Edge on Windows"
        elif "chrome" in lower_ua:
            display_name = "Chrome on Windows"
        elif "firefox" in lower_ua:
            display_name = "Firefox on Windows"
        else:
            display_name = "Windows PC"

    elif "linux" in lower_ua:
        device_type = "Computer"
        device_model = "Linux PC"
        phone_version = "Linux"
        display_name = "Linux Browser"

    if h_model:
        device_model = h_model
        display_name = h_model
    if h_ver:
        phone_version = h_ver
    if h_type:
        device_type = h_type

    return {
        "device_name": display_name,
        "phone_type": device_model,
        "phone_version": phone_version,
        "os_version": phone_version,
        "device_type": device_type
    }

@router.post("/signup/request-otp")
async def signup_request_otp(
    req: SignupRequestOtp,
    db: AsyncSession = Depends(get_db)
):
    """
    Sends a 6-digit email OTP for sign-up verification.
    Ensures email is not already registered.
    """
    clean_e = req.email.strip().lower()
    stmt = select(User).where(func.lower(User.email) == clean_e)
    existing = (await db.execute(stmt)).scalar_one_or_none()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="This email address is already registered. Please sign in instead."
        )

    res = await send_unified_otp(
        db=db,
        identifier=clean_e,
        method="email",
        purpose="signup_verification"
    )
    return {
        "status": "ok",
        "message": f"Verification code sent to {res.get('masked_destination', clean_e)}",
        "masked_destination": res.get("masked_destination", mask_email(clean_e))
    }

@router.post("/signup/verify-otp")
async def signup_verify_otp(
    req: SignupVerifyOtp,
    db: AsyncSession = Depends(get_db)
):
    """
    Verifies the email OTP before account creation.
    """
    clean_e = req.email.strip().lower()
    otp_record = await verify_unified_otp(
        db=db,
        identifier=clean_e,
        otp_code=req.otp_code.strip(),
        purpose="signup_verification"
    )
    return {
        "status": "ok",
        "verified": True,
        "email": clean_e,
        "message": "Email verified successfully ✓"
    }

@router.post("/verify-email/request-otp")
async def verify_email_request_otp(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Sends a 6-digit email OTP to existing users to verify their email within the 7-day grace period.
    """
    if current_user.is_email_verified:
        return {
            "status": "ok",
            "message": "Your email is already verified.",
            "is_email_verified": True
        }

    clean_e = current_user.email.strip().lower()
    res = await send_unified_otp(
        db=db,
        identifier=clean_e,
        method="email",
        purpose="email_verification",
        user_id=current_user.id
    )
    return {
        "status": "ok",
        "message": f"Verification code sent to {res.get('masked_destination', clean_e)}",
        "masked_destination": res.get("masked_destination", mask_email(clean_e))
    }

@router.post("/verify-email/confirm")
async def verify_email_confirm(
    req: SignupVerifyOtp,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Confirms email verification OTP for an existing user and unblocks their account.
    """
    clean_e = current_user.email.strip().lower()
    otp_record = await verify_unified_otp(
        db=db,
        identifier=clean_e,
        otp_code=req.otp_code.strip(),
        purpose="email_verification"
    )
    current_user.is_email_verified = True
    current_user.verification_deadline = None
    await db.commit()
    await db.refresh(current_user)

    return {
        "status": "ok",
        "verified": True,
        "is_email_verified": True,
        "is_temporarily_blocked": False,
        "message": "Email successfully verified! Your account is fully unlocked."
    }

@router.get("/check-username")
async def check_username(
    request: Request,
    username: str = "",
    db: AsyncSession = Depends(get_db)
):
    """
    Checks if a username is valid and available in the database.
    Rate limited to prevent username enumeration attacks.
    """
    from app.core.rate_limit import enforce_rate_limit
    enforce_rate_limit(request, key_prefix="check_username", max_requests=20, window_seconds=60.0)
    import re
    from sqlalchemy import func
    clean_u = username.strip().lower()
    
    if not clean_u:
        return {
            "status": "error",
            "available": False,
            "valid": False,
            "username": clean_u,
            "message": "Username cannot be empty."
        }
    
    if len(clean_u) < 3 or len(clean_u) > 32:
        return {
            "status": "error",
            "available": False,
            "valid": False,
            "username": clean_u,
            "message": "Username must be between 3 and 32 characters."
        }
        
    if not re.match(r"^[a-zA-Z0-9_]+$", clean_u):
        return {
            "status": "error",
            "available": False,
            "valid": False,
            "username": clean_u,
            "message": "Username may only contain letters, numbers, and underscores without spaces."
        }
    # Enforce test account rule: test, guest, and mock usernames are completely disabled
    test_patterns = [r"^test", r"^guest", r"^audit", r"^shinobi_[ab]", r"^antiflood", r"^mock", r"^diag_user"]
    if any(re.search(pat, clean_u) for pat in test_patterns) and clean_u not in ("chinnu", "chinnu14754x"):
        return {
            "status": "error",
            "available": False,
            "valid": False,
            "username": clean_u,
            "message": "Registration of test and guest accounts is strictly disabled."
        }

    stmt = select(User).where(func.lower(User.username) == clean_u)
    user = (await db.execute(stmt)).scalar_one_or_none()
    if user:
        return {
            "status": "ok",
            "available": False,
            "valid": True,
            "username": clean_u,
            "message": f"Username '{clean_u}' is already taken."
        }
        
    return {
        "status": "ok",
        "available": True,
        "valid": True,
        "username": clean_u,
        "message": f"Username '{clean_u}' is available ✓"
    }

@router.get("/check-email")
async def check_email(
    email: str = "",
    db: AsyncSession = Depends(get_db)
):
    """
    Checks if an email address is valid format and available in the database.
    """
    import re
    from sqlalchemy import func
    clean_e = email.strip().lower()
    
    if not clean_e:
        return {
            "status": "error",
            "valid": False,
            "available": False,
            "email": clean_e,
            "message": "Email address cannot be empty."
        }
        
    email_regex = r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$"
    if not re.match(email_regex, clean_e) or len(clean_e) < 5:
        return {
            "status": "error",
            "valid": False,
            "available": False,
            "email": clean_e,
            "message": "Please enter a valid email address (e.g. name@example.com)."
        }
        
    stmt = select(User).where(func.lower(User.email) == clean_e)
    user = (await db.execute(stmt)).scalar_one_or_none()
    if user:
        return {
            "status": "ok",
            "valid": True,
            "available": False,
            "email": clean_e,
            "message": "This email address is already registered. Please sign in."
        }
        
    return {
        "status": "ok",
        "valid": True,
        "available": True,
        "email": clean_e,
        "message": "Email is valid & verified ✓"
    }

@router.post("/signup", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def signup(
    signup_data: UserSignup,
    response: Response,
    request: Request,
    db: AsyncSession = Depends(get_db)
):
    """
    Creates a new user account.
    Passwords are strictly hashed with Argon2id.
    Issues a secure, httpOnly session cookie.
    """
    clean_username = signup_data.username.strip().lower()
    clean_email = signup_data.email.strip().lower()
    clean_display_name = signup_data.display_name.strip() if (signup_data.display_name and signup_data.display_name.strip()) else clean_username

    # Enforce test accounts policy: creating test or guest accounts is strictly blocked
    test_patterns = [r"^test", r"^guest", r"^audit", r"^shinobi_[ab]", r"^antiflood", r"^mock", r"^diag_user", r"^member_\w{6}", r"^ninja_"]
    is_test_username = any(re.search(pat, clean_username) for pat in test_patterns)
    is_test_email = clean_email.endswith("@connecto.test") or clean_email.endswith("@example.com") or any(re.search(pat, clean_email) for pat in test_patterns)
    if is_test_username or is_test_email:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Registration of test/guest suite accounts is strictly disabled."
        )

    # Check if username or email already exists
    stmt = select(User).where(
        or_(
            User.username == clean_username,
            User.email == clean_email
        )
    )
    existing_user = (await db.execute(stmt)).scalar_one_or_none()
    if existing_user:
        if existing_user.username == clean_username:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Username is already taken. Please choose a different username."
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this email address already exists. Please sign in instead."
            )

    # Verify email OTP if provided, or verify that a signup OTP verification record exists for this email
    if signup_data.otp_code:
        await verify_unified_otp(
            db=db,
            identifier=clean_email,
            otp_code=signup_data.otp_code.strip(),
            purpose="signup_verification"
        )
    else:
        # Check if email was previously verified in this session via /auth/signup/verify-otp
        verified_stmt = (
            select(OTPVerification)
            .where(
                OTPVerification.identifier == clean_email,
                OTPVerification.purpose == "signup_verification",
                OTPVerification.is_verified == True
            )
            .order_by(OTPVerification.created_at.desc())
        )
        verified_record = (await db.execute(verified_stmt)).scalars().first()
        if not verified_record:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Please verify your email address with the 6-digit OTP code before creating an account."
            )

    # Hash password with Argon2id
    hashed_pwd = hash_password(signup_data.password)

    # Create new user
    new_user = User(
        username=clean_username,
        display_name=clean_display_name,
        email=clean_email,
        password_hash=hashed_pwd,
        date_of_birth=signup_data.date_of_birth,
        username_changed=False,
        is_email_verified=True,
        verification_deadline=None
    )
    db.add(new_user)
    await db.commit()
    await db.refresh(new_user)

    # Create session
    raw_token = generate_session_token()
    token_hash = hash_session_token(raw_token)
    expires = datetime.now(timezone.utc) + timedelta(days=settings.SESSION_EXPIRE_DAYS)

    session = UserSession(
        user_id=new_user.id,
        session_token_hash=token_hash,
        ip_address=request.client.host if request.client else None,
        user_agent=extract_request_user_agent(request),
        expires_at=expires
    )
    db.add(session)
    await db.commit()

    # Set httpOnly cookie
    response.set_cookie(
        key=settings.COOKIE_NAME,
        value=raw_token,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,
        max_age=settings.SESSION_EXPIRE_DAYS * 86400,
        path="/"
    )

    # Return user response with mobile session token
    user_dict = {
        "id": new_user.id,
        "username": new_user.username,
        "display_name": new_user.display_name,
        "email": new_user.email,
        "phone_number": new_user.phone_number,
        "two_factor_enabled": False,
        "two_factor_method": "sms",
        "requires_2fa": False,
        "avatar_url": new_user.avatar_url,
        "is_admin": bool(getattr(new_user, "is_admin", False)),
        "username_changed": new_user.username_changed,
        "is_email_verified": True,
        "verification_deadline": None,
        "created_at": new_user.created_at,
        "banner_url": getattr(new_user, "banner_url", None),
        "token": raw_token
    }
    return UserResponse(**user_dict)

@router.post("/login", response_model=UserResponse, status_code=status.HTTP_200_OK)
async def login(
    login_data: UserLogin,
    response: Response,
    request: Request,
    db: AsyncSession = Depends(get_db)
):
    """
    Authenticates user using ONLY login identifier (username/email/phone) and password.
    Verifies Argon2id password hash.
    If 2FA is enabled on the account, issues an OTP challenge via httpSMS or Email.
    Otherwise, sets secure httpOnly session cookie and returns full session.
    """
    user = await find_user_by_identifier(db, login_data.login)

    # Strictly read-only cryptographic password verification
    pwd_clean = (login_data.password or "").strip()
    is_valid_pwd = verify_password(user.password_hash, login_data.password) if user else False
    if not is_valid_pwd and user and pwd_clean != login_data.password:
        is_valid_pwd = verify_password(user.password_hash, pwd_clean)

    if not user or not is_valid_pwd:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username/email or password."
        )

    # Sole admin policy: ONLY chinnu14754x and connecto_admin are admin, all others are members
    is_adm = (user.username.lower() in ("chinnu14754x", "connecto_admin"))
    if bool(user.is_admin) != is_adm:
        user.is_admin = is_adm
        await db.commit()
        await db.refresh(user)

    # Check if Two-Factor Authentication (2FA) is enabled for this account
    if getattr(user, "two_factor_enabled", False):
        # 2FA is delivered strictly via email only
        two_fa_method = "email"
        destination = user.email or f"{user.username}@connecto.fun"

        otp_result = await send_unified_otp(
            db=db,
            identifier=destination,
            method="email",
            purpose="two_factor_login",
            user_id=user.id
        )

        return UserResponse(
            id=user.id,
            username=user.username,
            display_name=user.display_name,
            email=user.email,
            phone_number=user.phone_number,
            two_factor_enabled=True,
            two_factor_method="email",
            requires_2fa=True,
            masked_destination=otp_result["masked_destination"],
            is_admin=is_adm,
            username_changed=user.username_changed,
            created_at=user.created_at,
            token=None,
            dev_otp=None
        )

    # Standard Login (No 2FA Required)
    raw_token = generate_session_token()
    token_hash = hash_session_token(raw_token)
    expires = datetime.now(timezone.utc) + timedelta(days=settings.SESSION_EXPIRE_DAYS)

    session = UserSession(
        user_id=user.id,
        session_token_hash=token_hash,
        ip_address=request.client.host if request.client else None,
        user_agent=extract_request_user_agent(request),
        expires_at=expires
    )
    db.add(session)
    await db.commit()

    # Set httpOnly cookie
    response.set_cookie(
        key=settings.COOKIE_NAME,
        value=raw_token,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,
        max_age=settings.SESSION_EXPIRE_DAYS * 86400,
        path="/"
    )

    user_dict = {
        "id": user.id,
        "username": user.username,
        "display_name": user.display_name,
        "email": user.email,
        "phone_number": user.phone_number,
        "two_factor_enabled": False,
        "two_factor_method": getattr(user, "two_factor_method", "sms"),
        "requires_2fa": False,
        "avatar_url": user.avatar_url,
        "is_admin": is_adm,
        "username_changed": user.username_changed,
        "is_email_verified": getattr(user, "is_email_verified", False),
        "verification_deadline": getattr(user, "verification_deadline", None),
        "created_at": user.created_at,
        "banner_url": getattr(user, "banner_url", None),
        "token": raw_token
    }
    return UserResponse(**user_dict)

@router.post("/logout", status_code=status.HTTP_200_OK)
async def logout(
    request: Request,
    response: Response,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Logs out the user by destroying the active session in DB and clearing session cookie.
    Also sets is_online = False so presence shows correctly.
    """
    session_token = request.cookies.get(settings.COOKIE_NAME)
    if not session_token:
        auth_header = request.headers.get("Authorization")
        if auth_header and auth_header.startswith("Bearer "):
            session_token = auth_header.split(" ", 1)[1]

    if session_token:
        token_hash = hash_session_token(session_token)
        stmt = select(UserSession).where(UserSession.session_token_hash == token_hash)
        session_obj = (await db.execute(stmt)).scalar_one_or_none()
        if session_obj:
            await db.delete(session_obj)

    # Mark user as offline
    current_user.is_online = False
    await db.commit()

    # Clear cookie
    response.delete_cookie(
        key=settings.COOKIE_NAME,
        path="/",
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE
    )

    return {"message": "Logged out successfully."}


@router.post("/login/2fa-verify", response_model=UserResponse, status_code=status.HTTP_200_OK)
async def verify_login_2fa(
    verify_data: TwoFactorLoginVerify,
    response: Response,
    request: Request,
    db: AsyncSession = Depends(get_db)
):
    """
    Verifies 2FA OTP code submitted during login and issues a valid session token.
    """
    user = await find_user_by_identifier(db, verify_data.login)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User account not found."
        )

    destination = user.email or f"{user.username}@connecto.fun"

    await verify_unified_otp(
        db=db,
        identifier=destination,
        otp_code=verify_data.otp_code,
        purpose="two_factor_login"
    )

    # Issue session
    raw_token = generate_session_token()
    token_hash = hash_session_token(raw_token)
    expires = datetime.now(timezone.utc) + timedelta(days=settings.SESSION_EXPIRE_DAYS)

    session = UserSession(
        user_id=user.id,
        session_token_hash=token_hash,
        ip_address=request.client.host if request.client else None,
        user_agent=extract_request_user_agent(request),
        expires_at=expires
    )
    db.add(session)
    await db.commit()

    response.set_cookie(
        key=settings.COOKIE_NAME,
        value=raw_token,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,
        max_age=settings.SESSION_EXPIRE_DAYS * 86400,
        path="/"
    )

    is_adm = bool(getattr(user, "is_admin", False) or getattr(user, "is_recruiter", False))

    user_dict = {
        "id": user.id,
        "username": user.username,
        "display_name": user.display_name,
        "email": user.email,
        "phone_number": user.phone_number,
        "two_factor_enabled": user.two_factor_enabled,
        "two_factor_method": user.two_factor_method,
        "requires_2fa": False,
        "avatar_url": user.avatar_url,
        "is_admin": is_adm,
        "username_changed": user.username_changed,
        "is_email_verified": getattr(user, "is_email_verified", False),
        "verification_deadline": getattr(user, "verification_deadline", None),
        "created_at": user.created_at,
        "banner_url": getattr(user, "banner_url", None),
        "token": raw_token
    }
    return UserResponse(**user_dict)


@router.post("/forgot-password/request-otp", status_code=status.HTTP_200_OK)
async def forgot_password_request_otp(
    req: ForgotPasswordRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    Initiates password recovery via SMS (using httpSMS) or Email.
    Accepts identifier (email, phone, or username) and method ('sms' or 'email').
    """
    raw_id = req.identifier.strip()
    method = (req.method or "sms").lower()

    user = await find_user_by_identifier(db, raw_id)

    target_email = None
    target_phone = None

    if user:
        target_email = user.email
        target_phone = user.phone_number
    else:
        if "@" in raw_id:
            target_email = raw_id.lower()
        else:
            cleaned = clean_phone_number(raw_id)
            if cleaned and len(cleaned) >= 7:
                target_phone = cleaned

    if method == "sms" and not target_phone and target_email:
        method = "email"

    target = clean_phone_number(target_phone) if (method == "sms" and target_phone) else (target_email.lower() if target_email else None)
    if not target:
        if method == "sms":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No phone number is registered for this account. Please select Email verification."
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No email address found for this account."
            )

    return await send_unified_otp(
        db=db,
        identifier=target,
        method=method,
        purpose="forgot_password",
        user_id=user.id if user else None
    )


@router.post("/forgot-password/verify-otp", status_code=status.HTTP_200_OK)
async def forgot_password_verify_otp(
    req: ForgotPasswordVerify,
    db: AsyncSession = Depends(get_db)
):
    """
    Real-time verification of OTP. Returns success so frontend can unlock password reset field.
    """
    otp_record = await verify_unified_otp(
        db=db,
        identifier=req.identifier,
        otp_code=req.otp_code,
        purpose="forgot_password"
    )
    return {
        "status": "ok",
        "verified": True,
        "identifier": otp_record.identifier,
        "message": "Verification code verified successfully! You may now set your new password."
    }


@router.post("/forgot-password/reset", status_code=status.HTTP_200_OK)
async def forgot_password_reset(
    req: ForgotPasswordReset,
    response: Response,
    request: Request,
    db: AsyncSession = Depends(get_db)
):
    """
    Resets user password with verified OTP. Automatically creates a session and logs the user in.
    """
    if len(req.new_password) < 8:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="New password must be at least 8 characters long."
        )

    raw_id = req.identifier.strip().lower()
    user = await find_user_by_identifier(db, raw_id)

    # Check if there is an already-verified OTP record for this identifier
    from app.core.otp import verify_otp_hash
    conditions = [
        OTPVerification.identifier == raw_id,
        OTPVerification.identifier == clean_phone_number(raw_id)
    ]
    if user:
        conditions.append(OTPVerification.user_id == user.id)
        if user.email:
            conditions.append(OTPVerification.identifier == user.email.lower())
        if user.phone_number:
            conditions.append(OTPVerification.identifier == clean_phone_number(user.phone_number))

    now = datetime.now(timezone.utc)
    stmt = select(OTPVerification).where(
        or_(*conditions),
        OTPVerification.purpose == "forgot_password",
        OTPVerification.is_verified == True
    ).order_by(OTPVerification.created_at.desc())
    verified_records = (await db.execute(stmt)).scalars().all()
    otp_record = None
    for rec in verified_records:
        if verify_otp_hash(req.otp_code.strip(), rec.otp_code):
            expires = rec.expires_at.replace(tzinfo=timezone.utc) if rec.expires_at.tzinfo is None else rec.expires_at
            if expires + timedelta(minutes=15) > now:
                otp_record = rec
                break

    if not otp_record:
        # Not yet verified; verify now with rate-limiting and anti-brute-force
        otp_record = await verify_unified_otp(
            db=db,
            identifier=req.identifier,
            otp_code=req.otp_code,
            purpose="forgot_password"
        )

    # Find the user to update
    target_user = None
    if otp_record.user_id:
        target_user = (await db.execute(select(User).where(User.id == otp_record.user_id))).scalar_one_or_none()
    if not target_user and user:
        target_user = user
    if not target_user:
        target_user = await find_user_by_identifier(db, otp_record.identifier)

    if not target_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User account associated with this verification could not be found."
        )

    # Update password
    target_user.password_hash = hash_password(req.new_password)
    target_user.updated_at = datetime.now(timezone.utc)

    # Delete used OTP
    await db.delete(otp_record)

    # Invalidate existing sessions
    old_sessions = (await db.execute(select(UserSession).where(UserSession.user_id == target_user.id))).scalars().all()
    for s in old_sessions:
        await db.delete(s)

    # Create fresh session
    raw_token = generate_session_token()
    token_hash = hash_session_token(raw_token)
    expires = datetime.now(timezone.utc) + timedelta(days=settings.SESSION_EXPIRE_DAYS)

    session = UserSession(
        user_id=target_user.id,
        session_token_hash=token_hash,
        ip_address=request.client.host if request.client else None,
        user_agent=extract_request_user_agent(request),
        expires_at=expires
    )
    db.add(session)
    await db.commit()
    await db.refresh(target_user)

    # Immediately push updated user credentials to Cloudflare D1
    try:
        from app.db.d1_sync import d1_sync_manager
        asyncio.create_task(asyncio.to_thread(d1_sync_manager.sync_table_to_cloud, "users"))
    except Exception as e:
        logger.warning(f"[AUTH] Could not immediately push password reset to D1: {e}")

    response.set_cookie(
        key=settings.COOKIE_NAME,
        value=raw_token,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,
        max_age=settings.SESSION_EXPIRE_DAYS * 86400,
        path="/"
    )

    is_adm = bool(getattr(target_user, "is_admin", False) or getattr(target_user, "is_recruiter", False))

    return {
        "status": "ok",
        "message": "Password reset successfully! You are now logged in.",
        "token": raw_token,
        "user": {
            "id": target_user.id,
            "username": target_user.username,
            "display_name": target_user.display_name,
            "email": target_user.email,
            "phone_number": target_user.phone_number,
            "two_factor_enabled": target_user.two_factor_enabled,
            "two_factor_method": target_user.two_factor_method,
            "avatar_url": target_user.avatar_url,
            "is_admin": is_adm,
            "username_changed": target_user.username_changed,
            "created_at": target_user.created_at,
            "token": raw_token
        }
    }


@router.post("/2fa/request-otp", status_code=status.HTTP_200_OK)
async def two_factor_request_otp(
    req: TwoFactorRequestOtp,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Sends an OTP to setup or verify 2FA on the authenticated account.
    """
    target = current_user.email.lower() if current_user.email else f"{current_user.username}@connecto.fun"

    return await send_unified_otp(
        db=db,
        identifier=target,
        method="email",
        purpose="two_factor_setup",
        user_id=current_user.id
    )


@router.post("/2fa/verify", status_code=status.HTTP_200_OK)
async def two_factor_verify(
    req: TwoFactorVerifyOtp,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Verifies OTP and enables Two-Factor Authentication for the account via Email.
    """
    target = current_user.email.lower() if current_user.email else f"{current_user.username}@connecto.fun"

    await verify_unified_otp(
        db=db,
        identifier=target,
        otp_code=req.otp_code,
        purpose="two_factor_setup"
    )

    current_user.two_factor_enabled = True
    current_user.two_factor_method = "email"

    await db.commit()
    await db.refresh(current_user)

    return {
        "status": "ok",
        "two_factor_enabled": True,
        "two_factor_method": "email",
        "phone_number": current_user.phone_number,
        "message": "Two-Factor Authentication successfully activated via EMAIL!"
    }


@router.post("/2fa/toggle", status_code=status.HTTP_200_OK)
async def two_factor_toggle(
    req: TwoFactorToggleRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Enables or disables 2FA directly for the authenticated user.
    """
    if not req.enabled:
        if current_user.two_factor_enabled:
            otp_val = (req.otp_code or req.otp or "").strip()
            if not otp_val:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="A valid 6-digit verification code from your email is required to turn off Two-Factor Authentication."
                )
            ident = current_user.email or current_user.username
            await verify_unified_otp(
                db=db,
                identifier=ident,
                otp_code=otp_val,
                purpose="two_factor_disable",
                user_id=current_user.id
            )
        current_user.two_factor_enabled = False
        await db.commit()
        await db.refresh(current_user)
        return {
            "status": "ok",
            "two_factor_enabled": False,
            "two_factor_method": current_user.two_factor_method,
            "message": "Two-Factor Authentication disabled successfully."
        }
    else:
        if req.phone_number:
            current_user.phone_number = clean_phone_number(req.phone_number)
        if req.method:
            current_user.two_factor_method = req.method.lower()
        current_user.two_factor_enabled = True
        await db.commit()
        await db.refresh(current_user)
        return {
            "status": "ok",
            "two_factor_enabled": True,
            "two_factor_method": current_user.two_factor_method,
            "phone_number": current_user.phone_number,
            "message": "Two-Factor Authentication enabled."
        }


def verify_httpsms_webhook_authenticity(request: Request, raw_body: bytes) -> bool:
    """
    Verifies that incoming webhook requests originate from an authorized httpSMS gateway.
    Validates:
    1. Secret / API key headers (x-webhook-secret, x-api-key, Authorization: Bearer).
    2. HMAC-SHA256 signature headers (x-httpsms-signature, x-signature-sha256).
    3. Query parameter secret token fallback (?secret=... or ?api_key=...).
    """
    import hmac
    import hashlib

    secrets_to_check = []
    if getattr(settings, "HTTPSMS_WEBHOOK_SECRET", None) and settings.HTTPSMS_WEBHOOK_SECRET.strip():
        secrets_to_check.append(settings.HTTPSMS_WEBHOOK_SECRET.strip())
    if getattr(settings, "HTTPSMS_API_KEY", None) and settings.HTTPSMS_API_KEY.strip():
        secrets_to_check.append(settings.HTTPSMS_API_KEY.strip())

    if not secrets_to_check:
        return False

    # 1. Check API Key / Secret headers
    req_secret = (
        request.headers.get("x-webhook-secret") or
        request.headers.get("x-api-key") or
        request.headers.get("x-httpsms-key") or
        request.headers.get("x-httpsms-secret") or
        ""
    ).strip()

    auth_header = request.headers.get("authorization", "").strip()
    if auth_header.startswith("Bearer "):
        req_secret = req_secret or auth_header.removeprefix("Bearer ").strip()

    # Query param fallback
    if not req_secret:
        req_secret = (
            request.query_params.get("secret") or
            request.query_params.get("api_key") or
            request.query_params.get("token") or
            ""
        ).strip()

    if req_secret:
        for sec in secrets_to_check:
            if hmac.compare_digest(req_secret.encode("utf-8"), sec.encode("utf-8")):
                return True

    # 2. Check HMAC signature headers (x-httpsms-signature, x-signature-sha256, x-hub-signature-256)
    sig_header = (
        request.headers.get("x-httpsms-signature") or
        request.headers.get("x-signature-sha256") or
        request.headers.get("x-hub-signature-256") or
        ""
    ).strip()

    if sig_header:
        candidate_sigs = []
        if sig_header.startswith("sha256="):
            candidate_sigs.append(sig_header[7:].strip())
        elif "v1=" in sig_header:
            for part in sig_header.split(","):
                if part.strip().startswith("v1="):
                    candidate_sigs.append(part.strip()[3:].strip())
        else:
            candidate_sigs.append(sig_header)

        for sec in secrets_to_check:
            expected_hmac = hmac.new(sec.encode("utf-8"), raw_body, hashlib.sha256).hexdigest()
            for cand in candidate_sigs:
                if hmac.compare_digest(cand.lower(), expected_hmac.lower()):
                    return True

    return False


@router.post("/httpsms-webhook", status_code=status.HTTP_200_OK)
async def httpsms_webhook(request: Request, db: AsyncSession = Depends(get_db)):
    """
    Receives incoming webhook events from httpSMS (https://github.com/NdoleStudio/httpsms.git)
    Events handled:
    - message.phone.received: An incoming SMS received on user's phone / SIM
    - message.send.delivered / message.send.failed
    """
    import json
    try:
        raw_body = await request.body()
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Unable to read request body")

    if not verify_httpsms_webhook_authenticity(request, raw_body):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Unauthorized: Missing or invalid webhook signature/API secret"
        )

    try:
        payload = json.loads(raw_body)
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid JSON payload")

    if not isinstance(payload, dict):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid payload: Expected JSON object")

    event_type = payload.get("event")
    data = payload.get("data")
    if not isinstance(data, dict):
        data = {}

    print(f"[httpSMS Webhook] Event: {event_type} | Data: {data}")

    if event_type == "message.phone.received":
        contact = data.get("contact")
        content = (data.get("content") or "").strip()
        owner = data.get("owner")
        print(f"[httpSMS Inbound SMS] From: {contact} | Content: {content} | To: {owner}")

        # If incoming SMS contains a 6-digit verification code, auto-verify pending OTP
        if content.isdigit() and len(content) == 6 and contact:
            clean_contact = clean_phone_number(contact)
            stmt = select(OTPVerification).where(
                OTPVerification.identifier == clean_contact,
                OTPVerification.is_verified == False
            ).order_by(OTPVerification.created_at.desc())
            records = (await db.execute(stmt)).scalars().all()
            for rec in records:
                from app.core.otp import verify_otp_hash
                if verify_otp_hash(content, rec.otp_code) and not is_otp_expired(rec.expires_at):
                    rec.is_verified = True
                    await db.commit()
                    print(f"[httpSMS Webhook] Successfully auto-verified OTP from {clean_contact}")
                    break

    return {"status": "ok", "event": event_type}



# =========================================================================
# Legacy / Cross-Platform Route Aliases (For Android Release & Web Compat)
# =========================================================================

from pydantic import BaseModel

class LegacyForgotPwRequest(BaseModel):
    identifier: str

class LegacyVerifyOtpRequest(BaseModel):
    username: str
    otp: str

class LegacyResetPwWithOtpRequest(BaseModel):
    username: str
    otp: str
    new_password: str = Field(..., min_length=8, max_length=128)

class LegacyVerify2FaRequest(BaseModel):
    username: str
    otp: str

class LegacyResendOtpRequest(BaseModel):
    username: str

@router.post("/forgot-password", status_code=status.HTTP_200_OK)
async def legacy_forgot_password(req: LegacyForgotPwRequest, db: AsyncSession = Depends(get_db)):
    raw_id = req.identifier.strip()
    user = await find_user_by_identifier(db, raw_id)
    if user and user.email:
        method = "email"
    elif "@" in raw_id:
        method = "email"
    else:
        method = "sms"
    return await forgot_password_request_otp(ForgotPasswordRequest(identifier=raw_id, method=method), db=db)

@router.post("/reset-password-with-otp", status_code=status.HTTP_200_OK)
async def legacy_reset_password(req: LegacyResetPwWithOtpRequest, response: Response, request: Request, db: AsyncSession = Depends(get_db)):
    return await forgot_password_reset(ForgotPasswordReset(identifier=req.username, otp_code=req.otp, new_password=req.new_password), response=response, request=request, db=db)

@router.post("/verify-2fa-otp", status_code=status.HTTP_200_OK)
async def legacy_verify_2fa(req: LegacyVerify2FaRequest, response: Response, request: Request, db: AsyncSession = Depends(get_db)):
    return await verify_login_2fa(TwoFactorLoginVerify(login=req.username, otp_code=req.otp), response=response, request=request, db=db)

@router.post("/resend-otp", status_code=status.HTTP_200_OK)
async def legacy_resend_otp(req: LegacyResendOtpRequest, db: AsyncSession = Depends(get_db)):
    user = await find_user_by_identifier(db, req.username)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User account not found.")
    dest = user.email or f"{user.username}@connecto.fun"
    return await send_unified_otp(db=db, identifier=dest, method="email", purpose="two_factor_login", user_id=user.id)


class VerifyOldPasswordPayload(BaseModel):
    username: Optional[str] = None
    old_password: str

class ChangePasswordPayload(BaseModel):
    username: Optional[str] = None
    old_password: str
    new_password: str

@router.post("/verify-old-password", status_code=status.HTTP_200_OK)
async def verify_old_password_endpoint(
    req: VerifyOldPasswordPayload,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # SEC-B06 FIX: Require authenticated session; verify caller's own password
    target_user = current_user
    
    clean_pwd = (req.old_password or "").strip()
    is_valid = verify_password(target_user.password_hash, req.old_password)
    if not is_valid and clean_pwd != req.old_password:
        is_valid = verify_password(target_user.password_hash, clean_pwd)
    
    if not is_valid:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Incorrect old password.")
    
    return {
        "status": "ok",
        "valid": True,
        "message": "Old password verified successfully! You may now enter your new password."
    }

@router.post("/change-password", status_code=status.HTTP_200_OK)
async def change_password_endpoint(
    req: ChangePasswordPayload,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    target_user = current_user

    if not req.old_password or not req.new_password:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Both old and new passwords are required.")
    
    if len(req.new_password) < 8:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="New password must be at least 8 characters long.")

    clean_pwd = (req.old_password or "").strip()
    is_valid = verify_password(target_user.password_hash, req.old_password)
    if not is_valid and clean_pwd != req.old_password:
        is_valid = verify_password(target_user.password_hash, clean_pwd)
    
    if not is_valid:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Incorrect old password.")

    target_user.password_hash = hash_password(req.new_password)
    target_user.updated_at = datetime.now(timezone.utc)
    await db.commit()
    await db.refresh(target_user)

    # Immediately push updated user credential to Cloudflare D1
    try:
        from app.db.d1_sync import d1_sync_manager
        asyncio.create_task(asyncio.to_thread(d1_sync_manager.sync_table_to_cloud, "users"))
    except Exception as e:
        logger.warning(f"[AUTH] Could not immediately push password update to D1: {e}")

    return {
        "status": "ok",
        "message": "Password changed successfully in the database! You can now log in or unlock with your new password.",
        "username": target_user.username
    }


class TerminateSessionPayload(BaseModel):
    session_id: Optional[str] = None
    terminate_others: Optional[bool] = False
    username: Optional[str] = None


@router.get("/sessions", status_code=status.HTTP_200_OK)
async def get_active_sessions_endpoint(
    request: Request,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # SEC-B01 FIX: Enforce session authentication; remove untrusted header fallbacks
    target_user = current_user

    # Find raw token to check current session
    auth_header = request.headers.get("Authorization", "")
    raw_token = ""
    if auth_header.startswith("Bearer "):
        raw_token = auth_header[7:].strip()
    if not raw_token:
        raw_token = request.cookies.get(settings.COOKIE_NAME, "")
    
    current_token_hash = hash_session_token(raw_token) if raw_token else None

    now = datetime.now(timezone.utc)
    stmt = (
        select(UserSession)
        .where(
            UserSession.user_id == target_user.id,
            UserSession.expires_at > now,
            ~UserSession.session_token_hash.in_(
                select(TerminatedSession.session_token_hash)
            )
        )
        .order_by(UserSession.created_at.desc())
    )
    result = await db.execute(stmt)
    sessions = result.scalars().all()

    session_list = []
    has_current = False
    for s in sessions:
        is_curr = False
        if current_token_hash and s.session_token_hash == current_token_hash:
            is_curr = True
            has_current = True

        req_headers = request.headers if is_curr else None
        dev_info = parse_session_device_details(s.user_agent or "", headers=req_headers)

        # Upgrade stored UA for the current session if richer metadata is available
        if is_curr:
            enriched_ua = extract_request_user_agent(request)
            if enriched_ua and s.user_agent != enriched_ua:
                try:
                    s.user_agent = enriched_ua
                    await db.commit()
                except Exception as ex:
                    logger.debug(f"Failed to refresh session UA: {ex}")

        loc = "Online"
        if s.ip_address:
            if s.ip_address in ("127.0.0.1", "::1", "localhost"):
                loc = "Local Device"
            else:
                loc = f"IP: {s.ip_address}"

        session_list.append({
            "id": s.id,
            "device_name": dev_info["device_name"],
            "phone_type": dev_info["phone_type"],
            "phone_version": dev_info["phone_version"],
            "os_version": dev_info["os_version"],
            "device_type": dev_info["device_type"],
            "location": loc,
            "ip_address": s.ip_address or "",
            "is_current": is_curr,
            "created_at": s.created_at.isoformat() if s.created_at else "",
            "last_active_at": s.created_at.isoformat() if s.created_at else ""
        })

    if not has_current and session_list:
        session_list[0]["is_current"] = True

    return {
        "status": "ok",
        "sessions": session_list
    }


async def _sync_terminated_to_d1(token_hashes: list, user_id: str):
    try:
        from app.db.d1_client import get_d1_client
        d1 = get_d1_client()
        if d1 and token_hashes:
            stmts = []
            for h in token_hashes:
                stmts.append({
                    "sql": "INSERT OR REPLACE INTO terminated_sessions (session_token_hash) VALUES (?);",
                    "params": [h]
                })
                stmts.append({
                    "sql": "DELETE FROM user_sessions WHERE session_token_hash = ?;",
                    "params": [h]
                })
            stmts.append({
                "sql": "INSERT OR REPLACE INTO terminated_sessions (session_token_hash) VALUES (?);",
                "params": [f"all_before_{user_id}"]
            })
            await d1.batch(stmts)
    except Exception as e:
        logger.warning(f"Failed to sync terminated sessions to D1: {e}")


@router.post("/sessions/terminate", status_code=status.HTTP_200_OK)
async def terminate_session_endpoint(
    req: TerminateSessionPayload,
    request: Request,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # SEC-B01 FIX: Enforce session authentication; lock target user strictly to current user
    target_user = current_user

    auth_header = request.headers.get("Authorization", "")
    raw_token = ""
    if auth_header.startswith("Bearer "):
        raw_token = auth_header[7:].strip()
    if not raw_token:
        raw_token = request.cookies.get(settings.COOKIE_NAME, "")
    current_token_hash = hash_session_token(raw_token) if raw_token else None

    now = datetime.now(timezone.utc)
    terminated_hashes = []

    if req.terminate_others:
        stmt = select(UserSession).where(UserSession.user_id == target_user.id)
        sessions = (await db.execute(stmt)).scalars().all()
        for s in sessions:
            if current_token_hash and s.session_token_hash == current_token_hash:
                continue
            terminated_hashes.append(s.session_token_hash)
            term_obj = TerminatedSession(
                session_token_hash=s.session_token_hash,
                user_id=target_user.id,
                terminated_at=now
            )
            await db.merge(term_obj)
            await db.delete(s)

        all_before_marker = TerminatedSession(
            session_token_hash=f"all_before_{target_user.id}",
            user_id=target_user.id,
            terminated_at=now
        )
        await db.merge(all_before_marker)
        await db.commit()

        # Real-time WebSocket event & forced socket termination
        try:
            from app.core.ws import ws_manager
            termination_event = {
                "type": "session_terminated",
                "reason": "other_sessions_terminated",
                "message": "Your session was logged out from another device.",
                "initiator_token_hash": current_token_hash,
                "terminated_hashes": terminated_hashes,
                "timestamp": now.isoformat()
            }
            await ws_manager.send_personal_event(target_user.id, termination_event)
            if target_user.username:
                await ws_manager.send_personal_event(target_user.username, termination_event)

            await ws_manager.terminate_user_sessions(
                target_user.id,
                current_token_hash=current_token_hash,
                terminated_hashes=terminated_hashes
            )
        except Exception as e:
            logger.warning(f"Error terminating WebSocket sessions: {e}")

        try:
            if getattr(target_user, "fcm_token", None):
                from app.core.push import send_push_notification
                asyncio.create_task(
                    asyncio.to_thread(
                        send_push_notification,
                        target_user.fcm_token,
                        title="Session Terminated",
                        body="Your session was logged out from another device.",
                        notification_type="session_terminated",
                        data={
                            "initiator_token_hash": current_token_hash or "",
                            "terminated_hashes": ",".join(terminated_hashes)
                        }
                    )
                )
        except Exception as ex:
            logger.debug(f"Failed to send session termination push: {ex}")

        asyncio.create_task(_sync_terminated_to_d1(terminated_hashes, target_user.id))

        return {
            "status": "ok",
            "message": "All other sessions have been logged out.",
            "count": len(terminated_hashes)
        }

    if req.session_id:
        stmt = select(UserSession).where(
            UserSession.id == req.session_id,
            UserSession.user_id == target_user.id
        )
        session_to_delete = (await db.execute(stmt)).scalar_one_or_none()
        if session_to_delete:
            th = session_to_delete.session_token_hash
            term_obj = TerminatedSession(
                session_token_hash=th,
                user_id=target_user.id,
                terminated_at=now
            )
            await db.merge(term_obj)
            await db.delete(session_to_delete)
            await db.commit()

            try:
                from app.core.ws import ws_manager
                termination_event = {
                    "type": "session_terminated",
                    "reason": "session_terminated",
                    "message": "Your session was logged out.",
                    "session_id": req.session_id,
                    "session_token_hash": th,
                    "terminated_hashes": [th],
                    "timestamp": now.isoformat()
                }
                await ws_manager.send_personal_event(target_user.id, termination_event)
                if target_user.username:
                    await ws_manager.send_personal_event(target_user.username, termination_event)

                await ws_manager.terminate_user_sessions(
                    target_user.id,
                    terminated_hashes=[th]
                )
            except Exception as e:
                logger.warning(f"Error terminating WebSocket session: {e}")

            try:
                if getattr(target_user, "fcm_token", None):
                    from app.core.push import send_push_notification
                    asyncio.create_task(
                        asyncio.to_thread(
                            send_push_notification,
                            target_user.fcm_token,
                            title="Session Terminated",
                            body="Your session was logged out from another device.",
                            notification_type="session_terminated",
                            data={
                                "session_id": req.session_id or "",
                                "session_token_hash": th,
                                "terminated_hashes": th
                            }
                        )
                    )
            except Exception as ex:
                logger.debug(f"Failed to send session termination push: {ex}")

            asyncio.create_task(_sync_terminated_to_d1([th], target_user.id))

            return {"status": "ok", "message": "Session terminated successfully.", "count": 1}
        else:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Session not found.")

    raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Specify session_id or terminate_others.")


@router.post("/sessions/terminate-others", status_code=status.HTTP_200_OK)
async def terminate_others_session_endpoint(
    request: Request,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    return await terminate_session_endpoint(TerminateSessionPayload(terminate_others=True), request, db, current_user)


@router.get("/e2e/secret", status_code=status.HTTP_200_OK)
async def get_e2e_secret_endpoint(
    peer: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user)
):
    """
    Returns the user's deterministic derivation salt or shared conversation secret
    for WebCrypto AES-GCM conversation keys.
    """
    import hashlib
    if peer:
        p_clean = peer.strip().lower().replace("@", "")
        u_clean = current_user.username.strip().lower().replace("@", "")
        pair = ":".join(sorted([u_clean, p_clean]))
        salt = f"connecto_e2e_conv_{pair}_{settings.SECRET_KEY}"
        secret = hashlib.sha256(salt.encode()).hexdigest()
        return {"status": "ok", "e2e_secret": secret, "pair": pair}
    salt = f"connecto_e2e_{current_user.id}_{current_user.created_at}_{settings.SECRET_KEY}"
    secret = hashlib.sha256(salt.encode()).hexdigest()
    return {"status": "ok", "e2e_secret": secret}




