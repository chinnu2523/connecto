import hashlib
import hmac
import logging
import re
import secrets
import smtplib
from datetime import datetime, timedelta, timezone
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from typing import Optional, Dict, Any

import httpx
from fastapi import HTTPException, status
from sqlalchemy import select, and_, or_, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.db.models.user import User, OTPVerification

logger = logging.getLogger("connecto.otp")
logger.setLevel(logging.INFO)
if not logger.handlers:
    import sys
    _h = logging.StreamHandler(sys.stdout)
    _h.setLevel(logging.INFO)
    _h.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] [connecto.otp] %(message)s"))
    logger.addHandler(_h)


# -------------------------------------------------------------------------
# Formatting & Masking Helpers
# -------------------------------------------------------------------------

def clean_phone_number(phone: str) -> str:
    """Cleans phone numbers into E.164 compatible format."""
    cleaned = re.sub(r"[^\d+]", "", phone.strip())
    if len(cleaned) == 10 and not cleaned.startswith("+"):
        cleaned = f"+91{cleaned}"
    elif cleaned and not cleaned.startswith("+"):
        cleaned = f"+{cleaned}"
    return cleaned

def mask_phone_number(phone: str) -> str:
    """Masks phone number for safe public UI display (e.g. +91******1234)."""
    clean = clean_phone_number(phone)
    if len(clean) <= 5:
        return clean
    prefix = clean[:3]
    suffix = clean[-4:]
    masked_len = max(len(clean) - 7, 3)
    return f"{prefix}{'*' * masked_len}{suffix}"

def mask_email(email: str) -> str:
    """Masks email address for safe public UI display (e.g. j***@example.com)."""
    clean = email.strip().lower()
    if "@" not in clean:
        return clean
    user_part, domain_part = clean.split("@", 1)
    if len(user_part) <= 2:
        masked_user = user_part[:1] + "*"
    else:
        masked_user = user_part[:2] + "*" * (len(user_part) - 2)
    return f"{masked_user}@{domain_part}"

# -------------------------------------------------------------------------
# Cryptographic Hashing
# -------------------------------------------------------------------------

def generate_numeric_otp(length: int = 6) -> str:
    """Generates a cryptographically secure numeric OTP."""
    return "".join(secrets.choice("0123456789") for _ in range(length))

def hash_otp(otp_code: str) -> str:
    """
    Computes HMAC-SHA256 hash of the OTP using server's SECRET_KEY.
    Prevents plaintext storage in DB and eliminates rainbow table attacks.
    """
    return hmac.new(
        settings.SECRET_KEY.encode("utf-8"),
        otp_code.strip().encode("utf-8"),
        hashlib.sha256
    ).hexdigest()

def verify_otp_hash(candidate_otp: str, stored_hash: str) -> bool:
    """Constant-time verification of candidate OTP against stored HMAC-SHA256."""
    candidate_hash = hash_otp(candidate_otp)
    return hmac.compare_digest(candidate_hash, stored_hash)

# -------------------------------------------------------------------------
# Real Email Delivery Integration (Resend + SMTP Fallback)
# -------------------------------------------------------------------------

