-- ==============================================================================
-- Connecto Cloudflare D1 Database Schema
-- Unified Relational Schema for Website, Mobile App & Edge Services
-- Database: connecto-db (SQLite / D1 compatible)
-- ==============================================================================

PRAGMA foreign_keys = ON;

-- ------------------------------------------------------------------------------
-- 1. USERS & AUTHENTICATION
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    avatar_url TEXT DEFAULT NULL,
    banner_url TEXT DEFAULT NULL,
    fcm_token TEXT DEFAULT NULL,
    bio TEXT DEFAULT '',
    username_changed INTEGER NOT NULL DEFAULT 0,
    is_recruiter INTEGER NOT NULL DEFAULT 0,
    is_admin INTEGER NOT NULL DEFAULT 0,
    is_stealth INTEGER NOT NULL DEFAULT 0,
    is_online INTEGER NOT NULL DEFAULT 1,
    phone_number TEXT DEFAULT NULL,
    full_name TEXT DEFAULT '',
    date_of_birth TEXT DEFAULT NULL,
    gender TEXT DEFAULT NULL,
    location TEXT DEFAULT NULL,
    two_factor_enabled INTEGER NOT NULL DEFAULT 0,
    two_factor_method TEXT NOT NULL DEFAULT 'sms',
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_presence ON users(is_online, is_stealth);

-- Active user login sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token_hash TEXT NOT NULL UNIQUE,
    ip_address TEXT DEFAULT NULL,
    user_agent TEXT DEFAULT NULL,
    expires_at TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(session_token_hash);

