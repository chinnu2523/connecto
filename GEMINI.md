# Antigravity Rules for Connecto

## 1. Strict Test Accounts Only & Zero User Account Mutation Rule

### CRITICAL MANDATE:
**YOU MUST USE ONLY DEDICATED TEST ACCOUNTS FOR ALL TESTING AND VERIFICATION. NEVER USE REAL USER ACCOUNTS FOR TESTING, AND NEVER MAKE ANY CHANGES TO REAL USER ACCOUNTS UNDER ANY CIRCUMSTANCES.**

---

### Mandatory Policy Requirements

1. **Absolute Prohibition on Real User Accounts for Testing**:
   - Never log into, navigate with, or use real user accounts (such as `chinnu14754x`, `srinu`, `connecto_admin`, or any actual personal/production user account) during manual or automated testing, QA flows, UI inspection, calling tests, or verification scripts.
   - All tests, verification runs, emulator interactions, and feature validations must be conducted exclusively using dedicated test accounts (e.g., `test_user_xxx`, `qa_test_xxx`, `guest_test_xxx`).

2. **Absolute Zero-Mutation Policy on Real User Accounts**:
   - **Never make ANY modifications, updates, or deletions to real user accounts or their records**, across local databases (`connecto.db`, `connecto_staging.db`), remote databases (Cloudflare D1, PostgreSQL), or device storage (`shared_prefs`).
   - Specifically prohibited actions on real user accounts include:
     - Changing, copying, or resetting passwords or password hashes.
     - Changing roles or permissions (e.g., toggling `is_admin`, `is_recruiter`, or administrative privileges).
     - Altering personal profile details (usernames, display names, avatars, bios, emails, phone numbers, date of birth, statuses).
     - Modifying, inserting, or deleting messages, friend relationships, channels, or call logs associated with real user accounts.
     - Overwriting or tampering with saved sessions, tokens, or credentials of real users.

3. **Dedicated Test Accounts Protocol**:
   - When testing requires authentication:
     - Use or provision designated test accounts with explicit test prefixes (e.g., `test_user_qa`, `test_member_01`, `test_admin_01`).
     - Treat test accounts as disposable, isolated sandboxes.
   - If a test admin account is needed, create or use a dedicated test admin account (e.g. `test_admin_qa`) rather than using real user admin accounts.

4. **Post-Testing Isolation & Cleanup**:
   - Any test data created during test executions must remain strictly segregated from real user data.
   - Temporary test accounts and disposable testing data may be cleaned up after verification without ever touching or modifying real user records.
