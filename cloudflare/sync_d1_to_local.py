"""
Sync all data from Cloudflare D1 into local SQLite database.
"""
import json
import sqlite3
import urllib.request
import os

D1_KEY = "connecto_d1_sec_2026_prod"
D1_URL = "https://connecto.fun/api/v1/db/query"

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DB_PATH = os.path.join(BASE_DIR, "backend", "connecto.db")
STAGING_DB_PATH = os.path.join(BASE_DIR, "backend", "connecto_staging.db")

HEADERS = {
    "Content-Type": "application/json",
    "X-D1-Key": D1_KEY,
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
}

def query_d1(sql):
    req = urllib.request.Request(
        D1_URL,
        data=json.dumps({"sql": sql}).encode("utf-8"),
        headers=HEADERS
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))

def sync_from_d1(conn, table_name):
    cur = conn.cursor()
    # Check if table exists locally
    exists = cur.execute("SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?", (table_name,)).fetchone()[0]
    if not exists:
        print(f"Skipping {table_name}: not in local sqlite")
        return 0

    d1_res = query_d1(f"SELECT * FROM {table_name}")
    d1_rows = d1_res.get("results", [])
    if not d1_rows:
        return 0

    local_cols = [c[1] for c in cur.execute(f"PRAGMA table_info({table_name})").fetchall()]
    d1_cols = list(d1_rows[0].keys())
    common_cols = [c for c in d1_cols if c in local_cols]
    if not common_cols:
        return 0

    placeholders = ", ".join(["?" for _ in common_cols])
    col_str = ", ".join(common_cols)
    sql = f"INSERT OR REPLACE INTO {table_name} ({col_str}) VALUES ({placeholders})"

    rows_to_insert = []
    for r in d1_rows:
        rows_to_insert.append(tuple(r[c] for c in common_cols))

    cur.executemany(sql, rows_to_insert)
    conn.commit()
    return len(rows_to_insert)

def main():
    print(f"Opening local SQLite: {DB_PATH}")
    conn = sqlite3.connect(DB_PATH)

    # Disable foreign keys during initial bulk load to prevent ordering issues
    conn.execute("PRAGMA foreign_keys = OFF;")

    tables = [
        "users", "servers", "channels", "server_members", "dm_participants",
        "messages", "friendships", "message_read_receipts", "notifications",
        "course_tracks", "lessons", "quiz_questions", "job_openings",
        "voice_rooms", "voice_room_participants", "call_logs",
        "scheduled_messages", "audit_logs"
    ]

    for tbl in tables:
        count = sync_from_d1(conn, tbl)
        print(f"  [D1 -> SQLite] {tbl:<25}: {count} row(s)")

    conn.execute("PRAGMA foreign_keys = ON;")
    conn.close()

    print(f"Copying to {STAGING_DB_PATH}...")
    with open(DB_PATH, "rb") as src, open(STAGING_DB_PATH, "wb") as dst:
        dst.write(src.read())

    print("Sync complete!")

if __name__ == "__main__":
    main()
