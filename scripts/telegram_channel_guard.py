#!/usr/bin/env python3
"""
Connecto Telegram Channel Guard & Welcome Automator

Features:
1. Greets every new member joining Connecto-Fun (@VCONNECTOFUN).
2. Enforces Admin-Only posting (auto-deletes any unauthorized message from non-admins).
3. Works for both the broadcast channel and any linked discussion group.
"""

import argparse
import json
import logging
import os
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path
from typing import List, Set

# Configure Logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger("channel_guard")

# Load environment variables
ENV_PATHS = [
    Path(__file__).resolve().parent.parent / '.env',
    Path.home() / '.env'
]

def load_env():
    for p in ENV_PATHS:
        if p.exists():
            with open(p, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith('#') and '=' in line:
                        k, v = line.split('=', 1)
                        k = k.strip()
                        v = v.strip().strip('"').strip("'")
                        if k not in os.environ:
                            os.environ[k] = v

load_env()

BOT_TOKEN = os.environ.get("TELEGRAM_BOT_TOKEN")
CHANNEL_ID = os.environ.get("TELEGRAM_CHANNEL", "@VCONNECTOFUN")
ADMIN_CHAT_ID = os.environ.get("TELEGRAM_ADMIN_CHAT_ID")

if not BOT_TOKEN:
    logger.error("TELEGRAM_BOT_TOKEN is missing. Please set it in .env")
    sys.exit(1)

BASE_URL = f"https://api.telegram.org/bot{BOT_TOKEN}"

def api_call(method: str, payload: dict = None) -> dict:
    url = f"{BASE_URL}/{method}"
    try:
        data = None
        headers = {}
        if payload is not None:
            data = json.dumps(payload).encode("utf-8")
            headers["Content-Type"] = "application/json"

        req = urllib.request.Request(url, data=data, headers=headers, method="POST" if data else "GET")
        with urllib.request.urlopen(req, timeout=20) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        err = e.read().decode("utf-8", errors="ignore")
        try:
            return json.loads(err)
        except Exception:
            return {"ok": False, "description": f"HTTP {e.code}: {err}"}
    except Exception as e:
        return {"ok": False, "description": str(e)}

def get_channel_admins(target_chat: str) -> Set[int]:
    """Fetch all admin user IDs for the channel/group."""
    res = api_call("getChatAdministrators", {"chat_id": target_chat})
    admins = set()
    if res.get("ok"):
        for admin in res.get("result", []):
            user = admin.get("user", {})
            u_id = user.get("id")
            if u_id:
                admins.add(u_id)
    return admins

def send_welcome(chat_id: str or int, user_id: int = None, name: str = "Friend"):
    """Send styled welcome message to the channel/group."""
    user_tag = f"<a href=\"tg://user?id={user_id}\">{name}</a>" if user_id else name
    text = (
        f"👋 <b>Welcome to Connecto-Fun, {user_tag}!</b> 🎉\n\n"
        f"We're glad to have you in the official <b>Connecto-Fun</b> channel.\n\n"
        f"• 📌 <b>Updates:</b> Real-time maintenance status & platform announcements\n"
        f"• 🔒 <b>Security:</b> Official notices are posted by administrators only\n"
        f"• 🌐 <b>Connecto Platform:</b> <a href=\"https://connecto.fun\">https://connecto.fun</a>"
    )

    reply_markup = {
        "inline_keyboard": [
            [
                {"text": "🌐 Visit Connecto", "url": "https://connecto.fun"},
                {"text": "💬 Channel Updates", "url": "https://t.me/VCONNECTOFUN"}
            ]
        ]
    }

    return api_call("sendMessage", {
        "chat_id": chat_id,
        "text": text,
        "parse_mode": "HTML",
        "disable_web_page_preview": True,
        "reply_markup": reply_markup
    })

def delete_unauthorized_message(chat_id: int or str, message_id: int, user_name: str):
    """Delete message sent by non-admin."""
    logger.warning("🚨 Unauthorized message from %s in %s. Deleting...", user_name, chat_id)
    res = api_call("deleteMessage", {
        "chat_id": chat_id,
        "message_id": message_id
    })
    if res.get("ok"):
        logger.info("🗑️ Deleted message %d successfully.", message_id)
    else:
        logger.error("Failed to delete message: %s", res.get("description"))

def run_guard(poll_interval: int = 2):
    logger.info("🛡️ Starting Connecto Channel Guard & Welcome Automator...")
    logger.info("   • Target Channel: %s", CHANNEL_ID)
    
    # Verify bot
    me = api_call("getMe")
    if not me.get("ok"):
        logger.error("Bot authentication failed: %s", me.get("description"))
        return
    bot_name = me.get("result", {}).get("username")
    logger.info("   • Bot Active: @%s", bot_name)

    # Cache channel admins
    admins = get_channel_admins(CHANNEL_ID)
    logger.info("   • Verified %d Administrators in channel.", len(admins))
    last_admin_refresh = time.time()

    offset = None
    allowed_updates = ["chat_member", "my_chat_member", "message"]

    logger.info("✅ Channel Guard is live! Monitoring member joins and enforcing Admin-Only chatting.")

    while True:
        try:
            # Refresh admins every 10 minutes
            if time.time() - last_admin_refresh > 600:
                admins = get_channel_admins(CHANNEL_ID)
                last_admin_refresh = time.time()

            payload = {
                "timeout": 15,
                "allowed_updates": allowed_updates
            }
            if offset:
                payload["offset"] = offset

            res = api_call("getUpdates", payload)
            if not res.get("ok"):
                time.sleep(poll_interval)
                continue

            updates = res.get("result", [])
            for u in updates:
                offset = u.get("update_id", 0) + 1

                # 1. Check for new member joining (chat_member update)
                cm = u.get("chat_member")
                if cm:
                    chat = cm.get("chat", {})
                    new_member = cm.get("new_chat_member", {})
                    old_member = cm.get("old_chat_member", {})
                    status_new = new_member.get("status")
                    status_old = old_member.get("status")

                    # If member joined (was 'left' or 'kicked', now 'member')
                    if status_new == "member" and status_old in ["left", "kicked", None]:
                        user = new_member.get("user", {})
                        u_name = user.get("first_name", "Friend")
                        u_id = user.get("id")
                        logger.info("👋 New subscriber joined: %s (ID: %s)", u_name, u_id)
                        send_welcome(chat.get("id"), user_id=u_id, name=u_name)

                # 2. Check for new_chat_members in group messages
                msg = u.get("message")
                if msg:
                    chat = msg.get("chat", {})
                    chat_id = chat.get("id")
                    chat_type = chat.get("type")
                    from_user = msg.get("from", {})
                    user_id = from_user.get("id")
                    user_name = from_user.get("first_name", "Unknown")

                    # New members joined a group
                    new_members = msg.get("new_chat_members")
                    if new_members:
                        for nm in new_members:
                            if not nm.get("is_bot"):
                                nm_name = nm.get("first_name", "Friend")
                                nm_id = nm.get("id")
                                logger.info("👋 New group member joined: %s (ID: %s)", nm_name, nm_id)
                                send_welcome(chat_id, user_id=nm_id, name=nm_name)
                        continue

                    # 3. Enforce Admin-Only chatting in groups / channels
                    # If this is a group/supergroup/channel and sender is NOT in admins list:
                    if chat_type in ["group", "supergroup", "channel"]:
                        if user_id and user_id not in admins and not from_user.get("is_bot"):
                            # Delete the unauthorized message
                            delete_unauthorized_message(chat_id, msg.get("message_id"), user_name)

            time.sleep(0.5)
        except KeyboardInterrupt:
            logger.info("🛑 Guard service stopped by user.")
            break
        except Exception as e:
            logger.error("Exception in guard loop: %s", str(e))
            time.sleep(3)

def main():
    parser = argparse.ArgumentParser(description="Connecto Telegram Channel Guard")
    parser.add_argument("--daemon", action="store_true", help="Run guard service in foreground daemon loop")
    parser.add_argument("--test-welcome", help="Simulate a welcome greeting for given name")
    parser.add_argument("--check-permissions", action="store_true", help="Inspect channel permissions and admins")

    args = parser.parse_args()

    if args.test_welcome:
        print(f"Testing welcome message for: {args.test_welcome}...")
        res = send_welcome(CHANNEL_ID, name=args.test_welcome)
        if res.get("ok"):
            print("✅ Test welcome delivered successfully to channel!")
        else:
            print("❌ Failed:", res.get("description"))
        return

    if args.check_permissions:
        admins = get_channel_admins(CHANNEL_ID)
        print(f"Verified {len(admins)} administrators in {CHANNEL_ID}: {admins}")
        return

    # Default run
    run_guard()

if __name__ == '__main__':
    main()
