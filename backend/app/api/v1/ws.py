import asyncio
import json
from datetime import datetime, timezone
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from sqlalchemy import select
from app.core.config import settings
from app.core.security import hash_session_token
from app.core.ws import ws_manager
from app.db.models.user import User, UserSession
from app.db.session import AsyncSessionLocal

router = APIRouter(tags=["WebSocket"])

async def authenticate_ws_session(websocket: WebSocket) -> User | None:
    import urllib.parse
    from datetime import timedelta
    from sqlalchemy import func
    session_token = websocket.cookies.get(settings.COOKIE_NAME) or websocket.query_params.get("token")
    raw_username = (
        websocket.query_params.get("username") or
        websocket.path_params.get("username") or
        websocket.scope.get("ws_username")
    )
    username = urllib.parse.unquote(raw_username).strip() if raw_username else None
    user_id_param = websocket.query_params.get("user_id")

    async with AsyncSessionLocal() as db:
        if session_token:
            token_hash = hash_session_token(session_token)
            now = datetime.now(timezone.utc)
            stmt = (
                select(UserSession)
                .where(
                    UserSession.session_token_hash == token_hash,
                    UserSession.expires_at > now
                )
            )
            session = (await db.execute(stmt)).scalar_one_or_none()
            if session:
                user_stmt = select(User).where(User.id == session.user_id)
                user = (await db.execute(user_stmt)).scalar_one_or_none()
                if user:
                    # Sliding session renewal: keep session alive while actively connected
                    session.expires_at = now + timedelta(days=settings.SESSION_EXPIRE_DAYS)
                    await db.commit()
                    return user
            else:
                # Check recent session for token
                old_stmt = select(UserSession).where(UserSession.session_token_hash == token_hash)
                old_session = (await db.execute(old_stmt)).scalar_one_or_none()
                if old_session:
                    user_stmt = select(User).where(User.id == old_session.user_id)
                    user = (await db.execute(user_stmt)).scalar_one_or_none()
                    if user:
                        old_session.expires_at = now + timedelta(days=settings.SESSION_EXPIRE_DAYS)
                        await db.commit()
                        return user

        # Security Hardening: Only explicitly identified guest / anonymous sessions are permitted without a token.
        # Registered user identities strictly require a valid session token to prevent impersonation.
        if username and (username.startswith("guest_") or username.startswith("anon-")):
            clean_guest = username.strip()[:32]
            return User(
                id=f"guest_{clean_guest}",
                username=clean_guest,
                display_name=f"Guest {clean_guest.removeprefix('guest_')[:6]}",
                is_admin=False
            )

        # Allow test runner to simulate users in explicit test environment only
        if getattr(settings, "ENV", "production") == "test":
            if user_id_param or username:
                query = select(User)
                if user_id_param and len(user_id_param) == 36:
                    query = query.where(User.id == user_id_param)
                elif username:
                    query = query.where(func.lower(User.username) == username.lower())
                return (await db.execute(query)).scalar_one_or_none()

        return None

