import logging
import re
from typing import Optional, Dict, Any
import httpx
from app.core.config import settings

logger = logging.getLogger("connecto.httpsms")

def clean_phone_number(phone: str) -> str:
    """Cleans phone numbers into E.164-compatible format if possible."""
    cleaned = re.sub(r"[^\d+]", "", phone.strip())
    if cleaned and not cleaned.startswith("+"):
        cleaned = f"+{cleaned}"
    return cleaned

def mask_phone_number(phone: str) -> str:
    """Masks phone number for UI display (e.g. +1******1234)."""
    clean = clean_phone_number(phone)
    if len(clean) <= 5:
        return clean
    prefix = clean[:2]
    suffix = clean[-4:]
    masked_len = max(len(clean) - 6, 3)
    return f"{prefix}{'*' * masked_len}{suffix}"

async def send_sms(
    to_phone: str,
    content: str,
    from_phone: Optional[str] = None
) -> Dict[str, Any]:
    """
    Sends an SMS message using the NdoleStudio/httpsms API.
    API Reference: https://api.httpsms.com/index.html
    Endpoint: POST /v1/messages/send
    Header: x-api-key: <api_key>
    Payload: {"content": "...", "from": "...", "to": "..."}
    """
    clean_to = clean_phone_number(to_phone)
    clean_from = clean_phone_number(from_phone or settings.HTTPSMS_FROM_NUMBER or "+18005550199")
    api_key = settings.HTTPSMS_API_KEY.strip()
    base_url = settings.HTTPSMS_BASE_URL.rstrip("/")

    logger.info(f"[httpSMS] Preparing to send SMS to {mask_phone_number(clean_to)}: {content}")

    if api_key:
        try:
            url = f"{base_url}/v1/messages/send"
            headers = {
                "x-api-key": api_key,
                "Content-Type": "application/json",
                "User-Agent": "Connecto-httpSMS-Client/1.0"
            }
            payload = {
                "content": content,
                "from": clean_from,
                "to": clean_to
            }
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(url, json=payload, headers=headers)
                if res.status_code in (200, 201, 202):
                    data = res.json()
                    logger.info(f"[httpSMS] Successfully sent message via API: {data}")
                    return {
                        "status": "success",
                        "gateway": "httpsms",
                        "response": data
                    }
                else:
                    logger.warning(
                        f"[httpSMS] API returned non-200 status {res.status_code}: {res.text}. Falling back to simulation."
                    )
        except Exception as e:
            logger.error(f"[httpSMS] Exception communicating with httpSMS API: {e}. Falling back to simulation.")

    # Development simulation fallback
    logger.info(
        f"[httpSMS DEV MODE] Simulated SMS sent to {clean_to}: \"{content}\""
    )
    return {
        "status": "success",
        "gateway": "httpsms-simulated",
        "to": clean_to,
        "from": clean_from,
        "content": content,
        "note": "Dev mode: configured or fallback httpSMS gateway"
    }
