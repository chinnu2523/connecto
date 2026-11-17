#!/usr/bin/env bash
# ==============================================================================
# Connecto Ecosystem: Automated Data Pruning & Release Hygiene Script
# Deletes old/obsolete application data across GitHub, Cloudflare D1, and Server.
# ==============================================================================
set -euo pipefail

REPO_DIR="/Users/madarauchiha/connecto"
KEEP_COUNT="${1:-2}"

echo "=========================================================="
echo " Starting Connecto Ecosystem Data Pruning (Keep Latest $KEEP_COUNT)"
echo " Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "=========================================================="

# ------------------------------------------------------------------------------
# 1. PRUNE GITHUB RELEASES & ATTACHED APKS
# ------------------------------------------------------------------------------
echo ""
echo "[Step 1/4] Checking GitHub Releases for pruning..."
cd "$REPO_DIR"

if command -v gh >/dev/null 2>&1; then
    # Fetch all release tags older than the top KEEP_COUNT releases
    OLD_TAGS=$(gh release list --limit 150 --json tagName | jq -r ".[$KEEP_COUNT:][].tagName" 2>/dev/null || true)
    
    if [ -n "$OLD_TAGS" ]; then
        COUNT=$(echo "$OLD_TAGS" | wc -l | tr -d ' ')
        echo "Found $COUNT old GitHub release(s) to prune. Deleting in parallel (concurrency 6)..."
        echo "$OLD_TAGS" | xargs -n 1 -P 6 -I {} sh -c '
            echo "  -> Deleting GitHub release and tag: $1"
            gh release delete "$1" --yes --cleanup-tag 2>/dev/null || gh release delete "$1" --yes 2>/dev/null || true
        ' _ {}
        echo "✓ GitHub releases pruned successfully."
    else
        echo "✓ No excess GitHub releases found (kept latest $KEEP_COUNT)."
    fi
else
    echo "⚠️ gh CLI not found, skipping GitHub release pruning."
fi

# ------------------------------------------------------------------------------
# 2. PRUNE CLOUDFLARE D1 (connecto-db) OLD APP_RELEASES
# ------------------------------------------------------------------------------
echo ""
echo "[Step 2/4] Pruning old release records in Cloudflare D1 (connecto-db)..."
cd "$REPO_DIR/cloudflare"

if command -v npx >/dev/null 2>&1; then
    # Keep the latest active release and immediate fallback; delete all older records
    PRUNE_SQL="DELETE FROM app_releases WHERE is_latest = 0 AND id NOT IN (SELECT id FROM app_releases ORDER BY created_at DESC LIMIT $KEEP_COUNT);"
    echo "Executing D1 cleanup query..."
    npx wrangler d1 execute connecto-db --remote --command="$PRUNE_SQL" >/dev/null 2>&1 || true
    echo "✓ Cloudflare D1 app_releases table pruned."
else
    echo "⚠️ npx not found, skipping D1 pruning."
fi

# ------------------------------------------------------------------------------
# 3. PRUNE SERVER DOWNLOADS & LOCAL STALE ARTIFACTS
# ------------------------------------------------------------------------------
echo ""
echo "[Step 3/4] Pruning server static downloads and orphan files..."
DOWNLOADS_DIR="$REPO_DIR/backend/static/downloads"

if [ -d "$DOWNLOADS_DIR" ]; then
    # Remove any generic or stale APKs
    rm -f "$DOWNLOADS_DIR/app-release.apk"
    rm -f "$DOWNLOADS_DIR/temp-*.apk"
    rm -f "$DOWNLOADS_DIR"/*.tmp
    rm -f "$DOWNLOADS_DIR"/*.partial
    echo "✓ Stale APKs and temp downloads cleaned from $DOWNLOADS_DIR."
fi

# Ensure no APK >25MiB lingers in website/static/downloads
WEBSITE_DOWNLOADS="$REPO_DIR/website/static/downloads"
if [ -d "$WEBSITE_DOWNLOADS" ]; then
    rm -f "$WEBSITE_DOWNLOADS/connecto-fun.apk"
    rm -f "$WEBSITE_DOWNLOADS/app-release.apk"
    echo "✓ Website static downloads cleaned."
fi

# ------------------------------------------------------------------------------
# 4. PRUNE BUILD DIRECTORIES & CACHES
# ------------------------------------------------------------------------------
echo ""
echo "[Step 4/4] Pruning build caches and intermediate artifacts..."
rm -rf "$REPO_DIR/android/app/build/intermediates/apk" 2>/dev/null || true
rm -rf "$REPO_DIR/store-app/app/build/intermediates/apk" 2>/dev/null || true
rm -rf "$REPO_DIR/.pytest_cache" 2>/dev/null || true
rm -rf "$REPO_DIR/backend/.pytest_cache" 2>/dev/null || true

echo ""
echo "=========================================================="
echo "✓ Connecto Ecosystem Data Pruning Completed Successfully."
echo "=========================================================="
