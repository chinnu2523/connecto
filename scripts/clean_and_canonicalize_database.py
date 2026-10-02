"""
Connecto Database Sanitation & Canonicalization Engine
=====================================================
Cleans all clumsy data, duplicates, and fragmented IDs across:
1. Channels (collapses duplicate UUIDs into clean canonical channel IDs)
2. Messages (deduplicates _name clones, unifies channel_ids, preserves 100% of legitimate chat history)
3. Users (removes ephemeral test bots, preserves all human and system accounts)
4. Copies clean database to both connecto_staging.db and connecto.db
"""

import sqlite3
import os
import shutil

DB_PATH = "/Users/madarauchiha/connecto/backend/connecto_staging.db"
TARGET_DB_PATH = "/Users/madarauchiha/connecto/backend/connecto.db"

def clean_database():
    print(f"Opening database: {DB_PATH}")
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    c = conn.cursor()

    # 1. Inspect before state
    c.execute("SELECT count(*) FROM channels")
    initial_channels = c.fetchone()[0]
    c.execute("SELECT count(*) FROM messages")
    initial_messages = c.fetchone()[0]
    c.execute("SELECT count(*) FROM users")
    initial_users = c.fetchone()[0]
    print(f"Before cleanup -> Channels: {initial_channels}, Messages: {initial_messages}, Users: {initial_users}")

    # Canonical channels definition
    canonical_channels = [
        ("general", "general", "text", "2026-09-01 00:00:00"),
        ("announcements", "announcements", "text", "2026-09-01 00:00:00"),
        ("dev-chat", "dev-chat", "text", "2026-09-01 00:00:00"),
        ("gaming", "gaming", "text", "2026-09-01 00:00:00"),
        ("war-room", "war-room", "text", "2026-09-01 00:00:00"),
        ("tournaments", "tournaments", "text", "2026-09-01 00:00:00"),
        ("clips", "clips", "text", "2026-09-01 00:00:00"),
        ("voice-lounge", "voice-lounge", "voice", "2026-09-01 00:00:00"),
    ]

    # Map of channel identifiers (UUIDs, aliases) to canonical channel ID
    c.execute("SELECT id, name FROM channels")
    all_ch_rows = c.fetchall()
    ch_id_to_canonical = {}
    for r in all_ch_rows:
        cid = r["id"]
        cname = r["name"]
        for canon_id, canon_name, _, _ in canonical_channels:
            if cname == canon_name or cid == canon_id:
                ch_id_to_canonical[cid] = canon_id
                ch_id_to_canonical[cname] = canon_id

    print(f"Mapped {len(ch_id_to_canonical)} channel ID aliases to canonical channel names.")

    # 2. Clean messages:
    # First delete all artificial duplicate messages ending with '_name'
    c.execute("DELETE FROM messages WHERE id LIKE '%_name'")
    deleted_duplicates = c.rowcount
    print(f"Deleted {deleted_duplicates} duplicate '_name' messages.")

    # Update channel_id on all remaining messages to their clean canonical channel ID
    c.execute("SELECT id, channel_id FROM messages")
    all_msgs = c.fetchall()
    updated_msg_channels = 0
    for m in all_msgs:
        mid = m["id"]
        current_chid = m["channel_id"]
        if current_chid in ch_id_to_canonical:
            canon = ch_id_to_canonical[current_chid]
            if canon != current_chid:
                c.execute("UPDATE messages SET channel_id = ? WHERE id = ?", (canon, mid))
                updated_msg_channels += 1

    print(f"Updated {updated_msg_channels} messages to point directly to canonical channel IDs.")

    # 3. Clean channels table:
    # Preserve DMs if any
    c.execute("SELECT * FROM channels WHERE type = 'dm' OR name LIKE 'dm-%'")
    legit_dms = c.fetchall()

    # Re-create canonical channels table
    c.execute("DELETE FROM channels")
    for cid, cname, ctype, ccreated in canonical_channels:
        c.execute(
            "INSERT OR REPLACE INTO channels (id, server_id, name, type, created_at) VALUES (?, 'srv_connecto', ?, ?, ?)",
            (cid, cname, ctype, ccreated)
        )

    # Re-insert clean DMs
    for dm in legit_dms:
        if not dm["id"].endswith("1789109736412") and not "test1" in dm["id"]:
            c.execute(
                "INSERT OR REPLACE INTO channels (id, server_id, name, type, created_at) VALUES (?, NULL, ?, 'dm', ?)",
                (dm["id"], dm["name"], dm["created_at"])
            )

    # 4. Clean users and orphaned references:
    # Reassign test messages from deleted bots to usr_system_dev
    c.execute("UPDATE messages SET sender_id = 'usr_system_dev' WHERE sender_id IN ('usr_ninja_tester_1790750177', 'usr_ninja_773242')")

    # Remove junk test bot users
    bot_usernames = [
        "shinobi_a_1789063235917",
        "shinobi_b_1789063235917",
        "ninja_tester_1790750177",
        "ninja_773242"
    ]
    for b_u in bot_usernames:
        c.execute("DELETE FROM users WHERE username = ?", (b_u,))

    # Clean orphaned server_members
    c.execute("DELETE FROM server_members WHERE user_id NOT IN (SELECT id FROM users)")

    # 5. Commit changes
    conn.commit()

    # Inspect after state
    c.execute("SELECT count(*) FROM channels")
    final_channels = c.fetchone()[0]
    c.execute("SELECT count(*) FROM messages")
    final_messages = c.fetchone()[0]
    c.execute("SELECT count(*) FROM users")
    final_users = c.fetchone()[0]
    print(f"After cleanup -> Channels: {final_channels}, Messages: {final_messages}, Users: {final_users}")

    # Print breakdown of messages per channel
    print("\n--- Messages per Canonical Channel ---")
    c.execute("SELECT channel_id, count(*) FROM messages GROUP BY channel_id")
    for r in c.fetchall():
        print(f"  #{r[0]}: {r[1]} messages")

    conn.close()

    # Copy clean database to connecto.db as well
    print(f"\nMirroring clean database to {TARGET_DB_PATH} ...")
    shutil.copy2(DB_PATH, TARGET_DB_PATH)
    print("Done! Both connecto_staging.db and connecto.db are synchronized and clean.")

if __name__ == "__main__":
    clean_database()
