-- Migration 0002: User Deleted Messages & Seen-Based Unsend / Local Deletes
CREATE TABLE IF NOT EXISTS user_deleted_messages (
    user_id TEXT NOT NULL,
    message_id TEXT NOT NULL,
    conversation_id TEXT DEFAULT NULL,
    deleted_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (user_id, message_id)
);

CREATE INDEX IF NOT EXISTS idx_user_deleted_conv ON user_deleted_messages(user_id, conversation_id);
CREATE INDEX IF NOT EXISTS idx_user_deleted_msg ON user_deleted_messages(message_id);

CREATE TABLE IF NOT EXISTS message_deletions (
    message_id TEXT NOT NULL,
    username TEXT NOT NULL,
    PRIMARY KEY (message_id, username)
);

-- Ensure read_at exists on messages
ALTER TABLE messages ADD COLUMN read_at TEXT DEFAULT NULL;
