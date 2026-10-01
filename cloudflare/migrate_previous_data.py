import sqlite3
import os
import json
import re

backup_db_path = "/Users/madarauchiha/Desktop/connecto_backups/connecto_backup_20260909_215020.db"
staging_db_path = "/Users/madarauchiha/AndroidStudioProjects/Connecto2/backend/connecto_staging.db"

conn_backup = sqlite3.connect(backup_db_path)
conn_backup.row_factory = sqlite3.Row
cur_b = conn_backup.cursor()

conn_staging = sqlite3.connect(staging_db_path)
conn_staging.row_factory = sqlite3.Row
cur_s = conn_staging.cursor()

sql_statements = []

def esc(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    s = str(v).replace("'", "''")
    return f"'{s}'"

print("--- 1. Migrating Users ---")
users = {}
# From backup
for r in cur_b.execute("SELECT * FROM users").fetchall():
    uid = r["id"]
    users[r["username"].lower()] = {
        "id": uid,
        "username": r["username"],
        "display_name": r["display_name"],
        "email": r["email"],
        "password_hash": r["password_hash"],
        "avatar_url": r["avatar_url"] or "👾",
        "bio": r["bio"] or "Connecto Shinobi",
        "is_admin": r["is_admin"] if "is_admin" in r.keys() else 0,
        "is_online": 1
    }

# From staging
for r in cur_s.execute("SELECT * FROM users").fetchall():
    u = r["username"].lower()
    uid = r["id"]
    users[u] = {
        "id": uid,
        "username": r["username"],
        "display_name": r["display_name"],
        "email": r["email"],
        "password_hash": r["password_hash"],
        "avatar_url": r["avatar_url"] or "👾",
        "bio": r["bio"] or "Connecto Shinobi",
        "is_admin": r["is_admin"] if "is_admin" in r.keys() else 1,
        "is_online": 1
    }

# Also ensure alias users
if "chinnu" in users:
    users["chinnu"]["is_admin"] = 1
    users["chinnu"]["rank"] = "Hokage"

for u, data in users.items():
    sql = f"INSERT OR REPLACE INTO users (id, username, display_name, email, password_hash, avatar_url, bio, is_admin, is_online) VALUES ({esc(data['id'])}, {esc(data['username'])}, {esc(data['display_name'])}, {esc(data['email'])}, {esc(data['password_hash'])}, {esc(data['avatar_url'])}, {esc(data['bio'])}, {data['is_admin']}, 1);"
    sql_statements.append(sql)

print(f"Total users to migrate: {len(users)}")

print("--- 2. Migrating Channels ---")
channels = {}
for r in cur_b.execute("SELECT * FROM channels").fetchall():
    ch_id = r["name"] # use friendly name as ID for consistency
    raw_id = r["id"]
    channels[r["name"]] = {
        "id": r["name"],
        "raw_id": raw_id,
        "name": r["name"],
        "type": r["type"],
        "created_at": r["created_at"]
    }
    # Also keep raw UUID as channel mapping so foreign keys work!
    sql = f"INSERT OR REPLACE INTO channels (id, name, type, created_at) VALUES ({esc(raw_id)}, {esc(r['name'])}, {esc(r['type'])}, {esc(r['created_at'])});"
    sql_statements.append(sql)
    sql_name = f"INSERT OR REPLACE INTO channels (id, name, type, created_at) VALUES ({esc(r['name'])}, {esc(r['name'])}, {esc(r['type'])}, {esc(r['created_at'])});"
    sql_statements.append(sql_name)

print(f"Total channels to migrate: {len(channels)}")

print("--- 3. Migrating Messages ---")
b_messages = cur_b.execute("SELECT * FROM messages").fetchall()
for m in b_messages:
    mid = m["id"]
    ch_id = m["channel_id"]
    # find channel name
    target_ch = ch_id
    for c_name, c_data in channels.items():
        if c_data["raw_id"] == ch_id:
            target_ch = c_name
            break

    sender_id = m["sender_id"]
    content = m["content"]
    created_at = m["created_at"]
    
    # Insert with friendly channel name
    sql = f"INSERT OR REPLACE INTO messages (id, channel_id, sender_id, content, created_at) VALUES ({esc(mid)}, {esc(target_ch)}, {esc(sender_id)}, {esc(content)}, {esc(created_at)});"
    sql_statements.append(sql)

print(f"Total messages to migrate: {len(b_messages)}")

print("--- 4. Migrating Friendships ---")
# From staging
s_friends = cur_s.execute("SELECT * FROM friendships").fetchall()
for f in s_friends:
    sql = f"INSERT OR REPLACE INTO friendships (id, user_id, friend_id, status, created_at) VALUES ({esc(f['id'])}, {esc(f['user_id'])}, {esc(f['friend_id'])}, {esc(f['status'])}, {esc(f['created_at'])});"
    sql_statements.append(sql)

# Also create mutual friendships between chinnu and key shinobis
user_id_map = {data["username"]: data["id"] for data in users.values()}
chinnu_id = user_id_map.get("chinnu", "usr_chinnu")
for friend_name in ["vivek", "madara_legend", "elena_vance", "shinobi_99", "system_dev"]:
    f_id = user_id_map.get(friend_name)
    if f_id:
        pair1_id = f"f_{chinnu_id}_{f_id}"[:36]
        pair2_id = f"f_{f_id}_{chinnu_id}"[:36]
        sql1 = f"INSERT OR REPLACE INTO friendships (id, user_id, friend_id, status) VALUES ({esc(pair1_id)}, {esc(chinnu_id)}, {esc(f_id)}, 'accepted');"
        sql2 = f"INSERT OR REPLACE INTO friendships (id, user_id, friend_id, status) VALUES ({esc(pair2_id)}, {esc(f_id)}, {esc(chinnu_id)}, 'accepted');"
        sql_statements.append(sql1)
        sql_statements.append(sql2)

print("--- 5. Migrating Voice Rooms ---")
voice_rooms = [
    ("vr_lounge", "LOU-001", "Main Shinobi Lounge", "Casual voice chat and hangout", "🎙️", chinnu_id, "chinnu", 25),
    ("vr_war_room", "WAR-774", "Tactical War Room 774", "Clan tournament communications & high-speed tactics", "⛩️", user_id_map.get("madara_legend", chinnu_id), "madara_legend", 12),
    ("vr_dev", "DEV-101", "Devs Audio Stage", "Architecture sync and low-latency audio debugging", "🛠️", user_id_map.get("system_dev", chinnu_id), "system_dev", 20),
    ("vr_tournament", "TOURN-FF", "Tournament Battle Arena", "Free Fire and CS competitive stage", "🏆", user_id_map.get("connecto_admin", chinnu_id), "connecto_admin", 50)
]
for vr in voice_rooms:
    sql = f"INSERT OR REPLACE INTO voice_rooms (id, code, name, topic, icon, creator_id, creator_username, max_participants, is_active) VALUES ({esc(vr[0])}, {esc(vr[1])}, {esc(vr[2])}, {esc(vr[3])}, {esc(vr[4])}, {esc(vr[5])}, {esc(vr[6])}, {vr[7]}, 1);"
    sql_statements.append(sql)

print(f"Total SQL statements generated: {len(sql_statements)}")

with open("/Users/madarauchiha/connecto/cloudflare/restore_all_previous_data.sql", "w") as f:
    f.write("\n".join(sql_statements) + "\n")

print("Saved to /Users/madarauchiha/connecto/cloudflare/restore_all_previous_data.sql")
