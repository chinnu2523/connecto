"""
Synchronize Clean Canonical State to Cloudflare D1
=================================================
Cleans Cloudflare D1 remote database and syncs the deduplicated,
canonical tables directly from local SQLite.
"""

import sys
import os

sys.path.insert(0, "/Users/madarauchiha/connecto/backend")
from app.db.d1_sync import d1_sync_manager

def sync_clean_to_d1():
    print("1. Cleaning remote Cloudflare D1 tables...")
    cleanup_statements = [
        "DELETE FROM messages WHERE id LIKE '%_name';",
        "DELETE FROM channels;",
        "DELETE FROM users WHERE username IN ('shinobi_a_1789063235917', 'shinobi_b_1789063235917', 'ninja_tester_1790750177', 'ninja_773242');"
    ]
    for stmt in cleanup_statements:
        res = d1_sync_manager.query_cloud_d1(stmt)
        print(f"  Executed: {stmt}")

    print("\n2. Pushing all clean canonical tables from SQLite to Cloudflare D1...")
    results = d1_sync_manager.sync_all_to_cloud()
    for tbl, count in results.items():
        print(f"  Table '{tbl}': {count} rows synced to Cloudflare D1")

    print("\n3. Verifying remote D1 table row counts...")
    check_query = "SELECT 'channels' as tbl, count(*) as cnt FROM channels UNION ALL SELECT 'messages', count(*) FROM messages UNION ALL SELECT 'users', count(*) FROM users;"
    res = d1_sync_manager.query_cloud_d1(check_query)
    if res:
        for r in res:
            print(f"  Remote D1 {r.get('tbl')}: {r.get('cnt')} rows")
    else:
        print("  Could not query counts directly via query_cloud_d1.")

if __name__ == "__main__":
    sync_clean_to_d1()
