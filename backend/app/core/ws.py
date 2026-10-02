import os
import asyncio
import json
from typing import Dict, Set, Any, Union, Optional
from fastapi import WebSocket
from sqlalchemy import select, func
from app.db.session import AsyncSessionLocal
from app.db.models.user import User

try:
    import orjson
    def fast_dumps(data: Any) -> str:
        return orjson.dumps(data).decode("utf-8")
except ImportError:
    def fast_dumps(data: Any) -> str:
        return json.dumps(data)

SEND_TIMEOUT_SECONDS = 2.0
FANOUT_CONCURRENCY = 500
_fanout_semaphore = asyncio.Semaphore(FANOUT_CONCURRENCY)

import httpx

_rust_relay_client: Optional[httpx.AsyncClient] = None

def _get_rust_relay_client() -> httpx.AsyncClient:
    global _rust_relay_client
    if _rust_relay_client is None or _rust_relay_client.is_closed:
        _rust_relay_client = httpx.AsyncClient(base_url="http://127.0.0.1:8082", timeout=0.5)
    return _rust_relay_client

_RELAY_SECRET = os.getenv("RELAY_INTERNAL_SECRET", "")

async def _forward_to_rust_relay(payload: dict):
    """Asynchronously forwards events to the standalone Rust connecto-relay without blocking."""
    try:
        client = _get_rust_relay_client()
        await client.post(
            "/api/broadcast",
            json=payload,
            headers={"X-Internal-Secret": _RELAY_SECRET}
        )
    except Exception:
        pass

async def _send_safe(ws: WebSocket, payload_str: str) -> bool:
    """Sends pre-serialized UTF-8 text with timeout; returns False on slow/broken socket."""
    try:
        async with _fanout_semaphore:
            await asyncio.wait_for(ws.send_text(payload_str), timeout=SEND_TIMEOUT_SECONDS)
        return True
    except Exception:
        return False

