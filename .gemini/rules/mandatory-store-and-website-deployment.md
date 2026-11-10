# Mandatory Deployment Rule: Our Store and Website Deployment After Every Application Change

> **CRITICAL & NON-NEGOTIABLE POLICY:**
> FOR EVERY IMPLEMENTATION, BUG FIX, CODE CHANGE, OR FEATURE UPDATE IN THE APPLICATION (CONNECTO, OUR STORE, OR ECOSYSTEM APPS), YOU MUST AUTOMATICALLY COMPILE, PUBLISH, AND DEPLOY THE LATEST VERSION IN **BOTH OUR STORE AND THE WEBSITE** WITHOUT REQUIRING EXPLICIT USER REMINDERS OR PROMPTING.

---

## 1. Scope & Automatic Trigger Conditions

This rule triggers automatically and unconditionally whenever:
1. Any code, asset, UI layout, security rule, or configuration change is made to **Connecto** (`android/app/`).
2. Any code, asset, UI layout, or configuration change is made to **Our Store** (`store-app/app/`).
3. Any change is made to backend APIs, edge endpoints, or website interfaces supporting these apps.

---

## 2. Mandatory Dual-Deployment Execution Pipeline

Whenever the trigger condition is met, execute the following steps in sequence:

### Step 1: Versioning & Change Recording
- In `app/build.gradle.kts`:
  - Advance `versionCode` (e.g. `118 -> 119`).
  - Advance `versionName` (e.g. `"4.6.29" -> "4.6.30"`).
- In `backend/app/api/v1/store.py`:
  - Update `version_name`, `version_code`, `size_bytes`, `size_formatted`.
  - Add descriptive, user-friendly changelog bullets summarizing the exact change.

### Step 2: Production Release Build
- Environment:
  ```bash
  export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
  ```
- Build Connecto:
  ```bash
  cd /Users/madarauchiha/connecto/android && ./gradlew assembleRelease --no-daemon
  ```
- Build Our Store (when modified):
  ```bash
  cd /Users/madarauchiha/connecto/store-app && ./gradlew assembleRelease --no-daemon
  ```

### Step 3: Fast CDN Binary Staging (GitHub Releases)
- Upload the release APKs directly to the active GitHub release:
  ```bash
  cd /Users/madarauchiha/connecto
  gh release upload <active_release_tag> android/app/build/outputs/apk/release/app-release.apk#connecto-fun.apk --clobber
  ```
- Also stage in backend local downloads directory for direct fallback:
  ```bash
  cp android/app/build/outputs/apk/release/app-release.apk backend/static/downloads/connecto-fun.apk
  cp store-app/app/build/outputs/apk/release/app-release.apk backend/static/downloads/our-store.apk
  ```

### Step 4: Deploy to the Website
- Deploy the updated website assets to Cloudflare Pages:
  ```bash
  cd /Users/madarauchiha/connecto/website
  # Ensure large binary files (>25MB) are removed from website/static/downloads/ so Cloudflare Pages deploys via _redirects
  rm -f static/downloads/connecto-fun.apk
  npx wrangler pages deploy static --project-name=connecto-web --commit-dirty=true
  ```

### Step 5: Deploy to Our Store Ecosystem
- Synchronize Our Store catalog in `backend/app/api/v1/store.py`.
- If edge worker routes are modified, deploy edge worker:
  ```bash
  cd /Users/madarauchiha/connecto/cloudflare && npx wrangler deploy
  ```

### Step 6: Git Commit & Push
- Commit all code, metadata, and asset changes to git and push to GitHub `main`:
  ```bash
  cd /Users/madarauchiha/connecto
  git add android/ store-app/ website/ backend/
  git commit -m "feat/fix: <description> and deploy to Our Store and Website"
  git push origin main
  ```

---

## 3. Post-Deployment Verification Requirement
Before closing any conversation turn or claiming completion:
1. Verify Cloudflare Pages deployment URL is active and healthy.
2. Verify GitHub Release asset has uploaded with non-zero byte size.
3. Verify Our Store API (`https://connecto.fun/api/v1/store/apps`) serves the latest version and changelog.