@router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    user = await authenticate_ws_session(websocket)
    if not user:
        try:
            await websocket.accept()
            await websocket.close(code=4001, reason="Unauthorized")
        except Exception:
            pass
        return

    # Mark client_type on websocket scope
    if "client_type" not in websocket.scope:
        websocket.scope["client_type"] = "app" if websocket.query_params.get("user_id") else "web"

    await ws_manager.connect(user.id, websocket, user.username)

    # Auto-subscribe channel from path if provided
    ch_id = websocket.path_params.get("channel_id") or websocket.scope.get("channel_id")
    if ch_id:
        ws_manager.subscribe_channel(user.id, ch_id)

    # If there are any pending calls ringing for this user, alert them immediately
    for r_id, call_info in list(ws_manager.pending_calls.items()):
        if (call_info.get("callee_id") == user.id or
            call_info.get("callee_identifier", "").lower() == user.username.lower()) and \
            call_info.get("status") == "ringing":
            await websocket.send_text(json.dumps({
                "type": "incoming_call",
                "room_id": r_id,
                "caller_id": call_info.get("caller_id"),
                "caller_username": call_info.get("caller_username"),
                "caller_name": call_info.get("caller_name"),
                "caller_avatar": call_info.get("caller_avatar")
            }))

    try:
        while True:
            try:
                raw_text = await asyncio.wait_for(websocket.receive_text(), timeout=45.0)
            except asyncio.TimeoutError:
                # Connection idle with no application text frames: send keepalive ping to maintain proxy stream
                try:
                    await websocket.send_text(json.dumps({"type": "ping"}))
                    continue
                except Exception:
                    break
            try:
                payload = json.loads(raw_text)
                msg_type = payload.get("type") or payload.get("action")

                if msg_type in ("subscribe", "channel:subscribe"):
                    channels = payload.get("channels", [])
                    channel_id = payload.get("channel_id")
                    if channel_id and channel_id not in channels:
                        channels.append(channel_id)
                    for ch_id in channels:
                        ws_manager.subscribe_channel(user.id, ch_id)
                    await websocket.send_text(json.dumps({"type": "subscribed", "channels": channels}))

                elif msg_type == "ping":
                    await websocket.send_text(json.dumps({"type": "pong"}))

                # --- TYPING INDICATORS ---
                elif msg_type in ("typing_start", "typing:start"):
                    ch_id = (payload.get("channel_id") or payload.get("channel") or "general").strip()
                    await ws_manager.broadcast_to_channel(ch_id, {
                        "type": "typing_start",
                        "channel_id": ch_id,
                        "user_id": user.id,
                        "username": user.username,
                        "display_name": user.display_name or user.username,
                        "avatar_url": user.avatar_url,
                    })

                elif msg_type in ("typing_stop", "typing:stop"):
                    ch_id = (payload.get("channel_id") or payload.get("channel") or "general").strip()
                    await ws_manager.broadcast_to_channel(ch_id, {
                        "type": "typing_stop",
                        "channel_id": ch_id,
                        "user_id": user.id,
                        "username": user.username,
                    })


                # --- 1:1 DIRECT CALL SIGNALING ---
                elif msg_type in ("call_invite", "call:initiate"):
                    target_identifier = (payload.get("target_user") or
                                         payload.get("target") or
                                         payload.get("target_user_id") or
                                         payload.get("callee_id") or
                                         payload.get("callee_username") or
                                         payload.get("callee") or "").strip()
                    room_id = payload.get("room_id") or payload.get("call_id") or f"call_{user.id}_{int(datetime.now().timestamp())}"
                    callee_id = await ws_manager.invite_to_call(
                        caller_id=user.id,
                        caller_username=user.username,
                        caller_name=user.display_name or user.username,
                        caller_avatar=user.avatar_url or payload.get("avatar") or "",
                        callee_identifier=target_identifier,
                        room_id=room_id
                    )
                    await websocket.send_text(json.dumps({
                        "type": "call_ringing",
                        "room_id": room_id,
                        "target_user": target_identifier,
                        "callee_id": callee_id
                    }))

                elif msg_type in ("call_accept", "call:accept"):
                    room_id = payload.get("room_id") or payload.get("call_id")
                    if room_id:
                        await ws_manager.accept_call(user.id, room_id)

                elif msg_type in ("call_decline", "call:decline"):
                    room_id = payload.get("room_id") or payload.get("call_id")
                    reason = payload.get("reason", "declined")
                    if room_id:
                        await ws_manager.decline_call(user.id, room_id, reason)

                elif msg_type in ("call_end", "call:end"):
                    room_id = payload.get("room_id") or payload.get("call_id")
                    if room_id:
                        await ws_manager.end_call(user.id, room_id)

                # --- REAL-TIME FULL-DUPLEX AUDIO PACKET RELAY (FALLBACK) ---
                elif msg_type in ("voice_data", "voice:data"):
                    room_id = payload.get("room_id") or payload.get("channel_id")
                    audio_data = payload.get("audio") or payload.get("payload")
                    if room_id and audio_data:
                        # Broadcast audio chunk to all other peers in the room
                        await ws_manager.broadcast_to_voice_room(
                            room_id=room_id,
                            event={
                                "type": "voice_data",
                                "room_id": room_id,
                                "channel_id": room_id,
                                "sender_id": user.id,
                                "sender_name": user.display_name or user.username,
                                "audio": audio_data,
                                "payload": audio_data
                            },
                            exclude_user_id=user.id
                        )

                # --- REAL-TIME CHAT MESSAGE RELAY ---
                elif msg_type in ("chat_message", "message_send", "message:send"):
                    ch_id = payload.get("channel_id") or payload.get("channel")
                    content = payload.get("content") or payload.get("text") or payload.get("message")
                    if ch_id and content:
                        event_payload = {
                            "type": "message_created",
                            "channel_id": ch_id,
                            "data": {
                                "id": payload.get("id") or f"msg_{int(datetime.now().timestamp())}",
                                "channel_id": ch_id,
                                "sender_id": user.id,
                                "sender_username": user.username,
                                "sender_display_name": user.display_name or user.username,
                                "sender_avatar_url": user.avatar_url,
                                "content": content,
                                "created_at": datetime.now(timezone.utc).isoformat()
                            }
                        }
                        await ws_manager.broadcast_to_channel(ch_id, event_payload)

                # --- WEBRTC & VOICE ROOM SIGNALING ---
                elif msg_type in ("voice_join", "voice:join"):
                    ch_id = payload.get("channel_id") or payload.get("room_id")
                    if ch_id:
                        existing_peers = ws_manager.join_voice_room(user.id, ch_id)
                        # Notify joining user of existing peers in room
                        await websocket.send_text(json.dumps({
                            "type": "voice_room_joined",
                            "channel_id": ch_id,
                            "room_id": ch_id,
                            "existing_peer_ids": list(existing_peers)
                        }))
                        # Notify existing peers that a new user joined
                        for peer_id in existing_peers:
                            await ws_manager.send_personal_event(peer_id, {
                                "type": "voice_peer_joined",
                                "channel_id": ch_id,
                                "room_id": ch_id,
                                "peer_id": user.id,
                                "peer_username": user.username,
                                "peer_display_name": user.display_name
                            })

                elif msg_type in ("voice_leave", "voice:leave"):
                    ch_id = payload.get("channel_id") or payload.get("room_id")
                    if ch_id:
                        remaining_peers = ws_manager.leave_voice_room(user.id, ch_id)
                        for peer_id in remaining_peers:
                            await ws_manager.send_personal_event(peer_id, {
                                "type": "voice_peer_left",
                                "channel_id": ch_id,
                                "room_id": ch_id,
                                "peer_id": user.id
                            })

                elif msg_type in ("voice_offer", "call:offer", "voice_answer", "call:answer", "voice_ice_candidate", "call:ice-candidate"):
                    target_id = (
                        payload.get("target_user_id") or
                        payload.get("target") or
                        payload.get("target_user") or
                        payload.get("callee_id") or
                        payload.get("callee") or ""
                    ).strip()

                    room_id = payload.get("room_id") or payload.get("call_id") or payload.get("channel_id")

                    if not target_id and room_id:
                        call_info = ws_manager.pending_calls.get(room_id)
                        if call_info:
                            if call_info.get("caller_id") == user.id:
                                target_id = call_info.get("callee_id") or call_info.get("callee_identifier")
                            else:
                                target_id = call_info.get("caller_id") or call_info.get("caller_username")
                        if not target_id:
                            peers = ws_manager.voice_rooms.get(room_id, set()) - {user.id}
                            if peers:
                                target_id = list(peers)[0]

                    if target_id:
                        resolved_target = await ws_manager.async_resolve_user_id(target_id)
                        is_offer = msg_type in ("voice_offer", "call:offer")
                        is_answer = msg_type in ("voice_answer", "call:answer")
                        is_ice = msg_type in ("voice_ice_candidate", "call:ice-candidate")

                        relay_payload = dict(payload)
                        relay_payload["sender_user_id"] = user.id
                        relay_payload["sender_username"] = user.username
                        relay_payload["caller"] = user.username
                        relay_payload["room_id"] = room_id
                        relay_payload["call_id"] = room_id
                        relay_payload["channel_id"] = room_id
                        relay_payload["target_user_id"] = resolved_target
                        relay_payload["target"] = target_id

                        if is_offer or is_answer:
                            expected_type = "offer" if is_offer else "answer"
                            raw_sdp = payload.get("sdp")
                            if isinstance(raw_sdp, dict):
                                sdp_str = raw_sdp.get("sdp", "")
                                sdp_dict = raw_sdp
                            elif isinstance(raw_sdp, str):
                                sdp_str = raw_sdp
                                sdp_dict = {"type": expected_type, "sdp": sdp_str}
                            else:
                                sdp_str = ""
                                sdp_dict = {"type": expected_type, "sdp": ""}

                            relay_payload["sdp_str"] = sdp_str
                            relay_payload["sdp_object"] = sdp_dict

                            app_event = dict(relay_payload)
                            app_event["type"] = f"voice_{expected_type}"
                            app_event["action"] = f"voice:{expected_type}"
                            app_event["sdp"] = sdp_str

                            web_event = dict(relay_payload)
                            web_event["type"] = f"call:{expected_type}"
                            web_event["action"] = f"call:{expected_type}"
                            web_event["sdp"] = sdp_dict

                            target_sockets = set(ws_manager.active_connections.get(resolved_target, set()))
                            target_sockets.update(ws_manager.active_connections.get(target_id, set()))
                            clean_t = target_id.strip().lower()
                            target_sockets.update(ws_manager.active_connections.get(clean_t, set()))
                            target_sockets.discard(websocket)  # Never echo back to sender

                            dead_sockets = set()
                            for ws in list(target_sockets):
                                ctype = ws.scope.get("client_type")
                                evt_to_send = app_event if ctype == "app" else web_event
                                try:
                                    await ws.send_text(json.dumps(evt_to_send))
                                except Exception:
                                    dead_sockets.add(ws)
                            if dead_sockets and resolved_target in ws_manager.active_connections:
                                ws_manager.active_connections[resolved_target].difference_update(dead_sockets)

                        elif is_ice:
                            cand = payload.get("candidate")
                            if isinstance(cand, str):
                                try:
                                    cand_dict = json.loads(cand)
                                except Exception:
                                    cand_dict = {"candidate": cand, "sdpMid": "0", "sdpMLineIndex": 0}
                            elif isinstance(cand, dict):
                                cand_dict = cand
                            else:
                                cand_dict = cand

                            app_ice = dict(relay_payload)
                            app_ice["type"] = "voice_ice_candidate"
                            app_ice["action"] = "voice:ice-candidate"
                            app_ice["candidate"] = cand_dict

                            web_ice = dict(relay_payload)
                            web_ice["type"] = "call:ice-candidate"
                            web_ice["action"] = "call:ice-candidate"
                            web_ice["candidate"] = cand_dict

                            target_sockets = set(ws_manager.active_connections.get(resolved_target, set()))
                            target_sockets.update(ws_manager.active_connections.get(target_id, set()))
                            clean_t = target_id.strip().lower()
                            target_sockets.update(ws_manager.active_connections.get(clean_t, set()))
                            target_sockets.discard(websocket)  # Never echo back to sender

                            dead_sockets = set()
                            for ws in list(target_sockets):
                                ctype = ws.scope.get("client_type")
                                evt_to_send = app_ice if ctype == "app" else web_ice
                                try:
                                    await ws.send_text(json.dumps(evt_to_send))
                                except Exception:
                                    dead_sockets.add(ws)
                            if dead_sockets and resolved_target in ws_manager.active_connections:
                                ws_manager.active_connections[resolved_target].difference_update(dead_sockets)

            except json.JSONDecodeError:
                pass

    except Exception as _ws_err:
        pass
    finally:
        ws_manager.disconnect(user.id, websocket)