#!/usr/bin/env python3
"""
Telegram Broadcaster & Account Connector for Connecto (@VCONNECTOFUN)

Supports:
1. Testing Bot API connectivity (`getMe`).
2. Discovering admin personal chat ID (`getUpdates`).
3. Broadcasting styled maintenance notices to channel `@VCONNECTOFUN`.
4. Sending direct private alerts to the administrator's Telegram account.
"""

import argparse
import json
import os
import sys
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

# Paths to search for .env
ENV_PATHS = [
    Path(__file__).resolve().parent.parent / '.env',
    Path.home() / '.env'
]

def load_env_file():
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

load_env_file()

BOT_TOKEN = os.environ.get('TELEGRAM_BOT_TOKEN')
CHANNEL_ID = os.environ.get('TELEGRAM_CHANNEL', '@VCONNECTOFUN')
ADMIN_CHAT_ID = os.environ.get('TELEGRAM_ADMIN_CHAT_ID')

BASE_URL = f"https://api.telegram.org/bot{BOT_TOKEN}" if BOT_TOKEN else None

def call_telegram_api(method: str, payload: dict = None) -> dict:
    if not BOT_TOKEN:
        return {
            "ok": False,
            "error": "TELEGRAM_BOT_TOKEN is not set. Add it to .env or environment."
        }

    url = f"https://api.telegram.org/bot{BOT_TOKEN}/{method}"
    try:
        data = None
        headers = {}
        if payload is not None:
            data = json.dumps(payload).encode('utf-8')
            headers['Content-Type'] = 'application/json'

        req = urllib.request.Request(url, data=data, headers=headers, method='POST' if data else 'GET')
        with urllib.request.urlopen(req, timeout=15) as resp:
            body = resp.read().decode('utf-8')
            return json.loads(body)
    except urllib.error.HTTPError as e:
        err_body = e.read().decode('utf-8', errors='ignore')
        try:
            return json.loads(err_body)
        except Exception:
            return {"ok": False, "description": f"HTTP {e.code}: {err_body}"}
    except Exception as e:
        return {"ok": False, "description": str(e)}

def test_connection():
    res = call_telegram_api("getMe")
    if res.get("ok"):
        bot = res.get("result", {})
        print("✅ Telegram Bot Connected Successfully!")
        print(f"   • Bot ID:       {bot.get('id')}")
        print(f"   • Bot Name:     {bot.get('first_name')}")
        print(f"   • Username:     @{bot.get('username')}")
        print(f"   • Can Read Msgs: {bot.get('can_read_all_group_messages', False)}")
        return True
    else:
        print("❌ Telegram Bot Connection Failed:")
        print(f"   {res.get('description', 'Unknown error')}")
        return False

def get_updates():
    res = call_telegram_api("getUpdates")
    if not res.get("ok"):
        print("❌ Failed to fetch updates:", res.get("description"))
        return []

    updates = res.get("result", [])
    if not updates:
        print("ℹ️ No recent messages found.")
        print("💡 Tip: Open Telegram, search for your bot, and send /start to register your account.")
        return []

    print(f"📨 Found {len(updates)} recent updates:")
    users = {}
    for u in updates:
        msg = u.get("message") or u.get("channel_post")
        if msg:
            chat = msg.get("chat", {})
            user_from = msg.get("from", {})
            c_id = chat.get("id")
            c_type = chat.get("type")
            title = chat.get("title") or f"{user_from.get('first_name', '')} {user_from.get('last_name', '')}".strip()
            username = chat.get("username") or user_from.get("username")
            text = msg.get("text", "")
            users[c_id] = {
                "chat_id": c_id,
                "type": c_type,
                "name": title,
                "username": f"@{username}" if username else "N/A",
                "last_text": text
            }

    for c_id, info in users.items():
        print(f"   • Chat ID: {info['chat_id']} | Type: {info['type']} | User: {info['name']} ({info['username']}) | Text: {repr(info['last_text'])}")

    return list(users.values())

def send_message(chat_id: str, text: str, parse_mode: str = "HTML", reply_markup: dict = None) -> bool:
    payload = {
        "chat_id": chat_id,
        "text": text,
        "parse_mode": parse_mode,
        "disable_web_page_preview": False
    }
    if reply_markup:
        payload["reply_markup"] = reply_markup

    res = call_telegram_api("sendMessage", payload)
    if res.get("ok"):
        print(f"✅ Message delivered to {chat_id}")
        return True
    else:
        print(f"❌ Failed to send message to {chat_id}: {res.get('description')}")
        return False

