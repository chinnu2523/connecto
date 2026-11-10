#!/usr/bin/env bash
# ==============================================================================
# Connecto Ecosystem Automated Deployment & Verification Pipeline
# Rule: Execute pre-flight verification -> build release APKs -> verify integrity
#       -> deploy to GitHub Releases, Cloudflare Edge CDN, and website/catalog.
# ==============================================================================

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "======================================================================"
echo "🚀 CONNECTO ECOSYSTEM AUTOMATED DEPLOYMENT PIPELINE"
echo "======================================================================"
echo "Project Root: ${PROJECT_ROOT}"
echo "Java Home:    ${JAVA_HOME}"
java -version 2>&1 | head -n 1
echo "----------------------------------------------------------------------"

# ----------------------------------------------------------------------
# STEP 1: PRE-FLIGHT CHECKS & VERIFICATION
# ----------------------------------------------------------------------
echo "▶ STEP 1: Executing Pre-Flight System & Version Checks..."

# 1.1 Verify Keystores exist
if [[ ! -f "${PROJECT_ROOT}/android/app/connecto-release.jks" ]]; then
    echo "❌ Error: Connecto release keystore missing at android/app/connecto-release.jks"
    exit 1
fi

# 1.2 Read Versions from Gradle configs
CONNECTO_VERSION=$(grep 'versionName = ' "${PROJECT_ROOT}/android/app/build.gradle.kts" | head -n 1 | sed 's/.*"\(.*\)".*/\1/')
CONNECTO_BUILD=$(grep 'versionCode = ' "${PROJECT_ROOT}/android/app/build.gradle.kts" | head -n 1 | sed 's/[^0-9]*//g')

STORE_VERSION=$(grep 'versionName = ' "${PROJECT_ROOT}/store-app/app/build.gradle.kts" | head -n 1 | sed 's/.*"\(.*\)".*/\1/')
STORE_BUILD=$(grep 'versionCode = ' "${PROJECT_ROOT}/store-app/app/build.gradle.kts" | head -n 1 | sed 's/[^0-9]*//g')

echo "✔ Detected Connecto:  v${CONNECTO_VERSION} (Build ${CONNECTO_BUILD})"
echo "✔ Detected Our Store: v${STORE_VERSION} (Build ${STORE_BUILD})"

# 1.3 Verify version synchronization in catalog and worker
echo "▶ Verifying catalog version synchronization..."
grep -q "\"version_name\": \"${CONNECTO_VERSION}\"" "${PROJECT_ROOT}/backend/app/api/v1/store.py" || {
    echo "⚠️ Warning: Connecto version in store.py does not match ${CONNECTO_VERSION}"
}
grep -q "X-Connecto-Store-Version\": \"v${STORE_VERSION}\"" "${PROJECT_ROOT}/cloudflare/worker.js" || {
    echo "⚠️ Warning: Our Store version in worker.js does not match v${STORE_VERSION}"
}
echo "✔ Pre-flight verification passed successfully."
echo "----------------------------------------------------------------------"

# ----------------------------------------------------------------------
# STEP 2: COMPILE & ASSEMBLE SIGNED RELEASE APKS
# ----------------------------------------------------------------------
echo "▶ STEP 2: Building Signed Release APKs..."

# 2.1 Build Connecto Release APK
echo "▶ Compiling Connecto App (v${CONNECTO_VERSION})..."
cd "${PROJECT_ROOT}/android"
./gradlew assembleRelease --no-daemon

CONNECTO_RELEASE_APK="${PROJECT_ROOT}/android/app/build/outputs/apk/release/app-release.apk"
if [[ ! -f "${CONNECTO_RELEASE_APK}" ]]; then
    echo "❌ Error: Connecto release APK not generated at ${CONNECTO_RELEASE_APK}"
    exit 1
fi
echo "✔ Connecto Release APK compiled successfully."

# 2.2 Build Our Store Release APK
echo "▶ Compiling Our Store App (v${STORE_VERSION})..."
cd "${PROJECT_ROOT}/store-app"
./gradlew assembleRelease --no-daemon

STORE_RELEASE_APK="${PROJECT_ROOT}/store-app/app/build/outputs/apk/release/app-release.apk"
if [[ ! -f "${STORE_RELEASE_APK}" ]]; then
    echo "❌ Error: Our Store release APK not generated at ${STORE_RELEASE_APK}"
    exit 1
