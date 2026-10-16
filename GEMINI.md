# Antigravity Rules for Connecto

## 1. Testing and Verification Account Safety Rule
**CRITICAL PROTOCOL: DO NOT USE USER ACCOUNTS FOR TESTING OR VERIFICATION.**

- **Strict Prohibition on Real User Accounts**:
  - Never use real user accounts (such as `chinnu14754x`, production users, or any other user's personal account) for:
    - Verification, testing, automated UI navigation, or login checks.
    - Modifying or resetting passwords, testing credential flows, or mutating account state.
    - Sending test messages or creating dummy conversations.
- **Dedicated Test / Guest Accounts Only**:
  - Always create or use dedicated temporary test accounts (e.g. `test_user_xxx`, `guest_test_xxx`) or guest sessions for verification and testing.
- **Mandatory Post-Test Cleanup**:
  - Once testing and verification are completed, the temporary test or guest accounts and all associated test data (sessions, messages, tokens, database entries) MUST be immediately and completely removed and deleted from the database and local storage.
