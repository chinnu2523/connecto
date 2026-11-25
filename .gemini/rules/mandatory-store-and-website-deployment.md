---
trigger: always_on
description: Mandatory post-change deployment rule requiring every Connecto/Our Store code change or update to be compiled, published, and deployed to both the Website and Our Store.
---

# Mandatory Deployment Rule: Our Store and Website Deployment After Every Change

> **CRITICAL & NON-NEGOTIABLE POLICY:**
> FOR EVERY CODE CHANGE, BUG FIX, OPTIMIZATION, OR FEATURE UPDATE IN THE APPLICATION (`android/app/`, `store-app/app/`, `cloudflare/`, `backend/`, OR `website/`), YOU MUST AUTOMATICALLY COMPILE, PUBLISH, AND DEPLOY THE LATEST VERSION IN **BOTH OUR STORE AND THE WEBSITE** WITHOUT WAITING FOR USER REMINDERS.

---

## 1. Scope & Automatic Trigger Conditions

This rule triggers automatically and unconditionally whenever:
1. Any code, asset, UI layout, security rule, or configuration change is made to **Connecto** (`android/app/`).
2. Any code, asset, UI layout, or configuration change is made to **Our Store** (`store-app/app/`).
3. Any change is made to backend APIs, Cloudflare Edge Worker (`cloudflare/worker.js`), or website interfaces (`website/static/`).

---

## 2. Mandatory Dual-Deployment Execution Pipeline

Whenever the trigger condition is met, execute the following steps in sequence:

### Step 1: Version Bump & Changelog Synchronization
- Increment `versionCode` and `versionName` in `android/app/build.gradle.kts` (and `store-app/app/build.gradle.kts` if modified).
- Update `version_name`, `version_code`, `size_bytes`, `size_formatted`, and `changelog` in:
  - `cloudflare/worker.js` (Our Store catalog + CDN `canonicalApkUrls`, `cacheKey`, `X-Connecto-Version`, `X-Connecto-Build`).
  - `backend/app/api/v1/store.py`.
- Update version badges in `website/static/index.html`, `website/static/terms.html`, and `website/static/privacy.html`.

### Step 2: Production Release Build
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
cd /Users/madarauchiha/connecto/android && ./gradlew assembleRelease --no-daemon
```
*(If `store-app/` was modified, also run `cd /Users/madarauchiha/connecto/store-app && ./gradlew assembleRelease --no-daemon`.)*

### Step 3: Fast CDN Binary Distribution (GitHub Releases)
Stage the exact filenames (`connecto-fun.apk`, `our-store.apk`) so GitHub Release asset names match CDN URLs, then create/upload to the release tag:
```bash
cd /Users/madarauchiha/connecto
cp android/app/build/outputs/apk/release/app-release.apk connecto-fun.apk
cp android/app/build/outputs/apk/release/app-release.apk backend/static/downloads/connecto-fun.apk
gh release create v<versionName> connecto-fun.apk --title "Connecto v<versionName>" --notes "<summary>"
# Or if updating an existing tag:
# gh release upload v<versionName> connecto-fun.apk --clobber
```
Update `size_bytes` in `cloudflare/worker.js` and `backend/app/api/v1/store.py` to match `stat -f%z connecto-fun.apk`, and bump the edge `cacheKey` in `cloudflare/worker.js` so Cloudflare does not serve a stale cached binary.

### Step 4: Deploy to the Website (Cloudflare Pages)
```bash
cd /Users/madarauchiha/connecto/website
rm -f static/downloads/connecto-fun.apk
npx wrangler pages deploy static --project-name=connecto-web --commit-dirty=true
```

### Step 5: Deploy to Our Store & Edge Gateway (Cloudflare Worker)
```bash
cd /Users/madarauchiha/connecto/cloudflare && npx wrangler deploy
```

### Step 6: Git Commit & Push
```bash
cd /Users/madarauchiha/connecto
git add android/ store-app/ website/ backend/ cloudflare/ .gemini/ .agent/
git commit -m "feat/fix: <description> (v<versionName>) [deployed to Our Store & Website]"
git push origin main
```

---

## 3. Mandatory End-to-End Verification
Before reporting completion:
1. Verify Website is live at `https://connecto.fun` / `https://connecto-web.pages.dev`.
2. Verify Our Store API (`GET https://connecto.fun/api/v1/store/apps`) returns the new `version_name`, `version_code`, `size_bytes`, and changelog.
3. Verify Direct APK CDN (`HEAD https://connecto.fun/downloads/connecto-fun.apk`) returns `HTTP/2 200`, `x-connecto-version: v<versionName>`, `x-connecto-build: <versionCode>`, and exact `content-length`.
