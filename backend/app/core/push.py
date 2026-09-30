"""
FCM Push Notification helper.

Firebase Admin SDK is initialized lazily on first use.
If no FIREBASE_CREDENTIALS_PATH is configured, push notifications
are silently skipped (graceful degradation).
"""
import os
import asyncio
import logging
from typing import Optional, Dict, Any

logger = logging.getLogger(__name__)

_firebase_app = None
_firebase_available = False

def _init_firebase():
    """Initialize Firebase Admin SDK once, lazily."""
    global _firebase_app, _firebase_available
    if _firebase_app is not None:
        return _firebase_available
    try:
        import firebase_admin
        from firebase_admin import credentials

        cred_path = os.environ.get("FIREBASE_CREDENTIALS_PATH", "/srv/apps/connecto-app/firebase-service-account.json")
        if not cred_path or not os.path.exists(cred_path):
            logger.warning(
                "[FCM] FIREBASE_CREDENTIALS_PATH not set or file not found. "
                "Push notifications will be skipped."
            )
            _firebase_available = False
            return False

        cred = credentials.Certificate(cred_path)
        _firebase_app = firebase_admin.initialize_app(cred)
        _firebase_available = True
        logger.info("[FCM] Firebase Admin SDK initialized successfully.")
        return True
    except ImportError:
        logger.warning("[FCM] firebase-admin not installed. Push notifications disabled.")
        _firebase_available = False
        return False
    except Exception as e:
        logger.warning(f"[FCM] Firebase init failed: {e}")
        _firebase_available = False
        return False


def send_push_notification_sync(
    fcm_token: str,
    title: str,
    body: str,
    data: Optional[Dict[str, str]] = None,
    notification_type: str = "default",
    android_priority: str = "normal"
) -> bool:
    """Synchronously sends a push notification via FCM. Returns True on success."""
    if not _init_firebase():
        return False
    if not fcm_token:
        return False
    try:
        from firebase_admin import messaging

        avatar_img = None
        if data:
            avatar_img = data.get("sender_avatar") or data.get("avatar") or data.get("image")
            if avatar_img and (avatar_img.startswith("/uploads/") or avatar_img.startswith("uploads/")):
                avatar_img = "https://connecto.fun/" + avatar_img.lstrip("/")
            elif avatar_img and not avatar_img.startswith("http"):
                avatar_img = None

        android_notif_kwargs = {
            "title": title,
            "body": body,
            "sound": "default",
            "channel_id": "connecto_voice_calls_v2" if notification_type in ("call_invite", "call", "incoming_call") else "connecto_messages_v2",
            "priority": "max" if notification_type == "call_invite" else "default",
            "visibility": "public" if notification_type == "call_invite" else "private",
            "sticky": notification_type == "call_invite",
        }
        if avatar_img:
            android_notif_kwargs["image"] = avatar_img

        android_config = messaging.AndroidConfig(
            priority="high" if notification_type == "call_invite" else android_priority,
            notification=messaging.AndroidNotification(**android_notif_kwargs)
        )

        message = messaging.Message(
            token=fcm_token,
            notification=messaging.Notification(title=title, body=body),
            android=android_config,
            data={k: str(v) for k, v in (data or {}).items()} | {"type": notification_type}
        )

        response = messaging.send(message)
        logger.info(f"[FCM] Push sent. MessageID={response} token={fcm_token[:20]}...")
        return True
    except Exception as e:
        # Token may be expired — log but do NOT crash the request
        logger.warning(f"[FCM] Push failed (token may be expired): {e}")
        return False


async def send_push_notification(
    fcm_token: str,
    title: str,
    body: str,
    data: Optional[Dict[str, str]] = None,
    notification_type: str = "default"
) -> bool:
    """Async wrapper — runs FCM send in a thread pool to avoid blocking the event loop."""
    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(
        None,
        lambda: send_push_notification_sync(fcm_token, title, body, data, notification_type)
    )
