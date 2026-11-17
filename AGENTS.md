# Antigravity Agent Rules for Connecto

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

---

## 2. Mandatory Post-Change Deployment Protocol for Connecto App & Our Store

### CRITICAL MANDATE:
**AFTER EVERY UPDATE, CODE CHANGE, BUG FIX, OR FEATURE IMPLEMENTATION RELATING TO THE CONNECTO APPLICATION OR OUR STORE APPLICATION, YOU MUST IMMEDIATELY COMPILE, PUBLISH, AND DEPLOY THE UPDATED APPS ACROSS THE WEBSITE AND THE OUR STORE ECOSYSTEM WITHOUT REQUIRING EXPLICIT USER REMINDERS.**

### Mandatory Dual-Deployment Execution Checklist:

1. **Compile Release APKs**:
   - Compile production release APKs with Java 17+ (`JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleRelease --no-daemon`).
2. **Deploy to GitHub Release Assets (Edge Binary Delivery)**:
   - Upload new APKs to active GitHub release (`v4.6.11`) via `gh release upload v4.6.11 <apk_path>#<asset_name> --clobber`.
3. **Deploy to Website (`https://connecto.fun`)**:
   - Update `website/static/index.html` and `website/static/store.html` with new version, build, size, and changelog.
   - Deploy Cloudflare Pages: `cd /Users/madarauchiha/connecto && npx wrangler pages deploy website/static --project-name connecto-web`.
   - Update `cloudflare/worker.js` and deploy Cloudflare Worker: `cd /Users/madarauchiha/connecto/cloudflare && npx wrangler deploy`.
   - Update D1 database release record in `app_releases` table (`is_latest = 1`).
4. **Deploy to Our Store Application Ecosystem**:
   - Verify `/api/v1/store/apps` serves updated `version_name`, `version_code`, `download_url`, and `changelog`.
   - Verify Our Store self-update and in-app auto-update flows.
5. **On-Device Verification**:
   - Install APK via `adb install -r` on connected emulator/device and verify with `dumpsys package`.
   - Capture 1080x2400 screenshot proofs.
6. **Commit & Push Repositories**:
   - Ensure all changes across `connecto` and `connecto-store` repositories are committed and pushed.

---

## 3. Mandatory Pruning & Deletion of Old Application Data, Releases & Artifacts

### CRITICAL MANDATE:
**ON EVERY APPLICATION RELEASE OR DEPLOYMENT CYCLE, YOU MUST AUTOMATICALLY DELETE AND PURGE ALL PREVIOUS/OLD APPLICATION RELEASES, OBSOLETE APKS, STALE DATABASE RECORDS, AND BUILD CACHES ACROSS GITHUB AND SERVER INFRASTRUCTURE.**

### Mandatory Pruning Checklist:
1. **GitHub Releases Pruning**:
   - Automatically delete all previous/older GitHub releases and tags, keeping ONLY the latest 2 releases (active + 1 fallback).
   - Run: `gh release delete <old_tag> --yes --cleanup-tag`.
2. **Cloudflare D1 Database Pruning (`connecto-db`)**:
   - Purge obsolete records in `app_releases`:
     `DELETE FROM app_releases WHERE is_latest = 0 AND id NOT IN (SELECT id FROM app_releases ORDER BY created_at DESC LIMIT 2);`
3. **Server Downloads & File Storage Pruning**:
   - Remove stale binaries (`backend/static/downloads/app-release.apk`, deprecated versions, temporary files).
   - Keep only current canonical binaries (`connecto-fun.apk`, `our-store.apk`, `pdf-studio.apk`).
4. **Automated Pruning Script**:
   - Run `/Users/madarauchiha/connecto/scripts/prune_old_data.sh 2` on every release cycle.


