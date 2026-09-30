import sqlite3
from datetime import datetime, timezone
import sys
sys.path.append('/srv/apps/connecto-app')
from app.core.security import hash_password

con = sqlite3.connect('/srv/apps/connecto-app/connecto_staging.db')
con.row_factory = sqlite3.Row
cur = con.cursor()
now_iso = datetime.now(timezone.utc).isoformat()

missing_users = [
    {
        'id': 'usr_madara',
        'username': 'vance',
        'display_name': 'Vance Sterling',
        'email': 'vance@connecto.fun',
        'password_hash': '$argon2id$v=19$m=65536,t=3,p=4$uekXUeMCwnVd2ZH3+yGeQg$pJuFeQeqPrGlrwep0hn3ky/vYigpvy8aJQ1A6XDc1aE',
        'avatar_url': '🔥',
        'bio': 'Wake Up to Reality. Nothing ever goes as planned in this accursed world.',
        'is_admin': 1,
        'phone_number': '+919876543210'
    },
    {
        'id': 'usr_sakura',
        'username': 'nova',
        'display_name': 'Nova Ray',
        'email': 'nova@connecto.fun',
        'password_hash': '$argon2id$v=19$m=65536,t=3,p=4$m6ZVIVBAeLAK53zI1U4/gQ$vXJU3bdgoZ+MVqg5brH/MOTXndswCIS7xUTa0R+0uFg',
        'avatar_url': '🌸',
        'bio': 'Medical Ninjutsu Specialist',
        'is_admin': 0,
        'phone_number': None
    },
    {
        'id': 'usr_naruto',
        'username': 'phoenix',
        'display_name': 'Phoenix Cole',
        'email': 'phoenix@connecto.fun',
        'password_hash': '$argon2id$v=19$m=65536,t=3,p=4$m6ZVIVBAeLAK53zI1U4/gQ$vXJU3bdgoZ+MVqg5brH/MOTXndswCIS7xUTa0R+0uFg',
        'avatar_url': '🍃',
        'bio': 'Vanguard Tactician',
        'is_admin': 0,
        'phone_number': None
    },
    {
        'id': 'usr_sasuke',
        'username': 'cipher',
        'display_name': 'Cipher Kage',
        'email': 'cipher@connecto.fun',
        'password_hash': '$argon2id$v=19$m=65536,t=3,p=4$m6ZVIVBAeLAK53zI1U4/gQ$vXJU3bdgoZ+MVqg5brH/MOTXndswCIS7xUTa0R+0uFg',
        'avatar_url': '⚡',
        'bio': 'Avenger / Shadow Shinobi',
        'is_admin': 0,
        'phone_number': None
    }
]

for u in missing_users:
    cur.execute('SELECT id FROM users WHERE id=? OR username=?', (u['id'], u['username']))
    existing = cur.fetchone()
    if not existing:
        cur.execute('''
            INSERT INTO users (
                id, username, display_name, email, password_hash, avatar_url, bio,
                username_changed, is_recruiter, is_admin, is_stealth, is_online,
                phone_number, two_factor_enabled, two_factor_method, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, ?, 0, 1, ?, 0, 'email', ?, ?)
        ''', (
            u['id'], u['username'], u['display_name'], u['email'], u['password_hash'],
            u['avatar_url'], u['bio'], u['is_admin'], u['phone_number'], now_iso, now_iso
        ))
        print(f"Restored user: {u['username']}")
    else:
        print(f"User already exists: {u['username']}")

    cur.execute("INSERT OR IGNORE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', ?, 'member', ?)", (u['id'], now_iso))

p_hash = hash_password('Password123!')

cur.execute("UPDATE users SET two_factor_enabled=0, password_hash=? WHERE username='app_alex'", (p_hash,))
cur.execute("UPDATE users SET two_factor_enabled=0, password_hash=? WHERE username='connecto_admin'", (p_hash,))
cur.execute("UPDATE users SET two_factor_enabled=0 WHERE username IN ('diag_user_sms', 'blade_shinobi', 'shadow_hayate')")

con.commit()
print('Database updates committed successfully!')