async def deliver_email_otp(to_email: str, otp_code: str, purpose: str, raw_purpose: str = "general") -> Dict[str, Any]:
    """
    Delivers OTP email using Resend (Modern REST API) or SMTP.
    NEVER logs the actual OTP code.
    """
    clean_to = to_email.strip().lower()
    masked = mask_email(clean_to)
    logger.info(f"[OTP Email] Initiating delivery for {purpose} (raw: {raw_purpose}) to {masked}")

    # Distinct subject and styling based on exact security purpose to prevent Gmail conversation threading
    raw_p = raw_purpose.lower()
    if raw_p == "two_factor_setup":
        subject = f"[Connecto 2FA Setup] Your Two-Factor Activation Code: {otp_code}"
        badge_title = "TWO-FACTOR SECURITY SETUP"
        badge_bg = "#6c5ce7"
        border_color = "#a855f7"
        text_color = "#d8b4fe"
        guidance_text = "This verification code is strictly for <b>activating Two-Factor Authentication (2FA)</b> in your Connecto Account Settings. <b>Do NOT use this code for password reset or sign-in.</b>"
    elif raw_p == "two_factor_login":
        subject = f"[Connecto 2FA Sign-In] Your Two-Factor Sign-In Code: {otp_code}"
        badge_title = "TWO-FACTOR SIGN-IN VERIFICATION"
        badge_bg = "#1f6feb"
        border_color = "#388bfd"
        text_color = "#93c5fd"
        guidance_text = "This verification code is strictly for <b>signing into your Connecto account</b>. <b>Do NOT use this code for password reset.</b>"
    elif raw_p == "forgot_password":
        subject = f"[Connecto Password Reset] Your Account Recovery Code: {otp_code}"
        badge_title = "PASSWORD RESET RECOVERY"
        badge_bg = "#d29922"
        border_color = "#e3b341"
        text_color = "#fde047"
        guidance_text = "This verification code is strictly for <b>resetting your forgotten password</b>. <b>Do NOT use this code for 2FA activation or sign-in.</b>"
    elif raw_p in ("email_verification", "signup_verification"):
        subject = f"[Connecto Email Verification] Your 6-Digit Verification Code: {otp_code}"
        badge_title = "ACCOUNT EMAIL VERIFICATION"
        badge_bg = "#059669"
        border_color = "#10b981"
        text_color = "#6ee7b7"
        guidance_text = "This verification code is to <b>verify your email address</b> on Connecto. Enter this 6-digit code in the app to complete account verification."
    else:
        subject = f"[Connecto Security] Your {purpose} Verification Code: {otp_code}"
        badge_title = f"{purpose.upper()} VERIFICATION"
        badge_bg = "#238636"
        border_color = "#2ea043"
        text_color = "#86efac"
        guidance_text = f"Use this verification code to complete {purpose}."

    body_text = f"""Hello,

Your verification code for {purpose} is: {otp_code}

IMPORTANT: {guidance_text.replace('<b>', '').replace('</b>', '')}

This code will expire in 10 minutes. If you did not request this verification code, please ignore this email or check your account security.

Best regards,
Connecto Security Team
https://connecto.fun
"""

    body_html = f"""<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <style>
    body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #000000; color: #f0f6fc; margin: 0; padding: 20px; }}
    .card {{ max-width: 480px; margin: auto; background: #0c0d12; border-radius: 16px; border: 1px solid #21262d; padding: 32px; text-align: center; }}
    .badge {{ display: inline-block; background: {badge_bg}; color: white; padding: 6px 14px; border-radius: 20px; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; }}
    .title {{ margin: 18px 0 8px; color: #ffffff; font-size: 22px; font-weight: 700; }}
    .sub {{ color: #8b949e; font-size: 14px; margin-bottom: 20px; line-height: 1.5; }}
    .guidance {{ background: rgba(255,255,255,0.04); border-left: 3px solid {border_color}; padding: 12px 14px; border-radius: 6px; text-align: left; font-size: 13px; color: {text_color}; margin-bottom: 20px; line-height: 1.4; }}
    .otp-box {{ font-size: 34px; font-weight: 900; letter-spacing: 12px; color: #ffffff; background: #161b22; padding: 18px 24px; border-radius: 14px; border: 1px dashed {border_color}; margin: 20px 0; font-family: 'Courier New', Courier, monospace; }}
    .warning {{ font-size: 12px; color: #8b949e; margin-top: 16px; }}
    .footer {{ font-size: 11px; color: #484f58; margin-top: 28px; border-top: 1px solid #161b22; padding-top: 16px; }}
  </style>
</head>
<body>
  <div class="card">
    <div class="badge">{badge_title}</div>
    <div class="title">{badge_title}</div>
    <div class="sub">Use the code below to complete your authentication.</div>
    <div class="guidance">{guidance_text}</div>
    <div class="otp-box">{otp_code}</div>
    <div class="warning">Valid for 10 minutes. Never share this code with anyone.</div>
    <div class="footer">Connecto Platform &bull; Automated Security Service &bull; https://connecto.fun</div>
  </div>
</body>
</html>"""

    # 1. Primary: Resend API (Direct Delivery to Target Recipient)
    if settings.RESEND_API_KEY:
        try:
            from_sender = settings.RESEND_FROM_EMAIL or "Connecto Security <security@connecto.fun>"
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(
                    "https://api.resend.com/emails",
                    headers={
                        "Authorization": f"Bearer {settings.RESEND_API_KEY.strip()}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "from": from_sender,
                        "to": [clean_to],
                        "subject": subject,
                        "html": body_html,
                        "text": body_text,
                        "headers": {
                            "X-Entity-Ref-ID": f"connecto-{raw_p}-{secrets.token_hex(6)}"
                        }
                    }
                )
                if res.status_code in (200, 201, 202):
                    logger.info(f"[OTP Email] Successfully delivered via Resend API directly to {masked} (subject: {subject})")
                    return {"status": "success", "gateway": "resend", "destination": masked}
                else:
                    logger.error(f"[OTP Email] Resend API error ({res.status_code}) delivering to {clean_to}: {res.text}")
        except Exception as e:
            logger.error(f"[OTP Email] Resend delivery exception for {clean_to}: {e}")

    # 2. Secondary: SMTP Gateway
    if settings.SMTP_HOST and settings.SMTP_USER and settings.SMTP_PASSWORD:
        try:
            msg = MIMEMultipart("alternative")
            msg["Subject"] = subject
            msg["From"] = settings.EMAILS_FROM or "security@connecto.fun"
            msg["To"] = clean_to
            msg.attach(MIMEText(body_text, "plain"))
            msg.attach(MIMEText(body_html, "html"))

            with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10) as server:
                server.starttls()
                server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
                server.sendmail(msg["From"], [clean_to], msg.as_string())

            logger.info(f"[OTP Email] Successfully delivered via SMTP to {masked}")
            return {"status": "success", "gateway": "smtp", "destination": masked}
        except Exception as e:
            logger.error(f"[OTP Email] SMTP delivery exception: {e}")

    logger.warning(f"[OTP Email] No live email gateway configured or delivery failed for {masked}")
    return {"status": "failed", "gateway": "unconfigured", "destination": masked, "error": "Email delivery failed: provider rejected or unconfigured"}