def broadcast_maintenance(status: str = "started", eta: str = "15 minutes", target: str = None):
    target = target or CHANNEL_ID
    now_utc = datetime.now(timezone.utc).strftime("%B %d, %Y - %H:%M UTC")

    if status == "started":
        text = (
            f"🛠 <b>[CONNECTO] Routine Server Maintenance Started</b>\n\n"
            f"We are performing scheduled server and database upgrades to ensure maximum performance and security.\n\n"
            f"• <b>Status:</b> Maintenance Active (Gateway Online)\n"
            f"• <b>Estimated Duration:</b> ~{eta}\n"
            f"• <b>Preservation:</b> 100% data, chats, and media safe\n"
            f"• <b>Timestamp:</b> {now_utc}\n\n"
            f"🔗 <b>Live Status:</b> <a href=\"https://connecto.fun\">https://connecto.fun</a>\n"
            f"📢 <i>Updates will be posted here as work progresses.</i>"
        )
    elif status == "online":
        text = (
            f"🎉 <b>[CONNECTO] Maintenance Complete — Connecto is Back Online!</b>\n\n"
            f"All system upgrades and database optimizations have completed successfully. Services are fully operational.\n\n"
            f"• <b>Status:</b> All Systems Operational 🟢\n"
            f"• <b>Latency:</b> Optimal (<50ms)\n"
            f"• <b>Timestamp:</b> {now_utc}\n\n"
            f"🚀 <b>Enter Connecto:</b> <a href=\"https://connecto.fun\">https://connecto.fun</a>\n"
            f"<i>Thank you for your patience!</i>"
        )
    else: # general update
        text = (
            f"ℹ️ <b>[CONNECTO] System Status Update</b>\n\n"
            f"• <b>Status:</b> {status}\n"
            f"• <b>Timestamp:</b> {now_utc}\n\n"
            f"🔗 <a href=\"https://connecto.fun\">https://connecto.fun</a>"
        )

    inline_keyboard = {
        "inline_keyboard": [
            [
                {"text": "🌐 View Connecto Status", "url": "https://connecto.fun"},
                {"text": "💬 Channel Updates", "url": "https://t.me/VCONNECTOFUN"}
            ]
        ]
    }

    return send_message(target, text, parse_mode="HTML", reply_markup=inline_keyboard)

def main():
    parser = argparse.ArgumentParser(description="Connecto Telegram Broadcaster")
    parser.add_argument("--test", action="store_true", help="Test bot token and connection")
    parser.add_argument("--get-updates", action="store_true", help="Fetch recent bot updates to identify chat IDs")
    parser.add_argument("--status", choices=["started", "online", "update"], help="Send styled maintenance status")
    parser.add_argument("--eta", default="15 minutes", help="Estimated duration for maintenance notice")
    parser.add_argument("--to-channel", action="store_true", help="Broadcast to @VCONNECTOFUN channel")
    parser.add_argument("--to-admin", action="store_true", help="Send alert directly to TELEGRAM_ADMIN_CHAT_ID")
    parser.add_argument("--chat-id", help="Explicit target chat ID or @channel")
    parser.add_argument("--message", help="Custom message text to send")

    args = parser.parse_args()

    if args.test:
        test_connection()
        return

    if args.get_updates:
        get_updates()
        return

    if args.status:
        dest = args.chat_id or (CHANNEL_ID if args.to_channel else ADMIN_CHAT_ID or CHANNEL_ID)
        broadcast_maintenance(status=args.status, eta=args.eta, target=dest)
        return

    if args.message:
        dest = args.chat_id or (CHANNEL_ID if args.to_channel else ADMIN_CHAT_ID)
        if not dest:
            print("❌ Target chat ID missing. Specify --chat-id or --to-channel.")
            return
        send_message(dest, args.message)
        return

    # Default if no arguments:
    if not BOT_TOKEN:
        print("⚠️ No TELEGRAM_BOT_TOKEN detected in environment or .env file.")
        print("   Run with --test once you have configured TELEGRAM_BOT_TOKEN.")
    else:
        test_connection()

if __name__ == '__main__':
    main()
