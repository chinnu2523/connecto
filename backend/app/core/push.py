"""
FCM Push Notification helper.

Firebase Admin SDK is initialized lazily and reliably on first use.
Supports explicit FIREBASE_CREDENTIALS_PATH, GOOGLE_APPLICATION_CREDENTIALS,
well-known ADC locations, or system ApplicationDefault credentials.
"""
import os
import asyncio
import logging
from typing import Optional, Dict, Any

logger = logging.getLogger(__name__)

import threading

_firebase_app = None
_firebase_available = False
_firebase_lock = threading.Lock()

def _init_firebase():
    """Initialize Firebase Admin SDK once, lazily and thread-safely."""
    global _firebase_app, _firebase_available
    if _firebase_available and _firebase_app is not None:
        return True
    with _firebase_lock:
        if _firebase_available and _firebase_app is not None:
            return True
        try:
            import firebase_admin
            from firebase_admin import credentials

            # Check if default Firebase app is already initialized
            try:
                _firebase_app = firebase_admin.get_app()
                _firebase_available = True
                logger.info("[FCM] Found existing initialized Firebase app.")
                return True
            except ValueError:
                pass

            candidate_paths = [
                os.environ.get("FIREBASE_CREDENTIALS_PATH"),
                os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"),
                os.path.expanduser("~/.config/gcloud/application_default_credentials.json"),
                "/srv/apps/connecto-app/firebase-service-account.json",
                os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../android/app/google-services.json"))
            ]

            cred = None
            for p in candidate_paths:
                if p and os.path.exists(p):
                    try:
                        cred = credentials.Certificate(p)
                        logger.info(f"[FCM] Loaded Firebase Certificate from {p}")
                        break
                    except Exception:
                        try:
                            os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = p
                            cred = credentials.ApplicationDefault()
                            logger.info(f"[FCM] Loaded ApplicationDefault credentials from {p}")
                            break
                        except Exception:
                            pass

            if cred is None:
                try:
                    cred = credentials.ApplicationDefault()
                    logger.info("[FCM] Loaded system ApplicationDefault credentials.")
                except Exception as e:
                    logger.warning(f"[FCM] ApplicationDefault lookup error: {e}")

            if cred is None:
                logger.warning(
                    "[FCM] No valid Firebase credentials found. Push notifications will be skipped."
                )
                _firebase_available = False
                return False

            _firebase_app = firebase_admin.initialize_app(cred, {"projectId": "jarvisgmail-488814"})
            _firebase_available = True
            logger.info("[FCM] Firebase Admin SDK initialized successfully for project jarvisgmail-488814.")
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
    android_priority: str = "high"
) -> bool:
    """Synchronously sends a push notification via FCM with high priority. Returns True on success."""
    if not _init_firebase():
        return False
    if not fcm_token:
        return False
    try:
        from firebase_admin import messaging

        avatar_img = None
        if data:
            raw_av = data.get("sender_avatar") or data.get("avatar") or data.get("image")
            if raw_av and str(raw_av).strip().lower() not in ("null", "none", "undefined"):
                clean_av = str(raw_av).strip()
                if clean_av.startswith("/uploads/") or clean_av.startswith("uploads/"):
                    avatar_img = "https://connecto.fun/" + clean_av.lstrip("/")
                elif clean_av.startswith("http://") or clean_av.startswith("https://"):
                    avatar_img = clean_av

        # Sanitize data to strictly adhere to FCM 4KB limit and reject base64 data URIs
        merged_data = {}
        if data:
            for k, v in data.items():
                if v is None:
                    continue
                v_str = str(v)
                if ("avatar" in k.lower() or "image" in k.lower()) and (v_str.startswith("data:") or len(v_str) > 500):
                    continue
                if len(v_str) > 512:
                    v_str = v_str[:512]
                merged_data[k] = v_str

        merged_data["type"] = notification_type
        merged_data["title"] = title[:128]
        merged_data["body"] = body[:256]
        if avatar_img and not avatar_img.startswith("data:"):
            merged_data["sender_avatar"] = avatar_img
            merged_data["avatar"] = avatar_img
        if "extra_notification_action" not in merged_data:
            merged_data["extra_notification_action"] = "com.example.connecto.ACTION_OPEN_CHAT"

        channel_id = "connecto_messages_v4"
        if notification_type in ("call", "call_invite", "incoming_call"):
            channel_id = "connecto_voice_calls_v2"
        elif notification_type in ("friend_request", "social"):
            channel_id = "connecto_social_v2"

        tag = merged_data.get("channel_id") or merged_data.get("sender_id") or merged_data.get("sender_username") or "connecto"

        is_call = notification_type in ("call", "call_invite", "incoming_call", "call_ended", "call_cancelled", "call_declined")
        if is_call:
            merged_data["extra_notification_action"] = "com.example.connecto.ACTION_ANSWER_CALL"

        # All FCM push notifications are sent as high-priority data-only messages.
        # This guarantees ConnectoFirebaseMessagingService builds strictly the native Android
        # MessagingStyle conversation notification (contact avatar on left), permanently eliminating
        # the duplicate generic app-icon notification posted automatically by Google Play Services.
        android_config = messaging.AndroidConfig(
            priority="high",
            ttl=60 if is_call else 86400
        )
        message = messaging.Message(
            token=fcm_token,
            android=android_config,
            data=merged_data
        )

        response = messaging.send(message)
        logger.info(f"[FCM] WhatsApp-grade push sent. MessageID={response} channel={channel_id} token={fcm_token[:20]}...")
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