# -------------------------------------------------------------------------
# Real SMS Delivery Integration (MSG91 + Twilio + httpSMS Fallback)
# -------------------------------------------------------------------------

async def deliver_sms_otp(to_phone: str, otp_code: str, purpose: str) -> Dict[str, Any]:
    """
    Delivers OTP SMS using MSG91 (DLT-compliant Indian Gateway) or Twilio / httpSMS.
    NEVER logs the actual OTP code.
    """
    clean_to = clean_phone_number(to_phone)
    masked = mask_phone_number(clean_to)
    logger.info(f"[OTP SMS] Initiating delivery for {purpose} to {masked}")
    content = f"Your Connecto verification code for {purpose} is {otp_code}. Valid for 10 minutes. Do not share this OTP."

    # 1. Primary: MSG91 API (Recommended for India)
    if settings.MSG91_AUTH_KEY:
        try:
            msg91_mobile = clean_to.lstrip("+")
            url = "https://control.msg91.com/api/v5/otp"
            params = {
                "template_id": settings.MSG91_TEMPLATE_ID or "",
                "mobile": msg91_mobile,
                "authkey": settings.MSG91_AUTH_KEY.strip(),
                "otp": otp_code,
                "sender": settings.MSG91_SENDER_ID or "CONNTO",
                "otp_expiry": 10
            }
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(url, params=params)
                if res.status_code in (200, 201, 202):
                    logger.info(f"[OTP SMS] Successfully delivered via MSG91 API to {masked}")
                    return {"status": "success", "gateway": "msg91", "destination": masked}
                else:
                    logger.error(f"[OTP SMS] MSG91 API error ({res.status_code}): {res.text}")
        except Exception as e:
            logger.error(f"[OTP SMS] MSG91 delivery exception: {e}")

    # 2. Secondary: Twilio Gateway (International)
    if settings.TWILIO_ACCOUNT_SID and settings.TWILIO_AUTH_TOKEN and settings.TWILIO_FROM_NUMBER:
        try:
            url = f"https://api.twilio.com/2010-04-01/Accounts/{settings.TWILIO_ACCOUNT_SID.strip()}/Messages.json"
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(
                    url,
                    auth=(settings.TWILIO_ACCOUNT_SID.strip(), settings.TWILIO_AUTH_TOKEN.strip()),
                    data={
                        "To": clean_to,
                        "From": settings.TWILIO_FROM_NUMBER.strip(),
                        "Body": content
                    }
                )
                if res.status_code in (200, 201, 202):
                    logger.info(f"[OTP SMS] Successfully delivered via Twilio to {masked}")
                    return {"status": "success", "gateway": "twilio", "destination": masked}
                else:
                    logger.error(f"[OTP SMS] Twilio API error ({res.status_code}): {res.text}")
        except Exception as e:
            logger.error(f"[OTP SMS] Twilio delivery exception: {e}")

    # 3. Tertiary: httpSMS Gateway
    if settings.HTTPSMS_API_KEY:
        try:
            url = f"{settings.HTTPSMS_BASE_URL.rstrip('/')}/v1/messages/send"
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(
                    url,
                    headers={
                        "x-api-key": settings.HTTPSMS_API_KEY.strip(),
                        "Content-Type": "application/json"
                    },
                    json={
                        "content": content,
                        "from": clean_phone_number(settings.HTTPSMS_FROM_NUMBER or "+18005550199"),
                        "to": clean_to
                    }
                )
                if res.status_code in (200, 201, 202):
                    logger.info(f"[OTP SMS] Successfully delivered via httpSMS to {masked}")
                    return {"status": "success", "gateway": "httpsms", "destination": masked}
        except Exception as e:
            logger.error(f"[OTP SMS] httpSMS delivery exception: {e}")

    # 4. Quaternary: Fast2SMS Gateway (India)
    if settings.FAST2SMS_API_KEY:
        try:
            raw_digits = re.sub(r"[^\d]", "", clean_to)
            if raw_digits.startswith("91") and len(raw_digits) == 12:
                raw_digits = raw_digits[2:]
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(
                    "https://www.fast2sms.com/dev/bulkV2",
                    headers={"authorization": settings.FAST2SMS_API_KEY.strip()},
                    data={
                        "variables_values": otp_code,
                        "route": "otp",
                        "numbers": raw_digits
                    }
                )
                if res.status_code == 200 and res.json().get("return") is True:
                    logger.info(f"[OTP SMS] Successfully delivered via Fast2SMS to {masked}")
                    return {"status": "success", "gateway": "fast2sms", "destination": masked}
                else:
                    logger.error(f"[OTP SMS] Fast2SMS error: {res.text}")
        except Exception as e:
            logger.error(f"[OTP SMS] Fast2SMS delivery exception: {e}")

    logger.warning(f"[OTP SMS] No live SMS gateway configured or delivery failed for {masked}")
    return {"status": "failed", "gateway": "unconfigured", "destination": masked, "error": "No live SMS gateway configured on server"}

