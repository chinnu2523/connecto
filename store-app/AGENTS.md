# Antigravity Rules for Our Store

## 1. Strict Test Accounts Only & Zero User Account Mutation Rule

### CRITICAL MANDATE:
**YOU MUST USE ONLY DEDICATED TEST ACCOUNTS FOR ALL TESTING AND VERIFICATION. NEVER USE REAL USER ACCOUNTS FOR TESTING, AND NEVER MAKE ANY CHANGES TO REAL USER ACCOUNTS UNDER ANY CIRCUMSTANCES.**

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
