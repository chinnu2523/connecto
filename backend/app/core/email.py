import logging
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from typing import Dict, Any
from app.core.config import settings

logger = logging.getLogger("connecto.email")

def mask_email(email: str) -> str:
    """Masks email address for UI display (e.g. j***@example.com)."""
    clean = email.strip().lower()
    if "@" not in clean:
        return clean
    user_part, domain_part = clean.split("@", 1)
    if len(user_part) <= 2:
        masked_user = user_part[:1] + "*"
    else:
        masked_user = user_part[:2] + "*" * (len(user_part) - 2)
    return f"{masked_user}@{domain_part}"

async def send_email_otp(
    to_email: str,
    otp_code: str,
    purpose: str = "Password Reset"
) -> Dict[str, Any]:
    """
    Sends an OTP verification email to the user.
    Uses configured SMTP credentials if provided, otherwise logs and simulates for development.
    """
    clean_to = to_email.strip().lower()
    logger.info(f"[Email OTP] Preparing {purpose} OTP for {mask_email(clean_to)}: {otp_code}")

    subject = f"Connecto Security: Your {purpose} Verification Code is {otp_code}"
    body_text = f"""Hello Shinobi,

Your verification code for {purpose} is:

    {otp_code}

This code will expire in 10 minutes. If you did not request this verification code, please ignore this email or check your account security.

Best regards,
The Connecto Security Team
https://connecto.fun
"""

    body_html = f"""
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0d1117; color: #f0f6fc; margin: 0; padding: 20px; }}
        .card {{ max-width: 500px; margin: auto; background: #161b22; border-radius: 16px; border: 1px solid #30363d; padding: 30px; text-align: center; }}
        .badge {{ display: inline-block; background: #238636; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 15px; }}
        .otp-box {{ font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #58a6ff; background: #0d1117; padding: 16px; border-radius: 12px; border: 1px dashed #58a6ff; margin: 20px 0; }}
        .footer {{ font-size: 12px; color: #8b949e; margin-top: 25px; }}
      </style>
    </head>
    <body>
      <div class="card">
        <div class="badge">CONNECTO SECURITY</div>
        <h2 style="margin: 0; color: #f0f6fc;">{purpose} Verification</h2>
        <p style="color: #8b949e; font-size: 14px;">Use the code below to complete your verification.</p>
        <div class="otp-box">{otp_code}</div>
        <p style="color: #8b949e; font-size: 13px;">This code expires in 10 minutes. Do not share this code with anyone.</p>
        <div class="footer">Connecto Gaming & Community Platform &bull; https://connecto.fun</div>
      </div>
    </body>
    </html>
    """

    if settings.SMTP_HOST and settings.SMTP_USER and settings.SMTP_PASSWORD:
        try:
            msg = MIMEMultipart("alternative")
            msg["Subject"] = subject
            msg["From"] = settings.EMAILS_FROM
            msg["To"] = clean_to

            msg.attach(MIMEText(body_text, "plain"))
            msg.attach(MIMEText(body_html, "html"))

            with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10) as server:
                server.starttls()
                server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
                server.sendmail(settings.EMAILS_FROM, [clean_to], msg.as_string())

            logger.info(f"[Email OTP] Email successfully sent to {clean_to}")
            return {"status": "success", "gateway": "smtp", "to": clean_to}
        except Exception as e:
            logger.error(f"[Email OTP] Failed to send via SMTP: {e}. Falling back to simulation.")

    logger.info(f"[Email DEV MODE] OTP email for {clean_to}: Code={otp_code}")
    return {
        "status": "success",
        "gateway": "email-simulated",
        "to": clean_to,
        "otp": otp_code,
        "note": "Dev mode: configured or fallback email dispatcher"
    }