# -------------------------------------------------------------------------
# Unified Send-OTP Function (Shared by 2FA Login, Forgot Password, 2FA Setup)
# -------------------------------------------------------------------------

async def send_unified_otp(
    db: AsyncSession,
    identifier: str,
    method: str,
    purpose: str,
    user_id: Optional[str] = None
) -> Dict[str, Any]:
    """
    Unified, single function that handles OTP generation, rate limiting, hashing,
    database storage, and real carrier delivery for both 2FA and Forgot Password.
    """
    clean_method = method.strip().lower()
    if clean_method not in ("email", "sms"):
        clean_method = "email" if "@" in identifier else "sms"

    clean_id = clean_phone_number(identifier) if clean_method == "sms" else identifier.strip().lower()
    masked_dest = mask_phone_number(clean_id) if clean_method == "sms" else mask_email(clean_id)
    now = datetime.now(timezone.utc)

    # 1. Rate Limiting: Check 60-second cooldown
    cooldown_cutoff = now - timedelta(seconds=60)
    recent_stmt = (
        select(OTPVerification)
        .where(
            OTPVerification.identifier == clean_id,
            OTPVerification.purpose == purpose,
            OTPVerification.created_at > cooldown_cutoff
        )
        .order_by(OTPVerification.created_at.desc())
        .limit(1)
    )
    recent_otp = (await db.execute(recent_stmt)).scalar_one_or_none()
    if recent_otp:
        elapsed = (now - recent_otp.created_at.replace(tzinfo=timezone.utc)).total_seconds()
        remaining = max(1, int(60 - elapsed))
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=f"Please wait {remaining} seconds before requesting a new verification code."
        )

    # 2. Rate Limiting: Max 5 requests per hour
    hour_cutoff = now - timedelta(hours=1)
    hour_stmt = (
        select(func.count(OTPVerification.id))
        .where(
            OTPVerification.identifier == clean_id,
            OTPVerification.created_at > hour_cutoff
        )
    )
    hourly_count = (await db.execute(hour_stmt)).scalar() or 0
    if hourly_count >= 5:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many verification requests. Please try again in an hour."
        )

    # 2.5 Spend Cap: Enforce Global Daily Quota to prevent toll fraud and runaway billing
    day_cutoff = now - timedelta(days=1)
    global_daily_stmt = (
        select(func.count(OTPVerification.id))
        .where(
            OTPVerification.method == clean_method,
            OTPVerification.created_at > day_cutoff
        )
    )
    daily_count = (await db.execute(global_daily_stmt)).scalar() or 0
    max_daily = getattr(settings, 'MAX_DAILY_EMAIL_DISPATCH', 100) if clean_method == 'email' else getattr(settings, 'MAX_DAILY_SMS_DISPATCH', 50)
    if daily_count >= max_daily:
        logger.error(f"[SPEND_CAP_TRIGGERED] Global daily quota of {max_daily} reached ({daily_count} sent).")
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Daily verification dispatch limit reached. Please contact support or try again tomorrow."
        )

    # 3. Invalidate previous pending OTPs for this identifier and purpose
    old_stmt = (
        select(OTPVerification)
        .where(
            OTPVerification.identifier == clean_id,
            OTPVerification.purpose == purpose,
            OTPVerification.is_verified == False
        )
    )
    old_otps = (await db.execute(old_stmt)).scalars().all()
    for old in old_otps:
        await db.delete(old)

    # 4. Generate & Hash OTP
    plain_otp = generate_numeric_otp(6)
    hashed_otp = hash_otp(plain_otp)
    expires_at = now + timedelta(minutes=10)

    # 5. Persist Hashed OTP Record
    otp_record = OTPVerification(
        user_id=user_id,
        identifier=clean_id,
        otp_code=hashed_otp,
        purpose=purpose,
        method=clean_method,
        attempts=0,
        expires_at=expires_at,
        is_verified=False
    )
    db.add(otp_record)
    await db.commit()

    # 6. Real Carrier Delivery
    friendly_purpose = {
        "two_factor_login": "Two-Factor Sign In",
        "forgot_password": "Password Recovery",
        "two_factor_setup": "Two-Factor Security Setup",
        "email_verification": "Email Verification",
        "signup_verification": "Email Verification"
    }.get(purpose, "Verification")

    delivery_success = False
    delivery_note = None

    if clean_method == "email":
        email_res = await deliver_email_otp(clean_id, plain_otp, friendly_purpose, raw_purpose=purpose)
        if email_res.get("status") == "success":
            delivery_success = True
            delivery_note = f"Verification code sent to {masked_dest}"
        else:
            logger.error(f"[OTP Dispatch Failed] Email delivery failed: {email_res}")
    else:
        sms_res = await deliver_sms_otp(clean_id, plain_otp, friendly_purpose)
        if sms_res.get("status") == "success":
            delivery_success = True
            delivery_note = f"Verification code sent to {masked_dest}"
        else:
            logger.warning(f"[OTP SMS Failed] {sms_res.get('error')}. Checking for backup email delivery...")

        # Dual dispatch or fallback to profile email
        try:
            user_rec = None
            if user_id:
                user_rec = (await db.execute(select(User).where(User.id == user_id))).scalar_one_or_none()
            else:
                user_rec = (await db.execute(select(User).where(or_(User.phone_number == clean_id, func.lower(User.username) == clean_id, func.lower(User.email) == clean_id)))).scalar_one_or_none()
            if user_rec and user_rec.email:
                fallback_email_res = await deliver_email_otp(user_rec.email, plain_otp, f"{friendly_purpose} (SMS Fallback)", raw_purpose=purpose)
                if fallback_email_res.get("status") == "success":
                    delivery_success = True
                    delivery_note = f"Verification code delivered to backup email {mask_email(user_rec.email)}"
                    logger.info(f"[OTP Dual Dispatch] Backup email delivery succeeded to {mask_email(user_rec.email)}")
        except Exception as sync_err:
            logger.warning(f"[OTP Dual Dispatch] Backup email delivery skipped: {sync_err}")

    if not delivery_success:
        if getattr(settings, "ENABLE_DEV_OTP_FALLBACK", False):
            resp_data = {
                "status": "ok",
                "message": f"Carrier unavailable; dev OTP issued for testing: {plain_otp}",
                "method": clean_method,
                "masked_destination": masked_dest,
                "identifier": clean_id,
                "dev_otp": plain_otp
            }
            return resp_data

        err_msg = (
            f"Unable to send SMS: No live SMS gateway configured on the server. Please choose Email verification."
            if clean_method == "sms"
            else f"Unable to deliver verification email. Please check your address or contact administrator."
        )
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=err_msg
        )

    resp_data = {
        "status": "ok",
        "message": delivery_note or f"Verification code sent to {masked_dest}",
        "method": clean_method,
        "masked_destination": masked_dest,
        "identifier": clean_id
    }
    if getattr(settings, "ENABLE_DEV_OTP_FALLBACK", False):
        resp_data["dev_otp"] = plain_otp
    return resp_data

