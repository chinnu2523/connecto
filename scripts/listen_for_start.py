#!/usr/bin/env python3
"""
Listen for /start or any incoming message to @vconnecto_alerts_bot.
Once detected, captures the user's chat_id, updates .env, and sends a confirmation DM.
"""

import json
import os
import time
import urllib.request
from pathlib import Path

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
                        if k.strip() not in os.environ:
                            os.environ[k.strip()] = v.strip().strip('"').strip("'")

load_env()
TOKEN = os.environ.get("TELEGRAM_BOT_TOKEN")
ENV_PATH = Path(__file__).resolve().parent.parent / ".env"

def send_reply(chat_id, first_name):
    url = f"https://api.telegram.org/bot{TOKEN}/sendMessage"
    payload = {
        "chat_id": chat_id,
        "text": (
            f"🎉 <b>Connection Confirmed!</b>\n\n"
            f"Hello {first_name}! Your Telegram account is now linked to <b>Connecto</b>.\n\n"
            f"• <b>Admin Chat ID:</b> <code>{chat_id}</code>\n"
            f"• <b>Target Channel:</b> @VCONNECTOFUN\n"
            f"• <b>Alerts:</b> Enabled 🟢\n\n"
            f"You will receive direct maintenance updates, server alerts, and incident reports here."
        ),
        "parse_mode": "HTML",
        "reply_markup": {
            "inline_keyboard": [
                [
                    {"text": "🌐 Open Connecto", "url": "https://connecto.fun"},
                    {"text": "📢 Updates Channel", "url": "https://t.me/VCONNECTOFUN"}
                ]
            ]
        }
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))

def save_chat_id(chat_id):
    lines = []
    if ENV_PATH.exists():
        with open(ENV_PATH, "r", encoding="utf-8") as f:
            lines = f.readlines()

    found = False
    new_lines = []
    for line in lines:
        if line.strip().startswith("TELEGRAM_ADMIN_CHAT_ID="):
            new_lines.append(f"TELEGRAM_ADMIN_CHAT_ID={chat_id}\n")
            found = True
        else:
            new_lines.append(line)

    if not found:
        if new_lines and not new_lines[-1].endswith("\n"):
            new_lines.append("\n")
        new_lines.append(f"TELEGRAM_ADMIN_CHAT_ID={chat_id}\n")

    with open(ENV_PATH, "w", encoding="utf-8") as f:
        f.writelines(new_lines)
    print(f"💾 Saved TELEGRAM_ADMIN_CHAT_ID={chat_id} to {ENV_PATH}")

def listen(timeout_total=90):
    print(f"👂 Listening for incoming messages on @vconnecto_alerts_bot for {timeout_total}s...")
    start_time = time.time()
    offset = None

    while time.time() - start_time < timeout_total:
        try:
            url = f"https://api.telegram.org/bot{TOKEN}/getUpdates?timeout=5"
            if offset:
                url += f"&offset={offset}"

            req = urllib.request.Request(url)
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode("utf-8"))

            if data.get("ok"):
                updates = data.get("result", [])
                for u in updates:
                    offset = u.get("update_id", 0) + 1
                    msg = u.get("message")
                    if msg:
                        chat = msg.get("chat", {})
                        user = msg.get("from", {})
                        chat_id = chat.get("id")
                        chat_type = chat.get("type")
                        first_name = user.get("first_name", "Admin")
                        username = user.get("username", "")

                        if chat_type == "private" and chat_id:
                            print(f"\n🎯 FOUND USER MESSAGE!")
                            print(f"   • User: {first_name} (@{username})")
                            print(f"   • Chat ID: {chat_id}")
                            print(f"   • Message: {msg.get('text', '')}")

                            # Save to .env
                            save_chat_id(chat_id)

                            # Send immediate confirmation message
                            res = send_reply(chat_id, first_name)
                            if res.get("ok"):
                                print(f"✅ Sent confirmation DM to {first_name} ({chat_id})!")
                            else:
                                print(f"⚠️ Failed to send confirmation DM: {res}")
                            return chat_id

            time.sleep(1)
        except Exception as e:
            time.sleep(2)

    print("\n⏱️ Timeout reached without receiving a message.")
    return None

if __name__ == "__main__":
    listen()
