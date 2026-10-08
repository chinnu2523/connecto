import re
import secrets
import logging
logger = logging.getLogger("connecto.auth")
from datetime import datetime, timedelta, timezone
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Request, Response, status
from sqlalchemy import select, or_, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user
from app.core.config import settings
from app.core.security import hash_password, verify_password, generate_session_token, hash_session_token
from app.core.otp import send_unified_otp, verify_unified_otp, clean_phone_number, mask_phone_number, mask_email
from app.db.models.user import User, UserSession, OTPVerification
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
        "masked_destination": res.get("masked_destination", mask_email(clean_e)),
        "dev_otp": res.get("dev_otp")
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
        "masked_destination": res.get("masked_destination", mask_email(clean_e)),
        "dev_otp": res.get("dev_otp")
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
        
    # Enforce test account rule: only 'test_user', 'chinnu', and 'vivek' are permitted test accounts
    test_patterns = [r"^test", r"^audit", r"^shinobi_[ab]", r"^antiflood", r"^mock", r"^diag_user"]
    if any(re.search(pat, clean_u) for pat in test_patterns) and clean_u not in ("chinnu", "vivek", "test_user"):
        return {
            "status": "error",
            "available": False,
            "valid": False,
            "username": clean_u,
            "message": "Registration of additional test accounts is disabled. Only designated test accounts ('test_user', 'chinnu', and 'vivek') are permitted."
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

    # Enforce test accounts policy: only 'vivek', 'srinu', and 'devi' are permitted test accounts
    test_patterns = [r"^test", r"^audit", r"^shinobi_[ab]", r"^antiflood", r"^mock", r"^diag_user"]
    is_test_username = any(re.search(pat, clean_username) for pat in test_patterns)
    is_test_email = clean_email.endswith("@connecto.test") or clean_email.endswith("@example.com") or any(re.search(pat, clean_email) for pat in test_patterns)
    if (is_test_username or is_test_email) and clean_username not in ("vivek", "srinu", "devi"):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Registration of additional test accounts is disabled. Only the designated accounts ('vivek', 'srinu', 'devi') are authorized."
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
        user_agent=request.headers.get("User-Agent"),
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

    # Strict password verification — ONLY Argon2id hash match is accepted.
    # Whitespace trimming handles mobile keyboard trailing-space edge case only.
    # NO fallback passwords, NO auto-rehash, NO dev password lists.
    pwd_clean = (login_data.password or "").strip()
    is_valid_pwd = verify_password(user.password_hash, login_data.password) if user else False
    if not is_valid_pwd and user and pwd_clean != login_data.password:
        is_valid_pwd = verify_password(user.password_hash, pwd_clean)

    if not user or not is_valid_pwd:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username/email or password."
        )

    # Sole admin policy: ONLY chinnu14754x is admin, all others are members
    is_adm = (user.username.lower() == "chinnu14754x")
    if bool(user.is_admin) != is_adm:
        user.is_admin = is_adm
        await db.commit()
        await db.refresh(user)

    # Check if Two-Factor Authentication (2FA) is enabled for this account
    if getattr(user, "two_factor_enabled", False):
        two_fa_method = (getattr(user, "two_factor_method", "sms") or "sms").lower()
        destination = user.phone_number if two_fa_method == "sms" else user.email

        # Fallback to email if sms selected but no phone attached
        if two_fa_method == "sms" and not destination:
            two_fa_method = "email"
            destination = user.email

        if not destination:
            destination = user.email or user.username

        otp_result = await send_unified_otp(
            db=db,
            identifier=destination,
            method=two_fa_method,
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
            two_factor_method=two_fa_method,
            requires_2fa=True,
            masked_destination=otp_result["masked_destination"],
            is_admin=is_adm,
            username_changed=user.username_changed,
            created_at=user.created_at,
            token=None,
            dev_otp=otp_result.get("dev_otp")
        )

    # Standard Login (No 2FA Required)
    raw_token = generate_session_token()
    token_hash = hash_session_token(raw_token)
    expires = datetime.now(timezone.utc) + timedelta(days=settings.SESSION_EXPIRE_DAYS)

    session = UserSession(
        user_id=user.id,
        session_token_hash=token_hash,
        ip_address=request.client.host if request.client else None,
        user_agent=request.headers.get("User-Agent"),
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

    destination = user.phone_number if getattr(user, "two_factor_method", "sms") == "sms" else user.email
    if not destination:
        destination = user.email or user.username

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
        user_agent=request.headers.get("User-Agent"),
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
    if len(req.new_password) < 4:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="New password must be at least 4 characters long."
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
        user_agent=request.headers.get("User-Agent"),
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
    method = (req.method or "sms").lower()
    phone = req.phone_number.strip() if req.phone_number else current_user.phone_number

    if method == "sms" and not phone:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A valid phone number is required to enable SMS 2FA."
        )

    target = clean_phone_number(phone) if method == "sms" else current_user.email.lower()

    return await send_unified_otp(
        db=db,
        identifier=target,
        method=method,
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
    Verifies OTP and enables Two-Factor Authentication for the account.
    """
    method = (req.method or "sms").lower()
    phone = req.phone_number.strip() if req.phone_number else current_user.phone_number
    target = clean_phone_number(phone) if method == "sms" else current_user.email.lower()

    await verify_unified_otp(
        db=db,
        identifier=target,
        otp_code=req.otp_code,
        purpose="two_factor_setup"
    )

    current_user.two_factor_enabled = True
    current_user.two_factor_method = method
    if phone:
        current_user.phone_number = clean_phone_number(phone)

    await db.commit()
    await db.refresh(current_user)

    return {
        "status": "ok",
        "two_factor_enabled": True,
        "two_factor_method": current_user.two_factor_method,
        "phone_number": current_user.phone_number,
        "message": f"Two-Factor Authentication successfully activated via {method.upper()}!"
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
        current_user.two_factor_enabled = False
        await db.commit()
        await db.refresh(current_user)
        return {
            "status": "ok",
            "two_factor_enabled": False,
            "two_factor_method": current_user.two_factor_method,
            "message": "Two-Factor Authentication disabled."
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


@router.post("/httpsms-webhook", status_code=status.HTTP_200_OK)
async def httpsms_webhook(request: Request, db: AsyncSession = Depends(get_db)):
    """
    Receives incoming webhook events from httpSMS (https://github.com/NdoleStudio/httpsms.git)
    Events handled:
    - message.phone.received: An incoming SMS received on user's phone / SIM
    - message.send.delivered / message.send.failed
    """
    try:
        payload = await request.json()
    except Exception:
        return {"status": "error", "message": "Invalid JSON payload"}

    event_type = payload.get("event")
    data = payload.get("data", {})

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
    new_password: str

class LegacyVerify2FaRequest(BaseModel):
    username: str
    otp: str

class LegacyResendOtpRequest(BaseModel):
    username: str

@router.post("/forgot-password", status_code=status.HTTP_200_OK)
async def legacy_forgot_password(req: LegacyForgotPwRequest, db: AsyncSession = Depends(get_db)):
    method = "email" if "@" in req.identifier else "sms"
    return await forgot_password_request_otp(ForgotPasswordRequest(identifier=req.identifier, method=method), db=db)

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
    method = (getattr(user, "two_factor_method", "email") or "email").lower()
    dest = user.phone_number if method == "sms" and user.phone_number else user.email
    return await send_unified_otp(db=db, identifier=dest, method=method, purpose="two_factor_login", user_id=user.id)

