import sqlite3
import json
import urllib.request
import sys

d1_key = "connecto_d1_sec_2026_prod"
d1_url = "https://connecto.fun/api/v1/db/query"

def query_d1(sql):
    req = urllib.request.Request(
        d1_url,
        data=json.dumps({"sql": sql}).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "X-D1-Key": d1_key,
            "User-Agent": "Mozilla/5.0"
        }
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))

def esc(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"

backup_db = "/Users/madarauchiha/Desktop/connecto_backups/connecto_backup_20260909_215020.db"
conn_b = sqlite3.connect(backup_db)
conn_b.row_factory = sqlite3.Row
b_users = conn_b.execute("SELECT * FROM users").fetchall()
user_map = {}
for u in b_users:
    uname = u["username"].lower()
    user_map[u["id"]] = f"usr_{uname}"

local_db = "/Users/madarauchiha/connecto/backend/connecto_staging.db"
conn_l = sqlite3.connect(local_db)
conn_l.row_factory = sqlite3.Row
cur_l = conn_l.cursor()

msgs = cur_l.execute("SELECT * FROM messages WHERE id NOT LIKE '%_name'").fetchall()

# Get D1 existing IDs
d1_res = query_d1("SELECT id FROM messages;")
d1_ids = {r["id"] for r in d1_res.get("results", [])}

success_count = 0
failed_count = 0
for m in msgs:
    mid = m["id"]
    if mid in d1_ids:
        success_count += 1
        continue
    raw_sender = m["sender_id"]
    clean_uuid = raw_sender.replace("usr_", "")
    sender_id = user_map.get(clean_uuid) or user_map.get(raw_sender) or raw_sender
    if not sender_id.startswith("usr_"):
        sender_id = f"usr_{sender_id.lower()}"
    
    ch_id = m["channel_id"]
    content = m["content"]
    created = m["created_at"]
    
    sql = f"INSERT OR REPLACE INTO messages (id, channel_id, sender_id, content, attachments, created_at) VALUES ({esc(mid)}, {esc(ch_id)}, {esc(sender_id)}, {esc(content)}, '{{}}', {esc(created)});"
    try:
        res = query_d1(sql)
        if res.get("success"):
            success_count += 1
            print(f"Synced missing: {mid} -> OK")
        else:
            print(f"Failed {mid}: {res.get('error')}")
            failed_count += 1
    except Exception as e:
        print(f"Exception {mid}: {e}")
        failed_count += 1

print(f"Sync complete! Total verified in D1: {success_count} / {len(msgs)}, Failed: {failed_count}")
