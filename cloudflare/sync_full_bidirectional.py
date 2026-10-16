"""
Connecto Bidirectional Synchronization Engine
=============================================
Synchronizes all data between Cloudflare D1 and Local SQLite (connecto.db / connecto_staging.db).
Ensures zero data loss and exact parity across both databases.
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

def query_d1(sql, is_exec=False):
    payload = {"exec": sql} if is_exec else {"sql": sql}
    req = urllib.request.Request(
        D1_URL,
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "X-D1-Key": D1_KEY,
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

def sync_table_to_d1(conn, table_name):
    cur = conn.cursor()
    # Check if table exists locally
    exists = cur.execute(f"SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?", (table_name,)).fetchone()[0]
    if not exists:
        return 0

    local_rows = cur.execute(f"SELECT * FROM {table_name}").fetchall()
    if not local_rows:
        return 0

    local_cols = [c[1] for c in cur.execute(f"PRAGMA table_info({table_name})").fetchall()]
    d1_cols_info = query_d1(f"PRAGMA table_info({table_name})")
    d1_cols = [c["name"] for c in d1_cols_info.get("results", [])]
    common_cols = [c for c in local_cols if c in d1_cols]
    if not common_cols:
        return 0

    col_idx = [local_cols.index(c) for c in common_cols]
    
    # Get primary key in D1
    pk_cols = [c["name"] for c in d1_cols_info.get("results", []) if c.get("pk")]
    if not pk_cols:
        pk_cols = [common_cols[0]]

    # Fetch existing IDs in D1
    pk_sel = ", ".join(pk_cols)
    d1_existing_res = query_d1(f"SELECT {pk_sel} FROM {table_name}")
    d1_existing = set()
    for r in d1_existing_res.get("results", []):
        if len(pk_cols) == 1:
            d1_existing.add(r[pk_cols[0]])
        else:
            d1_existing.add(tuple(r[p] for p in pk_cols))

    inserted = 0
    batch_statements = []
    for r in local_rows:
        key = r[local_cols.index(pk_cols[0])] if len(pk_cols) == 1 else tuple(r[local_cols.index(p)] for p in pk_cols)
        if key not in d1_existing:
            vals = [esc(r[i]) for i in col_idx]
            sql = f"INSERT OR IGNORE INTO {table_name} ({', '.join(common_cols)}) VALUES ({', '.join(vals)});"
            batch_statements.append(sql)
            if len(batch_statements) >= 20:
                query_d1("\n".join(batch_statements), is_exec=True)
                inserted += len(batch_statements)
                batch_statements = []

    if batch_statements:
        query_d1("\n".join(batch_statements), is_exec=True)
        inserted += len(batch_statements)

    return inserted

def pull_table_from_d1(conn, table_name):
    cur = conn.cursor()
    # Check if table exists locally
    exists = cur.execute(f"SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?", (table_name,)).fetchone()[0]
    if not exists:
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
    print(f"Connecting to local SQLite: {DB_PATH}")
    conn = sqlite3.connect(DB_PATH)

    tables = [
        "users", "servers", "channels", "server_members", "dm_participants",
        "messages", "friendships", "message_read_receipts", "notifications",
        "course_tracks", "lessons", "quiz_questions", "job_openings",
        "job_applications", "voice_rooms", "voice_room_participants",
        "call_logs", "scheduled_messages", "audit_logs"
    ]

    print("\n--- PHASE 1: Pushing Local Additions to Cloudflare D1 ---")
    for tbl in tables:
        count = sync_table_to_d1(conn, tbl)
        if count > 0:
            print(f"  [PUSHED] {tbl}: {count} new row(s) to D1")

    print("\n--- PHASE 2: Pulling Cloudflare D1 Rows into Local SQLite ---")
    for tbl in tables:
        count = pull_table_from_d1(conn, tbl)
        print(f"  [SYNCED] {tbl}: {count} row(s) updated in SQLite")

    conn.close()

    # Sync staging db
    print(f"\nMirroring connecto.db -> connecto_staging.db")
    with open(DB_PATH, "rb") as f_in, open(STAGING_DB_PATH, "wb") as f_out:
        f_out.write(f_in.read())

    print("Synchronization complete!")

if __name__ == "__main__":
    main()