# -------------------------------------------------------------------------
# Unified Verify-OTP Function
# -------------------------------------------------------------------------

async def verify_unified_otp(
    db: AsyncSession,
    identifier: str,
    otp_code: str,
    purpose: str,
    user_id: Optional[str] = None,
    max_attempts: int = 5
) -> OTPVerification:
    """
    Verifies an OTP code against stored HMAC-SHA256 hash.
    Enforces maximum attempt rate-limiting (anti-brute-force) and expiration.
    Includes smart cross-purpose detection to prevent user confusion between
    2FA and Password Reset codes.
    """
    clean_code = otp_code.strip()
    if not clean_code or not clean_code.isdigit():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid verification code format. Code must be 6 digits."
        )

    raw_id = identifier.strip().lower()
    clean_digits = clean_phone_number(raw_id)
    now = datetime.now(timezone.utc)

    conditions = [
        OTPVerification.identifier == raw_id,
        OTPVerification.identifier == clean_digits
    ]

    user_stmt = select(User).where(
        or_(
            func.lower(User.username) == raw_id,
            func.lower(User.email) == raw_id,
            User.phone_number == raw_id,
            User.phone_number == clean_digits
        )
    )
    user = (await db.execute(user_stmt)).scalars().first()
    if user:
        if user.email:
            conditions.append(OTPVerification.identifier == user.email.lower())
        if user.phone_number:
            conditions.append(OTPVerification.identifier == clean_phone_number(user.phone_number))
            conditions.append(OTPVerification.identifier == user.phone_number)
        conditions.append(OTPVerification.user_id == user.id)

    purpose_filter = (
        OTPVerification.purpose.in_(["two_factor_login", "two_factor_setup"])
        if purpose in ("two_factor_login", "two_factor_setup")
        else OTPVerification.purpose == purpose
    )

    stmt = (
        select(OTPVerification)
        .where(
            or_(*conditions),
            purpose_filter,
            OTPVerification.is_verified == False
        )
        .order_by(OTPVerification.created_at.desc())
    )
    otp_record = (await db.execute(stmt)).scalars().first()

    async def check_cross_purpose_error(cand_code: str) -> None:
        """Checks if the entered code belongs to another active OTP purpose."""
        alt_stmt = (
            select(OTPVerification)
            .where(
                or_(*conditions),
                OTPVerification.is_verified == False
            )
            .order_by(OTPVerification.created_at.desc())
        )
        alt_records = (await db.execute(alt_stmt)).scalars().all()
        for alt in alt_records:
            if verify_otp_hash(cand_code, alt.otp_code):
                if alt.purpose == "forgot_password" and purpose in ("two_factor_setup", "two_factor_login"):
                    raise HTTPException(
                        status_code=status.HTTP_400_BAD_REQUEST,
                        detail="The code you entered is from a Password Reset email, not 2FA. Please use the code from the email titled '[Connecto 2FA Setup]' or request a new one."
                    )
                elif alt.purpose in ("two_factor_setup", "two_factor_login") and purpose == "forgot_password":
                    raise HTTPException(
                        status_code=status.HTTP_400_BAD_REQUEST,
                        detail="The code you entered is from a Two-Factor Authentication email, not Password Reset. Please use the code from the email titled '[Connecto Password Reset]'."
                    )

    if not otp_record:
        await check_cross_purpose_error(clean_code)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired verification code. Please request a new one."
        )

    # Check expiration
    expires = otp_record.expires_at.replace(tzinfo=timezone.utc) if otp_record.expires_at.tzinfo is None else otp_record.expires_at
    if expires < now:
        await db.delete(otp_record)
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Verification code has expired. Please request a new one."
        )

    # Anti-Brute-Force: Rate limit failed attempts
    if (otp_record.attempts or 0) >= max_attempts:
        await db.delete(otp_record)
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many failed verification attempts. Code has been revoked for security. Please request a new code."
        )

    # Verify HMAC-SHA256 Hash
    is_valid = verify_otp_hash(clean_code, otp_record.otp_code)

    if not is_valid:
        await check_cross_purpose_error(clean_code)
        otp_record.attempts = (otp_record.attempts or 0) + 1
        await db.commit()
        remaining = max_attempts - otp_record.attempts
        if remaining <= 0:
            await db.delete(otp_record)
            await db.commit()
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Too many failed verification attempts. Code has been revoked. Please request a new code."
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid verification code. {remaining} attempt{'s' if remaining != 1 else ''} remaining."
        )

    # Validated successfully
    otp_record.is_verified = True
    await db.commit()
    return otp_record
