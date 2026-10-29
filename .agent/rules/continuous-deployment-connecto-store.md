# Continuous Mandatory Deployment Rule: Connecto, Our Store, JARVIS AI & PDF Studio Synchronization

**CRITICAL PROTOCOL: MANDATORY MULTI-CHANNEL DEPLOYMENT ON EVERY UPDATE OR IMPLEMENTATION**

Whenever any change, bug fix, feature enhancement, UI modification, or update is implemented for any application in the ecosystem:
1. **Connecto** (`com.connecto.app`) & **Our Store** (`com.connecto.store`):
   - **MUST BE DEPLOYED BOTH TO THE WEBSITE AND DIRECTLY INTO OUR STORE.**
2. **JARVIS AI** (`com.omni.assistant`) & **PDF Studio** (`com.app.pdfstudio`):
   - **MUST BE DEPLOYED DIRECTLY INTO OUR STORE** (via edge download gateways, `/api/v1/store/apps` catalog, and GitHub release binary assets).

---

## 1. Incremental Build & Version Alignment
1. **Connecto App (`android/app/build.gradle.kts`):**
   - Increment `versionCode` (e.g. `99 -> 100`) and update `versionName` (e.g. `"4.6.10" -> "4.6.11"`).
   - Compile signed release APK: `./gradlew assembleRelease`
2. **Our Store App (`connecto-store/app/build.gradle.kts`):**
   - Increment `versionCode` and update `versionName`.
   - Compile signed release APK:
     `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleRelease`
3. **JARVIS AI & PDF Studio:**
   - Package release binaries (`jarvis-ai-official.apk` and `pdf-studio.apk`).
   - Retain version alignment with Our Store catalog metadata.

---

## 2. Mandatory Synchronization to Website & Download Channels (Connecto & Our Store)
1. **Synchronize APK Binaries to Static Downloads:**
   - Copy compiled `app-release.apk` for Connecto to `website/static/downloads/connecto-fun.apk`, `website/downloads/connecto-fun.apk`, `backend/static/downloads/connecto-fun.apk`.
   - Copy compiled `app-release.apk` for Our Store to `website/static/downloads/our-store.apk`, `backend/static/downloads/our-store.apk`.
2. **Remove Oversized Files Before Pages Deploy:**
   - Cloudflare Pages enforces a strict 25 MiB file size limit for static assets.
   - Remove files larger than 25 MiB (`connecto-fun.apk`, `jarvis-ai-official.apk`, `pdf-studio.apk`) from `website/static/downloads/` prior to running `wrangler pages deploy`:
     `rm -f website/static/downloads/*.apk`
3. **Deploy Web Portal to Cloudflare Pages:**
   - Run:
     `cd /Users/madarauchiha/connecto && npx wrangler pages deploy website/static --project-name=connecto-web --force`

---

## 3. Mandatory Synchronization to Our Store REST API & Edge Gateways (All 4 Apps)
1. **Update Cloudflare Worker [`cloudflare/worker.js`](file:///Users/madarauchiha/connecto/cloudflare/worker.js):**
   - Update `version_name`, `version_code`, `size_bytes`, `size_formatted`, and `changelog` in `/api/v1/store/apps` for all 4 apps (**Our Store**, **Connecto**, **JARVIS AI**, and **PDF Studio**).
   - Ensure the direct APK routes (`/download/apk`, `/downloads/connecto-fun.apk`, `/downloads/our-store.apk`, `/downloads/jarvis-ai-official.apk`, `/downloads/pdf-studio.apk`) reference the latest release assets with exact headers.
2. **Deploy Cloudflare Worker:**
   - Run:
     `cd /Users/madarauchiha/connecto/cloudflare && npx wrangler deploy`

---

## 4. Mandatory GitHub Release Publication (Direct Store Pipeline)
1. **Upload Release Assets to GitHub Release Tag (e.g. `v4.6.10`):**
   - `gh release upload <tag> connecto-fun.apk --clobber`
   - `gh release upload <tag> our-store.apk --clobber`
   - `gh release upload <tag> ConnectoStore.apk --clobber`
   - `gh release upload <tag> jarvis-ai-official.apk --clobber`
   - `gh release upload <tag> pdf-studio.apk --clobber`

---

## 5. Live Verification
1. **Verify Edge Gateway Endpoints:**
   - `curl -sI https://connecto.fun/downloads/connecto-fun.apk` (HTTP 200)
   - `curl -sI https://connecto.fun/downloads/our-store.apk` (HTTP 200)
   - `curl -sI https://connecto.fun/downloads/jarvis-ai-official.apk` (HTTP 200 or 302 to binary asset)
   - `curl -sI https://connecto.fun/downloads/pdf-studio.apk` (HTTP 200)
2. **Verify Our Store Catalog API:**
   - `curl -s https://connecto.fun/api/v1/store/apps` (Must return all 4 applications).
3. **Verify Our Store App on Device / Emulator:**
   - Launch Our Store (`com.connecto.store`) and confirm the app reflects updated versions with 1-tap in-app install/update actions and zero errors.
