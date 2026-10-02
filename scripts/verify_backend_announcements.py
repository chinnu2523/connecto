import asyncio
import os
import sys
import json
import sqlite3
import uuid
from datetime import datetime, timezone, timedelta

sys.path.insert(0, "/Users/madarauchiha/connecto/backend")
os.environ["ENV"] = "test"
os.environ["DATABASE_URL"] = "sqlite+aiosqlite:////Users/madarauchiha/connecto/backend/connecto.db"

import httpx
from app.main import app
from app.core.security import hash_session_token

RAW_TOKEN_REGULAR = "test_regular_auth_token_456"
RAW_TOKEN_ADMIN = "test_admin_auth_token_789"

def setup_test_sessions():
    conn = sqlite3.connect("backend/connecto.db")
    cur = conn.cursor()
    now_iso = datetime.now(timezone.utc).isoformat()
    now_exp = (datetime.now(timezone.utc) + timedelta(days=30)).isoformat()

    # 1. Ensure connecto_admin has is_admin=1
    cur.execute("UPDATE users SET is_admin=1 WHERE username='connecto_admin' OR id='usr_connecto_admin'")
    # 2. Ensure srinu has is_admin=0
    cur.execute("UPDATE users SET is_admin=0 WHERE username='srinu' OR id='usr_8fovb10u'")

    # Insert test session for srinu
    hash_reg = hash_session_token(RAW_TOKEN_REGULAR)
    cur.execute("DELETE FROM user_sessions WHERE session_token_hash=?", (hash_reg,))
    cur.execute(
        "INSERT INTO user_sessions (id, user_id, session_token_hash, created_at, expires_at) VALUES (?, ?, ?, ?, ?)",
        (str(uuid.uuid4()), "usr_8fovb10u", hash_reg, now_iso, now_exp)
    )

    # Insert test session for connecto_admin
    hash_adm = hash_session_token(RAW_TOKEN_ADMIN)
    cur.execute("DELETE FROM user_sessions WHERE session_token_hash=?", (hash_adm,))
    cur.execute(
        "INSERT INTO user_sessions (id, user_id, session_token_hash, created_at, expires_at) VALUES (?, ?, ?, ?, ?)",
        (str(uuid.uuid4()), "usr_connecto_admin", hash_adm, now_iso, now_exp)
    )

    conn.commit()
    conn.close()
    print("✓ Test user sessions configured in connecto.db")

async def test_backend_announcements():
    setup_test_sessions()

    print("\n=======================================================")
    print("STEP 1: TESTING BACKEND REST AUTHORIZATION FOR #announcements")
    print("=======================================================")

    transport = httpx.ASGITransport(app=app)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as client:
        headers_regular = {"Authorization": f"Bearer {RAW_TOKEN_REGULAR}"}
        headers_admin = {"Authorization": f"Bearer {RAW_TOKEN_ADMIN}"}

        # 1.1 Non-admin user tries to post to /api/channels/announcements/messages
        res_post_reg = await client.post(
            "/api/channels/announcements/messages",
            json={"content": "Attempting unauthorized post as regular user", "channel_id": "announcements"},
            headers=headers_regular
        )
        print(f"[TEST 1.1] Non-admin POST /api/channels/announcements/messages status: {res_post_reg.status_code}")
        assert res_post_reg.status_code == 403, f"Expected 403 Forbidden, got {res_post_reg.status_code}: {res_post_reg.text}"
        detail_msg = res_post_reg.json().get("detail", "")
        print(f"  -> Response detail: {detail_msg}")
        assert "Access Denied" in detail_msg or "strictly reserved" in detail_msg

        # 1.2 Non-admin user tries to post to /api/messages with channel_id='announcements'
        res_post_reg2 = await client.post(
            "/api/messages",
            json={"content": "Attempting unauthorized post via /api/messages", "channel_id": "announcements"},
            headers=headers_regular
        )
        print(f"[TEST 1.2] Non-admin POST /api/messages status: {res_post_reg2.status_code}")
        assert res_post_reg2.status_code == 403, f"Expected 403 Forbidden, got {res_post_reg2.status_code}: {res_post_reg2.text}"

        # 1.3 Non-admin user tries to schedule message in announcements
        res_sched_reg = await client.post(
            "/api/channels/announcements/scheduled-messages",
            json={"content": "Scheduled spam in announcements", "delivery_time_epoch": 9999999999},
            headers=headers_regular
        )
        print(f"[TEST 1.3] Non-admin POST scheduled-messages status: {res_sched_reg.status_code}")
        assert res_sched_reg.status_code == 403, f"Expected 403 Forbidden, got {res_sched_reg.status_code}: {res_sched_reg.text}"

        # 1.4 Admin user (connecto_admin) posts official message to /api/channels/announcements/messages
        res_post_adm = await client.post(
            "/api/channels/announcements/messages",
            json={"content": "Verified Administrator Broadcast: Maintenance Telemetry & Cluster Status Green.", "channel_id": "announcements"},
            headers=headers_admin
        )
        print(f"[TEST 1.4] Admin POST /api/channels/announcements/messages status: {res_post_adm.status_code}")
        assert res_post_adm.status_code == 200, f"Expected 200 OK for admin, got {res_post_adm.status_code}: {res_post_adm.text}"

        # 1.5 Fetch announcements messages to verify seeded content
        res_get = await client.get("/api/channels/announcements/messages")
        assert res_get.status_code == 200, f"Failed to get messages: {res_get.text}"
        data = res_get.json()
        messages = data.get("messages", []) if isinstance(data, dict) else data
        print(f"[TEST 1.5] Retrieved {len(messages)} announcements messages.")
        contents = [m.get("content", "") for m in messages]
        has_maintenance = any("MAINTENANCE" in c or "Telemetry" in c for c in contents)
        has_v398 = any("v3.9.8" in c for c in contents)
        print(f"  -> Has Server Maintenance Notice: {has_maintenance}")
        print(f"  -> Has v3.9.8 Platform Release: {has_v398}")
        assert has_maintenance and has_v398, "Expected maintenance and v3.9.8 announcements to be present."
        print("\n✓ ALL BACKEND REST AUTHORIZATION & SEEDING TESTS PASSED!")

if __name__ == "__main__":
    asyncio.run(test_backend_announcements())