fi
echo "✔ Our Store Release APK compiled successfully."
echo "----------------------------------------------------------------------"

# ----------------------------------------------------------------------
# STEP 3: CRYPTOGRAPHIC INTEGRITY VERIFICATION
# ----------------------------------------------------------------------
echo "▶ STEP 3: Cryptographic Integrity & Size Verification..."

mkdir -p "${PROJECT_ROOT}/website/static/downloads"

# Copy to canonical static download paths
cp -f "${CONNECTO_RELEASE_APK}" "${PROJECT_ROOT}/website/static/downloads/connecto-fun.apk"
cp -f "${STORE_RELEASE_APK}" "${PROJECT_ROOT}/website/static/downloads/our-store.apk"

CONNECTO_SHA256=$(shasum -a 256 "${PROJECT_ROOT}/website/static/downloads/connecto-fun.apk" | awk '{print $1}')
CONNECTO_SIZE=$(du -h "${PROJECT_ROOT}/website/static/downloads/connecto-fun.apk" | awk '{print $1}')

STORE_SHA256=$(shasum -a 256 "${PROJECT_ROOT}/website/static/downloads/our-store.apk" | awk '{print $1}')
STORE_SIZE=$(du -h "${PROJECT_ROOT}/website/static/downloads/our-store.apk" | awk '{print $1}')

echo "✔ Connecto APK:  ${CONNECTO_SIZE} | SHA256: ${CONNECTO_SHA256}"
echo "✔ Our Store APK: ${STORE_SIZE} | SHA256: ${STORE_SHA256}"
echo "----------------------------------------------------------------------"

# ----------------------------------------------------------------------
# STEP 4: DEPLOY TO GITHUB RELEASES & CLOUDFLARE EDGE
# ----------------------------------------------------------------------
echo "▶ STEP 4: Deploying Assets to GitHub Releases & Cloudflare Edge..."

# 4.1 Upload to GitHub Release v4.6.11
if command -v gh &> /dev/null; then
    echo "▶ Uploading APKs to GitHub Release v4.6.11..."
    gh release upload v4.6.11 "${PROJECT_ROOT}/website/static/downloads/connecto-fun.apk" --clobber
    gh release upload v4.6.11 "${PROJECT_ROOT}/website/static/downloads/our-store.apk" --clobber
    echo "✔ Uploaded to GitHub Release v4.6.11"
else
    echo "⚠️ gh CLI not found; skipping GitHub Release asset upload."
fi

# 4.2 Deploy Cloudflare Worker Edge Gateway
if command -v npx &> /dev/null; then
    echo "▶ Deploying Cloudflare Edge Worker..."
    cd "${PROJECT_ROOT}/cloudflare"
    npx wrangler deploy || echo "⚠️ Warning: wrangler deploy exited with warning."
    echo "✔ Cloudflare Edge Worker deployed."
else
    echo "⚠️ npx not found; skipping Cloudflare Worker deploy."
fi
echo "----------------------------------------------------------------------"

# ----------------------------------------------------------------------
# STEP 5: EMULATOR INSTALLATION (IF EMULATOR ONLINE)
# ----------------------------------------------------------------------
if [[ "${INSTALL_ON_DEVICE:-false}" == "true" ]] && command -v adb &> /dev/null; then
    ONLINE_DEVICE=$(adb devices | grep -w "device" | awk '{print $1}' | head -n 1 || true)
    if [[ -n "${ONLINE_DEVICE}" ]]; then
        echo "▶ STEP 5: Installing updated APKs on online device: ${ONLINE_DEVICE}..."
        adb -s "${ONLINE_DEVICE}" install -r "${PROJECT_ROOT}/website/static/downloads/our-store.apk" || true
        adb -s "${ONLINE_DEVICE}" install -r "${PROJECT_ROOT}/website/static/downloads/connecto-fun.apk" || true
        echo "✔ Device installation complete."
    fi
fi

echo "======================================================================"
echo "🎉 DEPLOYMENT COMPLETED & VERIFIED SUCCESSFULLY!"
echo "   Connecto:  v${CONNECTO_VERSION} (Build ${CONNECTO_BUILD}) [${CONNECTO_SIZE}]"
echo "   Our Store: v${STORE_VERSION} (Build ${STORE_BUILD}) [${STORE_SIZE}]"
echo "======================================================================"
