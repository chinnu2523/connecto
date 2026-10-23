-- SEC-05 FIX: Add per-user E2E secret salt column
-- This column stores a server-generated random secret used as HMAC key in
-- ConnectoE2EEncryption.deriveConversationKey() so that conversation keys
-- cannot be derived from usernames alone.
-- Generated lazily on first call to GET /api/v1/e2e/secret
ALTER TABLE users ADD COLUMN e2e_secret TEXT DEFAULT NULL;
