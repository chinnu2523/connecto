"""
Connecto D1 Cloud Sync & Origin Heartbeat Engine
================================================
Synchronizes local SQLite database with Cloudflare D1 Edge Database
and maintains continuous origin health telemetry for automated edge failover.

- Local Server Online: Pushes data to Cloudflare D1, broadcasts heartbeat (origin_state = 'UP').
- Local Server Offline: Automatically marks origin_state = 'DOWN' on shutdown.
  Cloudflare Edge Gateway detects offline state and takes over 100% of traffic.
"""

import asyncio
import json
import logging
import sqlite3
import time
import urllib.request
import urllib.error
from typing import Dict, Any, List, Optional
from app.core.config import settings, DEFAULT_DB_PATH

logger = logging.getLogger("connecto.d1_sync")

class D1SyncManager:
    def __init__(self):
        self.endpoint = getattr(settings, "D1_ENDPOINT", "https://connecto.fun/api/v1/db/query")
        self.health_endpoint = getattr(settings, "D1_HEALTH_ENDPOINT", "https://connecto.fun/api/v1/db/health")
        self.api_key = getattr(settings, "D1_API_KEY", "connecto_d1_sec_2026_prod")
        self.local_db_path = DEFAULT_DB_PATH
        self.user_agent = "Connecto-Sync-Daemon/1.0 (Darwin; x86_64)"
        self.is_running = False
        self._task: Optional[asyncio.Task] = None
        self.last_sync_time = 0

    def is_cloud_online(self) -> bool:
        """Checks if the Cloudflare D1 edge server is online and responsive."""
        try:
            req = urllib.request.Request(
                self.health_endpoint,
                headers={"User-Agent": self.user_agent}
            )
            with urllib.request.urlopen(req, timeout=3.0) as resp:
                if resp.status == 200:
                    data = json.loads(resp.read().decode("utf-8"))
                    return data.get("status") == "healthy"
            return False
        except Exception as e:
            logger.debug(f"[D1_SYNC] Cloud health probe failed: {e}")
            return False

    def query_cloud_d1(self, sql: str) -> Optional[List[Dict[str, Any]]]:
        """Executes a SQL statement against Cloudflare D1 HTTP Gateway."""
        try:
            req = urllib.request.Request(
                self.endpoint,
                data=json.dumps({"sql": sql}).encode("utf-8"),
                headers={
                    "Content-Type": "application/json",
                    "X-D1-Key": self.api_key,
                    "User-Agent": self.user_agent
                }
            )
            with urllib.request.urlopen(req, timeout=5.0) as resp:
                if resp.status == 200:
                    data = json.loads(resp.read().decode("utf-8"))
                    if data.get("success"):
                        return data.get("results", [])
                    else:
                        logger.error(f"[D1_SYNC] D1 query returned error: {data.get('error')}")
            return None
        except Exception as e:
            logger.error(f"[D1_SYNC] Failed to execute D1 query: {e}")
            return None

    def execute_batch_cloud_d1(self, items: List[Any]) -> bool:
        """Executes a list of SQL statements or param objects via Cloudflare D1 batch API."""
        if not items:
            return True
        try:
            batch_payload = []
            for item in items:
                if isinstance(item, str):
                    batch_payload.append({"sql": item})
                elif isinstance(item, dict):
                    batch_payload.append(item)
                else:
                    batch_payload.append({"sql": str(item)})

            req = urllib.request.Request(
                self.endpoint,
                data=json.dumps({"batch": batch_payload}).encode("utf-8"),
                headers={
                    "Content-Type": "application/json",
                    "X-D1-Key": self.api_key,
                    "User-Agent": self.user_agent
                }
            )
            with urllib.request.urlopen(req, timeout=10.0) as resp:
                if resp.status == 200:
                    data = json.loads(resp.read().decode("utf-8"))
                    return bool(data.get("success"))
                else:
                    logger.error(f"[D1_SYNC] D1 batch HTTP error: {resp.status}")
            return False
        except Exception as e:
            logger.error(f"[D1_SYNC] Failed to execute D1 batch: {e}")
            return False

    def send_heartbeat(self, state: str = "UP", details: str = "Local server operational on port 8081"):
        """Sends real-time origin heartbeat to Cloudflare D1."""
        t0 = time.time()
        # Escape single quotes in details
        clean_details = details.replace("'", "''")
        latency_ms = int((time.time() - t0) * 1000)
        sql = (
            f"INSERT OR REPLACE INTO server_status (id, origin_state, last_checked, latency_ms, details) "
            f"VALUES ('main_origin', '{state}', datetime('now'), {latency_ms}, '{clean_details}');"
        )
        res = self.query_cloud_d1(sql)
        if res is not None:
            logger.info(f"[D1_SYNC] Heartbeat sent: state={state}, latency={latency_ms}ms")
        else:
            logger.warning(f"[D1_SYNC] Could not send heartbeat state={state}")

    def sync_table_to_cloud(self, table_name: str) -> int:
        """Pushes rows from local SQLite table to Cloudflare D1 using INSERT OR REPLACE via parameterized batch API."""
        try:
            conn = sqlite3.connect(self.local_db_path)
            conn.row_factory = sqlite3.Row
            cur = conn.cursor()

            # Verify local table exists
            tables = [t[0] for t in cur.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()]
            if table_name not in tables:
                conn.close()
                return 0

            rows = cur.execute(f"SELECT * FROM {table_name}").fetchall()
            conn.close()

            if not rows:
                return 0

            # Batch statements in groups of 25 for fast, clean, atomic execution
            batch_size = 25
            total_synced = 0

            for i in range(0, len(rows), batch_size):
                batch = rows[i:i + batch_size]
                items = []
                for r in batch:
                    cols = list(r.keys())
                    col_names = ", ".join(cols)
                    placeholders = ", ".join(["?" for _ in cols])
                    stmt = f"INSERT OR REPLACE INTO {table_name} ({col_names}) VALUES ({placeholders});"
                    items.append({"sql": stmt, "params": [r[c] for c in cols]})

                success = self.execute_batch_cloud_d1(items)
                if success:
                    total_synced += len(batch)
                else:
                    # Fallback to row-by-row for resilience against isolated constraints
                    logger.warning(f"[D1_SYNC] Batch sync failed for {table_name} offset {i}, trying row-by-row...")
                    for single_item in items:
                        if self.execute_batch_cloud_d1([single_item]):
                            total_synced += 1

            return total_synced
        except Exception as e:
            logger.error(f"[D1_SYNC] Error syncing table {table_name} to cloud: {e}")
            return 0

    def sync_all_to_cloud(self) -> Dict[str, int]:
        """Synchronizes all primary application tables from local SQLite to Cloudflare D1."""
        target_tables = [
            "users",
            "user_sessions",
            "user_academy_profiles",
            "servers",
            "channels",
            "server_members",
            "dm_participants",
            "messages",
            "friendships",
            "message_read_receipts",
            "dm_read_states",
            "notifications",
            "course_tracks",
            "lessons",
            "quiz_questions",
            "user_progress",
            "job_openings",
            "job_applications",
            "voice_rooms",
            "voice_room_participants",
            "call_logs",
            "scheduled_messages",
            "bookmarks",
            "referrals",
            "moderation_reports",
            "audit_logs",
            "user_email_aliases"
        ]
        results = {}
        for tbl in target_tables:
            synced = self.sync_table_to_cloud(tbl)
            results[tbl] = synced
            if synced > 0:
                logger.info(f"[D1_SYNC] Table '{tbl}' synced: {synced} rows updated in Cloudflare D1")
        self.last_sync_time = time.time()
        return results

    def push_single_message(self, msg_payload: Dict[str, Any]) -> bool:
        """Immediately pushes an individual message to Cloudflare D1."""
        try:
            cols = list(msg_payload.keys())
            col_names = ", ".join(cols)
            placeholders = ", ".join(["?" for _ in cols])
            stmt = f"INSERT OR REPLACE INTO messages ({col_names}) VALUES ({placeholders});"
            item = {"sql": stmt, "params": [msg_payload[c] for c in cols]}
            return self.execute_batch_cloud_d1([item])
        except Exception as e:
            logger.error(f"[D1_SYNC] Error pushing single message to D1: {e}")
            return False

    def pull_from_cloud(self) -> Dict[str, int]:
        """Pulls recent changes from Cloudflare D1 into local SQLite to prevent regressions.
        
        Runs on local server startup before broadcasting 'UP' status:
        1. Syncs any messages sent during failover.
        2. Syncs updated user profile fields (avatar_url, banner_url, bio, display_name).
        3. Syncs new channels or friendships created in cloud mode.
        """
        results = {"messages_pulled": 0, "users_updated": 0, "friendships_pulled": 0}
        try:
            conn = sqlite3.connect(self.local_db_path)
            conn.row_factory = sqlite3.Row
            cur = conn.cursor()

            # 1. Pull New Messages from Cloud
            cloud_msgs = self.query_cloud_d1(
                "SELECT id, channel_id, sender_id, content, created_at, reply_to_id, reply_to_content, reply_to_author "
                "FROM messages ORDER BY created_at DESC LIMIT 100;"
            )
            if cloud_msgs:
                for msg in cloud_msgs:
                    mid = msg.get("id")
                    if not mid:
                        continue
                    cur.execute("SELECT id FROM messages WHERE id = ?", (mid,))
                    if not cur.fetchone():
                        # Message exists in cloud but not locally -> insert into local SQLite
                        cur.execute(
                            "INSERT OR IGNORE INTO messages (id, channel_id, sender_id, content, created_at, reply_to_id, reply_to_content, reply_to_author) "
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            (
                                mid,
                                msg.get("channel_id"),
                                msg.get("sender_id"),
                                msg.get("content"),
                                msg.get("created_at"),
                                msg.get("reply_to_id"),
                                msg.get("reply_to_content"),
                                msg.get("reply_to_author")
                            )
                        )
                        results["messages_pulled"] += 1

            # 2. Reconcile Users (Avatar, Banner, Bio, Display Name, Online State)
            cloud_users = self.query_cloud_d1(
                "SELECT id, username, display_name, avatar_url, banner_url, bio, is_stealth, updated_at FROM users;"
            )
            if cloud_users:
                for cu in cloud_users:
                    uid = cu.get("id")
                    if not uid:
                        continue
                    cur.execute("SELECT id, avatar_url, banner_url, bio, display_name, updated_at FROM users WHERE id = ?", (uid,))
                    loc_u = cur.fetchone()
                    if loc_u:
                        c_updated = cu.get("updated_at") or ""
                        l_updated = loc_u["updated_at"] or ""
                        # If cloud version has avatar/banner or newer timestamp, pull cloud profile
                        if (cu.get("avatar_url") and cu.get("avatar_url") != loc_u["avatar_url"]) or \
                           (cu.get("banner_url") and cu.get("banner_url") != loc_u["banner_url"]) or \
                           (c_updated > l_updated):
                            cur.execute(
                                "UPDATE users SET "
                                "display_name = coalesce(?, display_name), "
                                "avatar_url = coalesce(?, avatar_url), "
                                "banner_url = coalesce(?, banner_url), "
                                "bio = coalesce(?, bio), "
                                "updated_at = datetime('now') "
                                "WHERE id = ?",
                                (
                                    cu.get("display_name"),
                                    cu.get("avatar_url"),
                                    cu.get("banner_url"),
                                    cu.get("bio"),
                                    uid
                                )
                            )
                            results["users_updated"] += 1

            # 3. Pull Friendships created in cloud mode
            cloud_friends = self.query_cloud_d1("SELECT user_id, friend_id, status, created_at FROM friendships;")
            if cloud_friends:
                for f in cloud_friends:
                    cur.execute("SELECT 1 FROM friendships WHERE user_id = ? AND friend_id = ?", (f.get("user_id"), f.get("friend_id")))
                    if not cur.fetchone():
                        cur.execute(
                            "INSERT OR IGNORE INTO friendships (user_id, friend_id, status, created_at) VALUES (?, ?, ?, ?)",
                            (f.get("user_id"), f.get("friend_id"), f.get("status", "accepted"), f.get("created_at"))
                        )
                        results["friendships_pulled"] += 1

            conn.commit()
            conn.close()
            logger.info(f"[D1_SYNC] Reverse catch-up complete: {results}")
        except Exception as e:
            logger.error(f"[D1_SYNC] Error during reverse catch-up from cloud: {e}")
        return results

    async def run_worker(self):
        """Continuous background worker loop for periodic heartbeat and sync."""
        self.is_running = True
        logger.info("[D1_SYNC] Cloud Sync & Heartbeat worker started.")
        
        # Initial probe and bidirectional catch-up
        try:
            if self.is_cloud_online():
                logger.info("[D1_SYNC] Cloud server is ONLINE. Performing reverse catch-up before broadcast...")
                # 1. Pull changes made during downtime
                await asyncio.to_thread(self.pull_from_cloud)

                # 2. Send UP heartbeat
                self.send_heartbeat(state="UP", details="Local server started operational")

                # 3. Push any local delta to cloud
                await asyncio.to_thread(self.sync_all_to_cloud)
            else:
                logger.warning("[D1_SYNC] Cloud server offline or unreachable on startup.")
        except Exception as e:
            logger.error(f"[D1_SYNC] Initial sync error: {e}")

        # Periodic loop: Heartbeat every 15 seconds, full sync every 60 seconds
        counter = 0
        while self.is_running:
            try:
                await asyncio.sleep(15)
                counter += 1
                
                # Check cloud online
                online = await asyncio.to_thread(self.is_cloud_online)
                if online:
                    # Send UP heartbeat
                    await asyncio.to_thread(self.send_heartbeat, "UP", "Local server running on port 8081")

                    # Periodically sync incremental updates (every 60 seconds)
                    if counter % 4 == 0:
                        await asyncio.to_thread(self.sync_all_to_cloud)
                else:
                    logger.debug("[D1_SYNC] Cloud server probe did not respond, retrying next cycle...")
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"[D1_SYNC] Worker iteration error: {e}")

        # Shutdown sequence
        try:
            logger.info("[D1_SYNC] Local server shutting down. Broadcasting DOWN status to Cloudflare D1...")
            await asyncio.to_thread(
                self.send_heartbeat,
                "DOWN",
                "Local server gracefully shut down; Cloud server takeover active"
            )
        except Exception as e:
            logger.error(f"[D1_SYNC] Error sending shutdown heartbeat: {e}")

    def start(self):
        """Starts the sync worker as an asyncio background task."""
        if self._task is None or self._task.done():
            self._task = asyncio.create_task(self.run_worker())

    async def stop(self):
        """Stops the sync worker and broadcasts DOWN status."""
        self.is_running = False
        if self._task and not self._task.done():
            self._task.cancel()
            try:
                await self._task
            except (asyncio.CancelledError, Exception):
                pass
        # Final synchronous DOWN heartbeat
        try:
            self.send_heartbeat("DOWN", "Local server stopped; Cloud server active")
        except Exception:
            pass

# Singleton instance
d1_sync_manager = D1SyncManager()
