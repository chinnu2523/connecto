# Mandatory Ecosystem Deployment & Verification Rule

> **CRITICAL POLICY: AFTER EVERY IMPLEMENTATION, CHANGE, OR BUG FIX IN THE APPLICATION, YOU MUST AUTOMATICALLY COMPILE AND DEPLOY THE LATEST VERSION IN BOTH OUR STORE AND THE WEBSITE.**
> 
> Deployment is strictly mandatory on every code modification without waiting for user reminders.

---

## 1. Scope & Trigger Conditions

This rule triggers automatically and unconditionally whenever:
1. Any code, UI layout, theme, asset, or logic change is made to **Connecto** (`android/app/`).
2. Any code, UI layout, theme, asset, or logic change is made to **Our Store** (`store-app/app/`).
3. Any change is made to backend APIs, edge endpoints, or website interfaces supporting these apps.

---

## 2. Mandatory Dual-Deployment Pipeline (Store & Website)

Whenever any application change is made, execute the following steps in sequence:

### Step 1: Versioning & Changelog Synchronization
- Increment `versionCode` and advance `versionName` in `app/build.gradle.kts`.
- Update metadata in `backend/app/api/v1/store.py` (`version_name`, `version_code`, `size_bytes`, `size_formatted`, and changelog).
- Update metadata in `cloudflare/worker.js` if version headers or catalog are changed.

### Step 2: Production Release Build
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export ANDROID_HOME=/Users/madarauchiha/Library/Android/sdk

# Build Connecto Release APK
cd /Users/madarauchiha/connecto/android && ./gradlew assembleRelease --no-daemon

# Build Our Store Release APK (when modified)
cd /Users/madarauchiha/connecto/store-app && ./gradlew assembleRelease --no-daemon
```

### Step 3: Fast CDN Distribution (GitHub Releases)
Upload the compiled APKs to the latest GitHub Release (`chinnu2523/connecto`):
```bash
cd /Users/madarauchiha/connecto
LATEST_TAG=$(gh release list --limit 1 | awk '{print $1}')
gh release upload "$LATEST_TAG" android/app/build/outputs/apk/release/app-release.apk#connecto-fun.apk --clobber
gh release upload "$LATEST_TAG" store-app/app/build/outputs/apk/release/app-release.apk#our-store.apk --clobber

# Sync local fallback downloads
cp android/app/build/outputs/apk/release/app-release.apk backend/static/downloads/connecto-fun.apk
cp store-app/app/build/outputs/apk/release/app-release.apk backend/static/downloads/our-store.apk
```

### Step 4: Deploy to the Website (Cloudflare Pages)
Deploy the website static files to Cloudflare Pages:
```bash
cd /Users/madarauchiha/connecto/website
# Remove any local APK >25MiB from static/downloads to comply with Cloudflare Pages 25MiB file limit
# (Cloudflare _redirects transparently routes /downloads/connecto-fun.apk to the GitHub Releases CDN)
rm -f static/downloads/connecto-fun.apk
npx wrangler pages deploy static --project-name=connecto-web --commit-dirty=true
```

### Step 5: Deploy to Our Store Platform & Edge Gateway
Deploy the Cloudflare Worker edge gateway and verify the catalog:
```bash
cd /Users/madarauchiha/connecto/cloudflare && npx wrangler deploy
```

### Step 6: Git Commit & Push
Commit all changes and push to GitHub `main`:
```bash
cd /Users/madarauchiha/connecto
git add android/ store-app/ website/ backend/ cloudflare/ .gemini/ DEPLOYMENT_RULE.md
git commit -m "feat/fix: <description> [deployed to Our Store & Website]"
git push origin main
```

---

## 3. End-to-End Verification Check
Before reporting completion:
1. Verify website deployment is live at `https://connecto-web.pages.dev` and `https://connecto.fun`.
2. Verify GitHub Release asset exists and is downloadable.
3. Verify Our Store API returns the updated version and changelog.
