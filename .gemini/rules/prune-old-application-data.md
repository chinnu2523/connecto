# Mandatory Rule: Pruning & Deletion of Old Application Data, Releases & Artifacts

> **CRITICAL & NON-NEGOTIABLE POLICY:**
> ON EVERY RELEASE, APPLICATION DEPLOYMENT, OR MAINTENANCE CYCLE, YOU MUST AUTOMATICALLY PURGE AND DELETE ALL PREVIOUS AND OLD APPLICATION DATA, OLD RELEASES, OBSOLETE APKS, AND EXPIRED CACHES ACROSS GITHUB AND SERVER INFRASTRUCTURE.
> 
> ACCUMULATION OF STALE BINARIES, DEPRECATED DATABASE RECORDS, OR OLD GITHUB RELEASES IS STRICTLY FORBIDDEN.

---

## 1. Scope & Automatic Trigger Conditions

This rule executes automatically and unconditionally:
1. Immediately following the publication of any new application release (Connecto or Our Store).
2. During every dual-deployment cycle to Our Store and the Website.
3. During ecosystem maintenance and cleanup workflows.

---

## 2. Mandatory Cleanup Targets

### A. GitHub Releases & Storage
- **Retention Limit**: Retain ONLY the latest active production release (and at most 1 immediately preceding version for emergency fallback).
- **Deletion Protocol**: Automatically delete all older releases, obsolete draft releases, and their attached APK binaries from GitHub:
  ```bash
  cd /Users/madarauchiha/connecto
  OLD_TAGS=$(gh release list --limit 150 --json tagName | jq -r '.[2:][].tagName')
  for tag in $OLD_TAGS; do
      gh release delete "$tag" --yes --cleanup-tag
  done
  ```
- **Prohibition**: Never allow dozens of historical APK releases or abandoned draft releases to accumulate on GitHub.

### B. Cloudflare D1 Database (`connecto-db`)
- **Table**: `app_releases`
- **Retention Limit**: Retain only active latest release records (`is_latest = 1`) and the immediate fallback.
- **Cleanup Query**:
  ```sql
  DELETE FROM app_releases WHERE is_latest = 0 AND id NOT IN (SELECT id FROM app_releases ORDER BY created_at DESC LIMIT 2);
  ```
- All obsolete release rows, outdated checksum records, and legacy metadata must be purged.

### C. Server & Website File Storage
- **Server Downloads (`backend/static/downloads/`)**:
  - Remove stale binaries (e.g., `app-release.apk`, deprecated versions, temporary files).
  - Retain only active canonical APK files (`connecto-fun.apk`, `our-store.apk`, `pdf-studio.apk`).
- **Website Downloads (`website/static/downloads/`)**:
  - Ensure no APK binaries linger locally in Cloudflare Pages build outputs (Cloudflare Pages 25MiB limit).

### D. Local & Server Build Caches
- Clean up intermediate build outputs and caches regularly:
  - `android/app/build/intermediates/apk`
  - `store-app/app/build/intermediates/apk`
  - `.pytest_cache/`

---

## 3. Automated Execution Tool

All steps are consolidated into `scripts/prune_old_data.sh`:
```bash
/Users/madarauchiha/connecto/scripts/prune_old_data.sh 2
```
This script must be executed as part of every deployment pipeline before final verification.
