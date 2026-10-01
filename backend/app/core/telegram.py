"""
Core Telegram Integration Module for Connecto
Provides asynchronous and synchronous broadcasting of maintenance notices and alerts.
"""

import asyncio
import json
import logging
import os
import urllib.request
from typing import Optional

logger = logging.getLogger("connecto.telegram")

BOT_TOKEN = os.environ.get("TELEGRAM_BOT_TOKEN")
DEFAULT_CHANNEL = os.environ.get("TELEGRAM_CHANNEL", "@VCONNECTOFUN")
ADMIN_CHAT_ID = os.environ.get("TELEGRAM_ADMIN_CHAT_ID")

async def send_telegram_alert(
    text: str,
    target: Optional[str] = None,
    parse_mode: str = "HTML",
    inline_keyboard: Optional[list] = None
) -> bool:
    """Send an asynchronous Telegram message to channel or admin chat."""
    token = os.environ.get("TELEGRAM_BOT_TOKEN") or BOT_TOKEN
    chat_id = target or os.environ.get("TELEGRAM_CHANNEL", DEFAULT_CHANNEL)

    if not token:
        logger.warning("TELEGRAM_BOT_TOKEN not set; skipping Telegram alert.")
        return False

    url = f"https://api.telegram.org/bot{token}/sendMessage"
    payload = {
        "chat_id": chat_id,
        "text": text,
        "parse_mode": parse_mode,
        "disable_web_page_preview": False
    }
    if inline_keyboard:
        payload["reply_markup"] = {"inline_keyboard": inline_keyboard}

    try:
        def _post():
            data = json.dumps(payload).encode("utf-8")
            req = urllib.request.Request(
                url,
                data=data,
                headers={"Content-Type": "application/json"},
                method="POST"
            )
            with urllib.request.urlopen(req, timeout=10) as resp:
                return json.loads(resp.read().decode("utf-8"))

        loop = asyncio.get_event_loop()
        res = await loop.run_in_executor(None, _post)
        if res.get("ok"):
            logger.info("Telegram alert delivered successfully to %s", chat_id)
            return True
        else:
            logger.error("Telegram API error: %s", res.get("description"))
            return False
    except Exception as e:
        logger.error("Failed to send Telegram message: %s", str(e))
        return False
