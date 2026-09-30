#!/usr/bin/env python3
"""
Connecto Production Database Automated Backup Engine
- Performs non-blocking SQLite Online Backup of connecto_staging.db
- Validates database PRAGMA integrity_check before persisting
- Compresses snapshots with gzip to save disk space
- Enforces strict chmod 600 permissions
- Automatically prunes snapshots older than 14 days
"""
import os
import sys
import time
import glob
import gzip
import sqlite3
import shutil
from datetime import datetime, timezone

APP_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SQLITE_DB = os.path.join(APP_ROOT, "connecto_staging.db")
JSON_DB = os.path.join(APP_ROOT, "data", "app_database.json")
BACKUPS_DIR = os.path.join(APP_ROOT, "backups")
LOGS_DIR = os.path.join(APP_ROOT, "logs")

RETENTION_DAYS = 14

def run_backup():
    now_iso = datetime.now(timezone.utc).isoformat()
    now_str = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
    print(f"[{now_iso}] Starting Connecto Automated Production Database Backup...")

    os.makedirs(BACKUPS_DIR, exist_ok=True)
    os.makedirs(LOGS_DIR, exist_ok=True)
    try:
        os.chmod(BACKUPS_DIR, 0o700)
    except Exception:
        pass

    if not os.path.exists(SQLITE_DB):
        print(f"ERROR: Primary SQLite database '{SQLITE_DB}' does not exist.")
        sys.exit(1)

    temp_backup_path = os.path.join(BACKUPS_DIR, f"temp_backup_{now_str}.db")
    final_gz_path = os.path.join(BACKUPS_DIR, f"connecto_sqlite_backup_{now_str}.db.gz")

    try:
        # 1. Non-blocking SQLite online backup API
        print("Executing non-blocking SQLite online backup...")
        src_conn = sqlite3.connect(f"file:{SQLITE_DB}?mode=ro", uri=True)
        dest_conn = sqlite3.connect(temp_backup_path)
        with dest_conn:
            src_conn.backup(dest_conn, pages=100)
        src_conn.close()

        # 2. Validate backup integrity
        cur = dest_conn.cursor()
        integrity_res = cur.execute("PRAGMA integrity_check;").fetchone()
        fk_violations = cur.execute("PRAGMA foreign_key_check;").fetchall()
        user_count = cur.execute("SELECT count(*) FROM users;").fetchone()[0]
        msg_count = cur.execute("SELECT count(*) FROM messages;").fetchone()[0]
        dest_conn.close()

        if not integrity_res or integrity_res[0] != "ok":
            raise RuntimeError(f"Backup integrity check failed: {integrity_res}")
        if fk_violations:
            print(f"WARNING: FK violations in backup: {fk_violations}")

        print(f"Integrity verified: OK (Users: {user_count}, Messages: {msg_count})")

        # 3. Compress with gzip
        with open(temp_backup_path, "rb") as f_in:
            with gzip.open(final_gz_path, "wb", compresslevel=9) as f_out:
                shutil.copyfileobj(f_in, f_out)

        os.remove(temp_backup_path)
        os.chmod(final_gz_path, 0o600)
        file_size_kb = os.path.getsize(final_gz_path) / 1024.0
        print(f"✅ SQLite compressed backup created: {final_gz_path} ({file_size_kb:.2f} KB, chmod 600)")

        # 4. Also snapshot legacy JSON if present
        if os.path.exists(JSON_DB):
            json_gz_path = os.path.join(BACKUPS_DIR, f"connecto_json_backup_{now_str}.json.gz")
            with open(JSON_DB, "rb") as f_in:
                with gzip.open(json_gz_path, "wb", compresslevel=9) as f_out:
                    shutil.copyfileobj(f_in, f_out)
            os.chmod(json_gz_path, 0o600)
            print(f"✅ JSON backup snapshot created: {json_gz_path}")

        # 5. Prune backups older than retention window
        cutoff_epoch = time.time() - (RETENTION_DAYS * 86400)
        pruned_count = 0
        all_backups = glob.glob(os.path.join(BACKUPS_DIR, "connecto_*_backup_*.gz"))
        for bfile in all_backups:
            try:
                if os.path.getmtime(bfile) < cutoff_epoch:
                    os.remove(bfile)
                    pruned_count += 1
            except Exception as e:
                print(f"Warning: could not prune old backup {bfile}: {e}")

        print(f"Pruned {pruned_count} backups older than {RETENTION_DAYS} days. Active backups: {len(all_backups) - pruned_count}")
        print("Database backup completed successfully.")

    except Exception as exc:
        print(f"FATAL: Database backup failed: {exc}")
        if os.path.exists(temp_backup_path):
            try: os.remove(temp_backup_path)
            except Exception: pass
        sys.exit(1)

if __name__ == "__main__":
    run_backup()