class ConnectionManager:
    """
    High-Throughput WebSocket Connection Manager.
    Features:
    - Zero redundant serialization (payloads encoded once via Rust orjson)
    - Fully concurrent fanout across sockets with bounded worker semaphore
    - 2-second hard write timeout per socket (zero Slow Consumer head-of-line blocking)
    - Automatic dead socket pruning on transmission failure
    - Sub-millisecond peer audio relay for WebRTC/Voice rooms
    """
    def __init__(self):
        # user_id -> Set[WebSocket]
        self.active_connections: Dict[str, Set[WebSocket]] = {}
        # channel_id -> Set[user_id]
        self.channel_subscriptions: Dict[str, Set[str]] = {}
        # voice_channel_id / room_id -> Set[user_id]
        self.voice_rooms: Dict[str, Set[str]] = {}
        # username.lower() -> user_id
        self.username_to_id: Dict[str, str] = {}
        # user_id -> username.lower()
        self.id_to_username: Dict[str, str] = {}
        # room_id -> call metadata dict
        self.pending_calls: Dict[str, dict] = {}

    async def broadcast_global(self, message: Union[dict, str]):
        """Broadcasts an event to all currently active WebSocket clients concurrently."""
        payload = fast_dumps(message) if isinstance(message, dict) else str(message)
        try:
            raw_event = orjson.loads(payload) if isinstance(payload, (str, bytes)) else message
            asyncio.create_task(_forward_to_rust_relay({"channel_id": "general", "payload": raw_event}))
        except Exception:
            pass
        seen = set()
        socket_list = []
        for ws_set in list(self.active_connections.values()):
            for ws in list(ws_set):
                if ws not in seen:
                    seen.add(ws)
                    socket_list.append(ws)
        if socket_list:
            results = await asyncio.gather(*[_send_safe(ws, payload) for ws in socket_list], return_exceptions=True)
            dead = [ws for ws, ok in zip(socket_list, results) if ok is not True]
            if dead:
                for dws in dead:
                    self.prune_dead_socket(dws)

    def prune_dead_socket(self, dead_ws: WebSocket):
        """Prunes a dead/broken WebSocket from active connections and updates presence."""
        affected_users = set()
        for uid, ws_set in list(self.active_connections.items()):
            if dead_ws in ws_set:
                ws_set.discard(dead_ws)
                if not ws_set:
                    del self.active_connections[uid]
                    affected_users.add(uid)
        for u in affected_users:
            if u not in self.active_connections:
                asyncio.create_task(self._set_online(u, False))
                clean_u = self.id_to_username.get(u, u)
                asyncio.create_task(self.broadcast_global({"type": "presence_update", "user_id": u, "username": clean_u, "is_online": False, "status": "offline"}))

    async def connect(self, user_id: str, websocket: WebSocket, username: str = None):
        await websocket.accept()
        if user_id not in self.active_connections:
            self.active_connections[user_id] = set()
        self.active_connections[user_id].add(websocket)
        if username:
            clean_u = username.strip().lower()
            self.username_to_id[clean_u] = user_id
            self.id_to_username[user_id] = clean_u
            if clean_u not in self.active_connections:
                self.active_connections[clean_u] = set()
            self.active_connections[clean_u].add(websocket)
        # Mark user as online in DB when they establish a WebSocket connection (only if not stealth)
        async def _check_stealth_and_broadcast():
            try:
                from app.db.session import AsyncSessionLocal
                from app.db.models.user import User
                from sqlalchemy import select
                async with AsyncSessionLocal() as db:
                    u_stmt = select(User).where((User.id == user_id) | (User.username == user_id))
                    u_obj = (await db.execute(u_stmt)).scalar_one_or_none()
                    is_stealth = bool(getattr(u_obj, "is_stealth", False)) if u_obj else False
                if not is_stealth:
                    await self._set_online(user_id, True)
                    await self.broadcast_global({"type": "presence_update", "user_id": user_id, "username": username or "", "is_online": True, "status": "online"})
                else:
                    await self._set_online(user_id, False)
            except Exception as _ce:
                pass
        asyncio.create_task(_check_stealth_and_broadcast())

    def disconnect(self, user_id: str, websocket: WebSocket):
        if user_id in self.active_connections:
            self.active_connections[user_id].discard(websocket)
            if not self.active_connections[user_id]:
                del self.active_connections[user_id]

        clean_u = self.id_to_username.get(user_id)
        if clean_u and clean_u in self.active_connections:
            self.active_connections[clean_u].discard(websocket)
            if not self.active_connections[clean_u]:
                del self.active_connections[clean_u]

        if user_id not in self.active_connections:
            # Clean up text subscriptions
            for channel_id in list(self.channel_subscriptions.keys()):
                self.channel_subscriptions[channel_id].discard(user_id)
                if not self.channel_subscriptions[channel_id]:
                    del self.channel_subscriptions[channel_id]

            # Clean up voice room memberships
            for voice_channel_id in list(self.voice_rooms.keys()):
                self.voice_rooms[voice_channel_id].discard(user_id)
                if not self.voice_rooms[voice_channel_id]:
                    del self.voice_rooms[voice_channel_id]

            # Mark user as offline in DB — all their connections are gone
            asyncio.create_task(self._set_online(user_id, False))
            asyncio.create_task(self.broadcast_global({"type": "presence_update", "user_id": user_id, "username": clean_u or "", "is_online": False, "status": "offline"}))

    async def _set_online(self, user_id: str, online: bool):
        """Updates User.is_online in the database. Called on WS connect/disconnect."""
        import asyncio as _aio
        try:
            from app.db.session import AsyncSessionLocal
            from app.db.models.user import User
            from sqlalchemy import select
            async with AsyncSessionLocal() as db:
                stmt = select(User).where((User.id == user_id) | (User.username == user_id))
                user = (await db.execute(stmt)).scalar_one_or_none()
                if user:
                    if getattr(user, "is_stealth", False):
                        user.is_online = False
                    else:
                        user.is_online = online
                    await db.commit()
        except Exception as e:
            import logging
            logging.getLogger(__name__).warning(f"[WS_PRESENCE] Failed to set is_online={online} for {user_id}: {e}")

    def resolve_user_id(self, identifier: str) -> str:
        """Resolves username or display id to canonical user_id."""
        if not identifier:
            return ""
        clean = identifier.strip().lower().removeprefix("@")
        return self.username_to_id.get(clean, identifier.strip())

    async def async_resolve_user_id(self, identifier: str) -> str:
        """Resolves username or UUID with in-memory check then DB fallback."""
        if not identifier:
            return ""
        clean = identifier.strip().lower().removeprefix("@")
        if clean in self.username_to_id:
            return self.username_to_id[clean]
        if identifier.strip() in self.id_to_username:
            return identifier.strip()

        # Database fallback
        try:
            async with AsyncSessionLocal() as db:
                stmt = select(User).where(
                    (User.id == identifier.strip()) | (func.lower(User.username) == clean)
                )
                user = (await db.execute(stmt)).scalar_one_or_none()
                if user:
                    self.username_to_id[user.username.lower()] = user.id
                    self.id_to_username[user.id] = user.username.lower()
                    return user.id
        except Exception:
            pass
        return identifier.strip()

    def subscribe_channel(self, user_id: str, channel_id: str):
        if not channel_id:
            return
        ch_clean = str(channel_id).strip().removeprefix("#")
        variants = {
            str(channel_id),
            ch_clean,
            ch_clean.lower(),
            ch_clean.replace("-", "_"),
            ch_clean.replace("-", "_").lower(),
            ch_clean.replace("_", "-"),
            ch_clean.replace("_", "-").lower(),
        }
        for key in variants:
            if key not in self.channel_subscriptions:
                self.channel_subscriptions[key] = set()
            self.channel_subscriptions[key].add(user_id)

    def join_voice_room(self, user_id: str, voice_channel_id: str) -> Set[str]:
        if voice_channel_id not in self.voice_rooms:
            self.voice_rooms[voice_channel_id] = set()

        # Return existing peer IDs in room before adding current user
        existing_peers = set(self.voice_rooms[voice_channel_id]) - {user_id}
        self.voice_rooms[voice_channel_id].add(user_id)
        return existing_peers

    def leave_voice_room(self, user_id: str, voice_channel_id: str) -> Set[str]:
        if voice_channel_id in self.voice_rooms:
            self.voice_rooms[voice_channel_id].discard(user_id)
            remaining_peers = set(self.voice_rooms[voice_channel_id])
            if not self.voice_rooms[voice_channel_id]:
                del self.voice_rooms[voice_channel_id]
            return remaining_peers
        return set()

    async def send_personal_event(self, user_id: str, event: Union[dict, str]):
        """Sends an event directly to a specific user's active sockets concurrently with write timeout."""
        if not user_id:
            return
        payload = fast_dumps(event) if isinstance(event, dict) else str(event)
        sockets = set(self.active_connections.get(user_id, set()))
        clean = user_id.strip().lower()
        if clean in self.username_to_id:
            resolved_id = self.username_to_id[clean]
            sockets.update(self.active_connections.get(resolved_id, set()))
        for uname, uid in self.username_to_id.items():
            if uid == user_id or uname == clean:
                sockets.update(self.active_connections.get(uid, set()))
                sockets.update(self.active_connections.get(uname, set()))

        if not sockets:
            return

        sock_list = list(sockets)
        results = await asyncio.gather(*[_send_safe(ws, payload) for ws in sock_list], return_exceptions=True)

        dead_sockets = {sock_list[i] for i, ok in enumerate(results) if ok is not True}
        if dead_sockets and user_id in self.active_connections:
            self.active_connections[user_id].difference_update(dead_sockets)

    async def broadcast_to_channel(
        self,
        channel_id: Union[str, list[str], set[str]],
        event: Union[dict, str],
        participant_user_ids: Union[list[str], set[str]] = None
    ):
        """Broadcasts an event to all users subscribed to a channel and/or DM participants concurrently with zero redundant serialization."""
        payload = fast_dumps(event) if isinstance(event, dict) else str(event)

        channel_ids = [channel_id] if isinstance(channel_id, str) else list(channel_id)
        # Forward to standalone Rust high-throughput relay asynchronously
        try:
            raw_event = orjson.loads(payload) if isinstance(payload, (str, bytes)) else event
            for ch in channel_ids:
                if ch:
                    asyncio.create_task(_forward_to_rust_relay({"channel_id": str(ch).strip().removeprefix("#"), "payload": raw_event}))
        except Exception:
            pass

        target_users = set()
        for ch in channel_ids:
            if not ch:
                continue
            ch_str = str(ch).strip()
            clean_ch = ch_str.removeprefix("#")
            for k in (
                ch_str,
                clean_ch,
                clean_ch.lower(),
                clean_ch.replace("-", "_"),
                clean_ch.replace("-", "_").lower(),
                clean_ch.replace("_", "-"),
                clean_ch.replace("_", "-").lower(),
            ):
                target_users.update(self.channel_subscriptions.get(k, set()))
        if participant_user_ids:
            target_users.update(participant_user_ids)

        if not target_users:
            return

        # Resolve unique target sockets across all target users
        target_sockets = set()
        for uid in target_users:
            if uid in self.active_connections:
                target_sockets.update(self.active_connections[uid])
            clean = uid.strip().lower()
            if clean in self.username_to_id:
                res_id = self.username_to_id[clean]
                if res_id in self.active_connections:
                    target_sockets.update(self.active_connections[res_id])
            if clean in self.active_connections:
                target_sockets.update(self.active_connections[clean])

        if target_sockets:
            sock_list = list(target_sockets)
            results = await asyncio.gather(*[_send_safe(ws, payload) for ws in sock_list], return_exceptions=True)
            dead_sockets = {sock_list[i] for i, ok in enumerate(results) if ok is not True}
            if dead_sockets:
                for uid in target_users:
                    if uid in self.active_connections:
                        self.active_connections[uid].difference_update(dead_sockets)

    async def broadcast_to_voice_room(self, room_id: str, event: Union[dict, str], exclude_user_id: str = None):
        """Relays audio chunk or voice state to all other participants in the room concurrently."""
        peers = self.voice_rooms.get(room_id, set())
        payload = fast_dumps(event) if isinstance(event, dict) else str(event)
        # Forward to standalone Rust high-throughput relay asynchronously
        try:
            raw_event = orjson.loads(payload) if isinstance(payload, (str, bytes)) else event
            asyncio.create_task(_forward_to_rust_relay({"room_id": room_id, "payload": raw_event, "exclude_user_id": exclude_user_id}))
        except Exception:
            pass

        if not peers:
            return
        target_peers = [p for p in peers if p != exclude_user_id]
        if not target_peers:
            return

        target_sockets = set()
        for peer_id in target_peers:
            if peer_id in self.active_connections:
                target_sockets.update(self.active_connections[peer_id])
            clean = peer_id.strip().lower()
            if clean in self.username_to_id:
                res_id = self.username_to_id[clean]
                if res_id in self.active_connections:
                    target_sockets.update(self.active_connections[res_id])
            if clean in self.active_connections:
                target_sockets.update(self.active_connections[clean])

        if target_sockets:
            await asyncio.gather(*[_send_safe(ws, payload) for ws in target_sockets], return_exceptions=True)

    async def broadcast_to_all(self, event: Union[dict, str]):
        """Broadcasts an event to every currently connected user socket concurrently."""
        await self.broadcast_global(event)

    async def invite_to_call(
        self,
        caller_id: str,
        caller_username: str,
        caller_name: str,
        caller_avatar: str,
        callee_identifier: str,
        room_id: str
    ) -> str:
        callee_id = await self.async_resolve_user_id(callee_identifier)
        self.pending_calls[room_id] = {
            "room_id": room_id,
            "caller_id": caller_id,
            "caller_username": caller_username,
            "caller_name": caller_name,
            "caller_avatar": caller_avatar,
            "callee_id": callee_id,
            "callee_identifier": callee_identifier,
            "status": "ringing"
        }
        if room_id not in self.voice_rooms:
            self.voice_rooms[room_id] = set()
        self.voice_rooms[room_id].add(caller_id)

        incoming_event = {
            "type": "incoming_call",
            "action": "call:incoming",
            "room_id": room_id,
            "call_id": room_id,
            "caller_id": caller_id,
            "caller_username": caller_username,
            "caller_name": caller_name,
            "caller": caller_username,
            "caller_avatar": caller_avatar,
            "avatar": caller_avatar
        }
        await self.send_personal_event(callee_id, incoming_event)
        try:
            from app.db.session import AsyncSessionLocal
            from app.db.models.user import User
            from sqlalchemy import select
            async with AsyncSessionLocal() as db:
                stmt = select(User).where((User.id == callee_id) | (User.username == callee_identifier))
                callee_user = (await db.execute(stmt)).scalar_one_or_none()
                if callee_user and callee_user.fcm_token:
                    from app.core.push import send_push_notification
                    asyncio.create_task(send_push_notification(
                        fcm_token=callee_user.fcm_token,
                        title="Incoming Call",
                        body=f"{caller_name} is calling you on Connecto",
                        data={
                            "type": "call_invite",
                            "call_id": room_id,
                            "room_id": room_id,
                            "caller_id": caller_id,
                            "caller_username": caller_username,
                            "caller_name": caller_name,
                            "caller_avatar": caller_avatar or "",
                            "avatar": caller_avatar or "",
                            "sender": caller_name,
                            "sender_username": caller_username,
                            "title": "Incoming Call",
                            "body": f"{caller_name} is calling you on Connecto",
                            "reference_id": room_id
                        },
                        notification_type="call_invite"
                    ))
        except Exception as _call_push_err:
            import logging
            logging.getLogger(__name__).warning(f"[WS_CALL_FCM] Failed to trigger call push: {_call_push_err}")
        return callee_id

    async def accept_call(self, callee_id: str, room_id: str):
        call_info = self.pending_calls.get(room_id)
        caller_id = call_info.get("caller_id") if call_info else None

        if room_id not in self.voice_rooms:
            self.voice_rooms[room_id] = set()
        self.voice_rooms[room_id].add(callee_id)
        if caller_id:
            self.voice_rooms[room_id].add(caller_id)

        if call_info:
            call_info["status"] = "connected"

        caller_uname = call_info.get("caller_username", "") if call_info else ""
        callee_uname = call_info.get("callee_identifier", "") if call_info else ""
        event = {
            "type": "call_connected",
            "action": "call:accepted",
            "room_id": room_id,
            "call_id": room_id,
            "caller_id": caller_id,
            "callee_id": callee_id,
            "caller": caller_uname,
            "caller_username": caller_uname,
            "callee": callee_uname,
            "callee_username": callee_uname,
            "target": callee_uname,
            "participants": list(self.voice_rooms[room_id])
        }
        targets = {callee_id}
        if caller_id:
            targets.add(caller_id)
        for t in targets:
            await self.send_personal_event(t, event)

    async def decline_call(self, callee_id: str, room_id: str, reason: str = "declined"):
        call_info = self.pending_calls.pop(room_id, None)
        caller_id = call_info.get("caller_id") if call_info else None
        if caller_id:
            await self.send_personal_event(caller_id, {
                "type": "call_rejected",
                "action": "call:declined",
                "room_id": room_id,
                "call_id": room_id,
                "reason": reason
            })
        self.voice_rooms.pop(room_id, None)

    async def end_call(self, user_id: str, room_id: str = None, target: str = None):
        peers = self.voice_rooms.pop(room_id, set()) if room_id else set()
        call_info = self.pending_calls.pop(room_id, None) if room_id else None
        all_participants = set(peers)
        if call_info:
            if call_info.get("caller_id"):
                all_participants.add(call_info["caller_id"])
            if call_info.get("callee_id"):
                all_participants.add(call_info["callee_id"])
        if target:
            clean_target = str(target).strip()
            all_participants.add(clean_target)
            resolved_t = await self.async_resolve_user_id(clean_target)
            if resolved_t:
                all_participants.add(resolved_t)
        if user_id:
            all_participants.add(user_id)

        effective_room = room_id or "call"
        for p_id in all_participants:
            await self.send_personal_event(p_id, {
                "type": "call_ended",
                "action": "call:ended",
                "room_id": effective_room,
                "call_id": effective_room,
                "ended_by": user_id
            })

ws_manager = ConnectionManager()
