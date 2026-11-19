-- Migration 0003: Per-User Soft-Clear Schema (user_conversation_state)
CREATE TABLE IF NOT EXISTS user_conversation_state (
    user_id TEXT NOT NULL,
    conversation_id TEXT NOT NULL,
    hidden_from_inbox INTEGER NOT NULL DEFAULT 0,
    cleared_at TEXT DEFAULT NULL,
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (user_id, conversation_id)
);

CREATE INDEX IF NOT EXISTS idx_ucs_user ON user_conversation_state(user_id);
CREATE INDEX IF NOT EXISTS idx_ucs_conv ON user_conversation_state(conversation_id);