-- Multi-Factor & Verification OTPs
CREATE TABLE IF NOT EXISTS otp_verifications (
    id TEXT PRIMARY KEY,
    user_id TEXT REFERENCES users(id) ON DELETE CASCADE,
    identifier TEXT NOT NULL,
    otp_code TEXT NOT NULL,
    purpose TEXT NOT NULL DEFAULT 'forgot_password',
    method TEXT NOT NULL DEFAULT 'sms',
    attempts INTEGER NOT NULL DEFAULT 0,
    expires_at TEXT NOT NULL,
    is_verified INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_otp_identifier ON otp_verifications(identifier);
CREATE INDEX IF NOT EXISTS idx_otp_expires ON otp_verifications(expires_at);
CREATE INDEX IF NOT EXISTS idx_otp_user_id ON otp_verifications(user_id);

-- ------------------------------------------------------------------------------
-- 2. SERVERS, CHANNELS, MEMBERS & REAL-TIME CHAT
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS servers (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    icon_url TEXT DEFAULT NULL,
    owner_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_servers_owner_id ON servers(owner_id);

CREATE TABLE IF NOT EXISTS channels (
    id TEXT PRIMARY KEY,
    server_id TEXT DEFAULT NULL REFERENCES servers(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'text',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_channels_server_id ON channels(server_id);
CREATE INDEX IF NOT EXISTS idx_channels_name ON channels(name);

CREATE TABLE IF NOT EXISTS server_members (
    server_id TEXT NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'member',
    joined_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (server_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_server_members_user ON server_members(user_id);

CREATE TABLE IF NOT EXISTS dm_participants (
    channel_id TEXT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (channel_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_dm_participants_user ON dm_participants(user_id);

CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY,
    channel_id TEXT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    sender_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    attachments TEXT DEFAULT '[]',
    nonce TEXT DEFAULT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_messages_channel_created ON messages(channel_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_nonce ON messages(nonce);

CREATE TABLE IF NOT EXISTS friendships (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'pending',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_friendship_pair ON friendships(user_id, friend_id);
CREATE INDEX IF NOT EXISTS idx_friendship_user_status ON friendships(user_id, status);
CREATE INDEX IF NOT EXISTS idx_friendship_friend_status ON friendships(friend_id, status);

CREATE TABLE IF NOT EXISTS message_read_receipts (
    message_id TEXT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    read_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (message_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_read_receipts_user ON message_read_receipts(user_id);

CREATE TABLE IF NOT EXISTS dm_read_states (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel_id TEXT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    last_read_at TEXT NOT NULL DEFAULT (datetime('now')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE(user_id, channel_id)
);

CREATE INDEX IF NOT EXISTS idx_dm_read_user_channel ON dm_read_states(user_id, channel_id);

-- ------------------------------------------------------------------------------
-- 3. NOTIFICATIONS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    sender_username TEXT DEFAULT NULL,
    sender_avatar TEXT DEFAULT NULL,
    reference_id TEXT DEFAULT NULL,
    is_read INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications(created_at DESC);

-- ------------------------------------------------------------------------------
-- 4. ACADEMY & GAMIFIED LEARNING
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS course_tracks (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    icon TEXT NOT NULL DEFAULT 'Shield',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS lessons (
    id TEXT PRIMARY KEY,
    track_id TEXT NOT NULL REFERENCES course_tracks(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    content_markdown TEXT NOT NULL,
    xp_reward INTEGER NOT NULL DEFAULT 100,
    order_index INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_lessons_track_id ON lessons(track_id);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id TEXT PRIMARY KEY,
    lesson_id TEXT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_option_index INTEGER NOT NULL,
    explanation TEXT DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_quiz_lesson_id ON quiz_questions(lesson_id);

CREATE TABLE IF NOT EXISTS user_progress (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id TEXT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed INTEGER NOT NULL DEFAULT 1,
    score_percentage INTEGER NOT NULL DEFAULT 100,
    xp_earned INTEGER NOT NULL DEFAULT 100,
    completed_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE(user_id, lesson_id)
);

CREATE INDEX IF NOT EXISTS idx_user_progress_user ON user_progress(user_id);

CREATE TABLE IF NOT EXISTS user_academy_profiles (
    user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_xp INTEGER NOT NULL DEFAULT 0,
    current_rank TEXT NOT NULL DEFAULT 'Security Apprentice',
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- ------------------------------------------------------------------------------
-- 5. CAREERS & RECRUITMENT ATS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS job_openings (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    department TEXT NOT NULL DEFAULT 'Cybersecurity',
    location TEXT NOT NULL DEFAULT 'Remote / On-site',
    description TEXT NOT NULL,
    required_keywords TEXT NOT NULL DEFAULT '[]',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS job_applications (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL REFERENCES job_openings(id) ON DELETE CASCADE,
    applicant_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL,
    cv_file_url TEXT NOT NULL,
    parsed_text TEXT NOT NULL,
    ai_score INTEGER NOT NULL DEFAULT 0,
    detected_skills TEXT NOT NULL DEFAULT '[]',
    summary TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_job_app_job_id ON job_applications(job_id);
CREATE INDEX IF NOT EXISTS idx_job_app_applicant_id ON job_applications(applicant_id);

-- ------------------------------------------------------------------------------
-- 6. VOICE ROOMS & CALL LOGS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS voice_rooms (
    id TEXT PRIMARY KEY,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    topic TEXT NOT NULL DEFAULT 'Tactical voice comms',
    icon TEXT NOT NULL DEFAULT '⚔️',
    creator_id TEXT DEFAULT NULL,
    creator_username TEXT NOT NULL DEFAULT 'Gamer',
    max_participants INTEGER NOT NULL DEFAULT 8,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_voice_rooms_code ON voice_rooms(code);

CREATE TABLE IF NOT EXISTS voice_room_participants (
    id TEXT PRIMARY KEY,
    room_id TEXT NOT NULL REFERENCES voice_rooms(id) ON DELETE CASCADE,
    user_id TEXT DEFAULT NULL,
    username TEXT NOT NULL,
    nickname TEXT DEFAULT NULL,
    avatar TEXT DEFAULT NULL,
    rank TEXT NOT NULL DEFAULT 'Shinobi',
    muted INTEGER NOT NULL DEFAULT 0,
    speaking INTEGER NOT NULL DEFAULT 0,
    joined_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_vrp_room_id ON voice_room_participants(room_id);

CREATE TABLE IF NOT EXISTS call_logs (
    id TEXT PRIMARY KEY,
    user_id TEXT DEFAULT NULL,
    username TEXT NOT NULL,
    caller_name TEXT NOT NULL,
    call_type TEXT NOT NULL DEFAULT 'Voice Call',
    room_name TEXT DEFAULT NULL,
    room_code TEXT DEFAULT NULL,
    duration_seconds INTEGER NOT NULL DEFAULT 0,
    is_missed INTEGER NOT NULL DEFAULT 0,
    is_outgoing INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_call_logs_username ON call_logs(username);
CREATE INDEX IF NOT EXISTS idx_call_logs_created ON call_logs(created_at DESC);

-- ------------------------------------------------------------------------------
-- 7. SCHEDULED MESSAGES, AUDIT LOGS, BOOKMARKS & REFERRALS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scheduled_messages (
    id TEXT PRIMARY KEY,
    channel_id TEXT NOT NULL DEFAULT 'general',
    author TEXT NOT NULL,
    avatar TEXT DEFAULT '👤',
    content TEXT NOT NULL,
    delivery_time_epoch REAL NOT NULL,
    delivery_time_str TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_scheduled_channel_delivery ON scheduled_messages(channel_id, delivery_time_epoch, status);
CREATE INDEX IF NOT EXISTS idx_scheduled_author_status ON scheduled_messages(author, status);

CREATE TABLE IF NOT EXISTS audit_logs (
    id TEXT PRIMARY KEY,
    action TEXT NOT NULL,
    actor TEXT NOT NULL,
    target TEXT DEFAULT '',
    details TEXT DEFAULT '',
    ip_address TEXT DEFAULT '',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_audit_action_time ON audit_logs(action, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor_time ON audit_logs(actor, created_at DESC);

CREATE TABLE IF NOT EXISTS bookmarks (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    message_id TEXT NOT NULL,
    channel_id TEXT DEFAULT 'general',
    notes TEXT DEFAULT '',
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE(user_id, message_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmark_user_time ON bookmarks(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS referrals (
    id TEXT PRIMARY KEY,
    referrer_handle TEXT NOT NULL,
    candidate_id TEXT NOT NULL,
    milestone TEXT NOT NULL DEFAULT 'applied',
    xp_awarded INTEGER NOT NULL DEFAULT 100,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE(referrer_handle, candidate_id, milestone)
);

CREATE INDEX IF NOT EXISTS idx_referral_referrer_time ON referrals(referrer_handle, created_at DESC);

CREATE TABLE IF NOT EXISTS moderation_reports (
    id TEXT PRIMARY KEY,
    message_id TEXT DEFAULT NULL,
    channel_id TEXT DEFAULT 'general',
    message_author TEXT DEFAULT 'unknown',
    message_content TEXT DEFAULT '',
    reporter TEXT NOT NULL DEFAULT 'guest',
    reason TEXT NOT NULL DEFAULT 'other',
    notes TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'pending',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_mod_report_status ON moderation_reports(status);
