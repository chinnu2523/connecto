// ============================================================================
// CONNECTO ZERO-INSTALL GUEST ROOMS (connecto.fun/room/{roomCode})
// Lightweight Ephemeral Real-Time Chat & WebRTC Voice Engine
// ============================================================================

export async function generateGuestToken(roomCode, guestId, displayName, env) {
  const ts = Date.now();
  const safeName = encodeURIComponent(displayName || "Guest");
  const payload = `${roomCode}_${guestId}_${safeName}_${ts}`;
  const secret = (env && env.SECRET_KEY) || "connecto_edge_secret_fallback_2026";
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(payload));
  const sigHex = Array.from(new Uint8Array(sig)).map(b => b.toString(16).padStart(2, "0")).join("");
  return `cf_guest_${payload}.${sigHex}`;
}

export async function verifyGuestToken(token, env) {
  if (!token || !token.startsWith("cf_guest_")) return null;
  const dotIdx = token.lastIndexOf(".");
  if (dotIdx === -1) return null;
  const payload = token.substring(9, dotIdx);
  const providedSig = token.substring(dotIdx + 1);
  const parts = payload.split("_");
  if (parts.length < 4) return null;

  const roomCode = parts[0];
  const guestId = parts[1];
  const encName = parts.slice(2, -1).join("_");
  const ts = parseInt(parts[parts.length - 1], 10);
  if (isNaN(ts) || Date.now() - ts > 24 * 3600 * 1000) return null; // 24-hour expiration

  const secret = (env && env.SECRET_KEY) || "connecto_edge_secret_fallback_2026";
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(payload));
  const expectedSigHex = Array.from(new Uint8Array(sig)).map(b => b.toString(16).padStart(2, "0")).join("");
  if (providedSig.length !== expectedSigHex.length) return null;

  let diff = 0;
  for (let i = 0; i < providedSig.length; i++) {
    diff |= providedSig.charCodeAt(i) ^ expectedSigHex.charCodeAt(i);
  }
  if (diff !== 0) return null;

  return {
    isGuest: true,
    roomCode: roomCode,
    guestId: guestId,
    displayName: decodeURIComponent(encName)
  };
}

export async function purgeExpiredGuestRooms(env) {
  if (!env || !env.DB) return { deactivated: 0, purged: 0 };
  try {
    const deactRes = await env.DB.prepare(
      "UPDATE guest_rooms SET is_active = 0 WHERE expires_at <= datetime('now') AND is_active = 1"
    ).run().catch(() => ({ meta: { changes: 0 } }));

    await env.DB.prepare(
      "DELETE FROM guest_room_messages WHERE created_at <= datetime('now', '-3 days')"
    ).run().catch(() => {});

    const delRes = await env.DB.prepare(
      "DELETE FROM guest_rooms WHERE expires_at <= datetime('now', '-7 days')"
    ).run().catch(() => ({ meta: { changes: 0 } }));

    return {
      deactivated: deactRes?.meta?.changes || 0,
      purged: delRes?.meta?.changes || 0
    };
  } catch (err) {
    console.warn("[purgeExpiredGuestRooms] Error:", err.message);
    return { deactivated: 0, purged: 0, error: err.message };
  }
}

export async function handleGuestRoomsApi(request, url, env, ctx, resolveUserFromRequest, jsonResponse, getRealtimeRoom, broadcastRealtime) {
  const path = url.pathname.replace(/^\/api\/v1\//, "/api/");
  const method = request.method;

  // 1. POST /api/guest-rooms (Host creates room)
  if (path === "/api/guest-rooms" && method === "POST") {
    const caller = await resolveUserFromRequest(request, env);
    if (!caller) {
      return jsonResponse({ error: "Authentication required to create guest room" }, 401);
    }
    const body = await request.json().catch(() => ({}));
    const alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    let roomCode = "";
    const randArr = new Uint8Array(6);
    crypto.getRandomValues(randArr);
    for (let i = 0; i < 6; i++) {
      roomCode += alphabet.charAt(randArr[i] % alphabet.length);
    }

    const hostName = caller.display_name || caller.username || "Host";
    const title = (body.title && body.title.trim()) || `${hostName}'s Room`;
    const allowVoice = body.allow_voice !== false ? 1 : 0;
    const expiresAt = new Date(Date.now() + 24 * 3600 * 1000).toISOString();

    if (env.DB) {
      await env.DB.prepare(
        "INSERT INTO guest_rooms (room_code, host_user_id, title, allow_voice, is_active, expires_at, created_at) VALUES (?, ?, ?, ?, 1, ?, datetime('now'))"
      ).bind(roomCode, caller.id || caller.username, title, allowVoice, expiresAt).run();
    }

    const roomUrl = `https://connecto.fun/room/${roomCode}`;
    return jsonResponse({
      success: true,
      room_code: roomCode,
      room_url: roomUrl,
      title: title,
      host_user_id: caller.id || caller.username,
      host_username: caller.username,
      host_display_name: hostName,
      allow_voice: Boolean(allowVoice),
      expires_at: expiresAt
    });
  }

  // 2. GET /api/guest-rooms/:roomCode (Fetch room info)
  const roomCodeMatch = path.match(/^\/api\/guest-rooms\/([A-Za-z0-9_-]+)$/);
  if (roomCodeMatch && method === "GET") {
    const roomCode = roomCodeMatch[1].toUpperCase();
    if (!env.DB) {
      return jsonResponse({ error: "Database unavailable" }, 503);
    }

    const room = await env.DB.prepare(
      `SELECT g.*, u.username as host_username, u.display_name as host_display_name, u.avatar_url as host_avatar_url 
       FROM guest_rooms g 
       LEFT JOIN users u ON (g.host_user_id = u.id OR g.host_user_id = u.username) 
       WHERE UPPER(g.room_code) = ? LIMIT 1`
    ).bind(roomCode).first().catch(() => null);

    if (!room || room.is_active === 0) {
      return jsonResponse({ success: false, error: "Guest room not found or has been closed by host" }, 404);
    }

    if (new Date(room.expires_at).getTime() <= Date.now()) {
      return jsonResponse({ success: false, error: "Guest room has expired" }, 410);
    }

    // Recent room messages
    const recentMsgs = await env.DB.prepare(
      "SELECT id, room_code, sender_name, is_guest, content, is_whisper, created_at FROM guest_room_messages WHERE room_code = ? ORDER BY created_at ASC LIMIT 50"
    ).bind(room.room_code).all().catch(() => ({ results: [] }));

    return jsonResponse({
      success: true,
      room: {
        room_code: room.room_code,
        title: room.title,
        allow_voice: Boolean(room.allow_voice),
        expires_at: room.expires_at,
        created_at: room.created_at,
        host: {
          username: room.host_username || room.host_user_id,
          display_name: room.host_display_name || room.host_username || "Host",
          avatar_url: room.host_avatar_url || ""
        },
        recent_messages: recentMsgs?.results || []
      }
    });
  }

  // 3. POST /api/guest-rooms/:roomCode/join (Guest enters display name, gets token)
  const joinMatch = path.match(/^\/api\/guest-rooms\/([A-Za-z0-9_-]+)\/join$/);
  if (joinMatch && method === "POST") {
    const roomCode = joinMatch[1].toUpperCase();
    if (!env.DB) return jsonResponse({ error: "Database unavailable" }, 503);

    const room = await env.DB.prepare(
      "SELECT room_code, is_active, expires_at, title FROM guest_rooms WHERE UPPER(room_code) = ? LIMIT 1"
    ).bind(roomCode).first().catch(() => null);

    if (!room || room.is_active === 0) {
      return jsonResponse({ success: false, error: "Guest room not found or closed" }, 404);
    }
    if (new Date(room.expires_at).getTime() <= Date.now()) {
      return jsonResponse({ success: false, error: "Guest room has expired" }, 410);
    }

    const body = await request.json().catch(() => ({}));
    const displayName = (body.display_name || body.name || "Guest").trim().substring(0, 32) || "Guest";
    const guestId = Math.random().toString(36).substring(2, 8);
    const guestUsername = `guest_${guestId}`;
    const token = await generateGuestToken(room.room_code, guestId, displayName, env);

    return jsonResponse({
      success: true,
      room_code: room.room_code,
      token: token,
      username: guestUsername,
      display_name: displayName,
      title: room.title
    });
  }

  // 4. POST /api/guest-rooms/:roomCode/end (Host closes room)
  const endMatch = path.match(/^\/api\/guest-rooms\/([A-Za-z0-9_-]+)\/end$/);
  if (endMatch && method === "POST") {
    const caller = await resolveUserFromRequest(request, env);
    if (!caller) return jsonResponse({ error: "Authentication required" }, 401);

    const roomCode = endMatch[1].toUpperCase();
    if (!env.DB) return jsonResponse({ error: "Database unavailable" }, 503);

    const room = await env.DB.prepare(
      "SELECT room_code, host_user_id FROM guest_rooms WHERE UPPER(room_code) = ? LIMIT 1"
    ).bind(roomCode).first().catch(() => null);

    if (!room) return jsonResponse({ error: "Room not found" }, 404);
    const callerId = String(caller.id || "").toLowerCase();
    const callerUser = String(caller.username || "").toLowerCase();
    const hostId = String(room.host_user_id || "").toLowerCase();

    if (callerId !== hostId && callerUser !== hostId && callerUser !== "chinnu" && callerUser !== "chinnu14754x") {
      return jsonResponse({ error: "Only the room host can close this room" }, 403);
    }

    await env.DB.prepare(
      "UPDATE guest_rooms SET is_active = 0 WHERE room_code = ?"
    ).bind(room.room_code).run().catch(() => {});

    // Broadcast room_ended event to all connected sockets
    if (broadcastRealtime) {
      await broadcastRealtime(env, {
        type: "room_ended",
        channel_id: `room-${room.room_code}`,
        room_code: room.room_code,
        message: "This guest room has been closed by the host."
      });
    }

    return jsonResponse({
      success: true,
      message: "Guest room closed successfully",
      room_code: room.room_code
    });
  }

  return jsonResponse({ error: "Endpoint not found" }, 404);
}

export function renderGuestRoomHtml(roomCode) {
  const safeCode = (roomCode || "").toUpperCase().replace(/[^A-Z0-9_-]/g, "");
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Connecto Guest Room • ${safeCode}</title>
  <meta name="description" content="Instant zero-install guest room on Connecto. Real-time chat, whisper messages, and WebRTC voice.">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    :root {
      --bg: #0A0B0E;
      --card-bg: #12151E;
      --input-bg: #181C28;
      --border: rgba(255, 255, 255, 0.09);
      --accent-emerald: #10B981;
      --accent-indigo: #6366F1;
      --text: #F8FAFC;
      --text-muted: #94A3B8;
    }
    body {
      background: var(--bg);
      color: var(--text);
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      overflow-x: hidden;
      -webkit-font-smoothing: antialiased;
    }
    /* Top Conversion Banner */
    .top-banner {
      background: linear-gradient(90deg, rgba(99, 102, 241, 0.20) 0%, rgba(16, 185, 129, 0.20) 100%);
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      padding: 10px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      font-size: 13px;
      z-index: 50;
    }
    .top-banner a {
      background: #10B981;
      color: #FFFFFF;
      text-decoration: none;
      font-weight: 700;
      padding: 6px 14px;
      border-radius: 8px;
      font-size: 12px;
      white-space: nowrap;
      transition: opacity 0.2s;
    }
    .top-banner a:hover { opacity: 0.9; }
    .banner-close {
      background: transparent;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      font-size: 16px;
      padding: 2px 6px;
    }

    /* Main Container */
    .app-root {
      flex: 1;
      display: flex;
      flex-direction: column;
      max-width: 768px;
      width: 100%;
      margin: 0 auto;
      padding: 16px;
      height: calc(100vh - 46px);
    }

    /* Landing Card (Pre-Join) */
    .landing-card {
      margin: auto;
      width: 100%;
      max-width: 440px;
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 24px;
      padding: 32px 24px;
      text-align: center;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
    }
    .host-avatar {
      width: 76px;
      height: 76px;
      border-radius: 50%;
      margin: 0 auto 16px;
      border: 2px solid var(--accent-emerald);
      object-fit: cover;
      display: block;
      background: #181C28;
    }
    .room-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: rgba(16, 185, 129, 0.12);
      color: var(--accent-emerald);
      font-size: 12px;
      font-weight: 700;
      padding: 4px 10px;
      border-radius: 20px;
      margin-bottom: 12px;
    }
    .room-badge .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--accent-emerald);
      box-shadow: 0 0 8px var(--accent-emerald);
    }
    .room-title { font-size: 20px; font-weight: 700; margin-bottom: 6px; }
    .host-name { font-size: 14px; color: var(--text-muted); margin-bottom: 24px; }
    .input-group { margin-bottom: 18px; text-align: left; }
    .input-label { display: block; font-size: 12px; font-weight: 600; color: var(--text-muted); margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
    .text-input {
      width: 100%;
      background: var(--input-bg);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 12px 16px;
      color: var(--text);
      font-size: 15px;
      font-family: inherit;
      outline: none;
      transition: border-color 0.2s;
    }
    .text-input:focus { border-color: var(--accent-indigo); }
    .btn-join {
      width: 100%;
      background: linear-gradient(135deg, var(--accent-indigo), #4F46E5);
      color: #FFF;
      border: none;
      border-radius: 14px;
      padding: 14px;
      font-size: 15px;
      font-weight: 700;
      cursor: pointer;
      font-family: inherit;
      box-shadow: 0 8px 20px rgba(99, 102, 241, 0.35);
      transition: transform 0.15s, opacity 0.15s;
    }
    .btn-join:hover { opacity: 0.95; }
    .btn-join:active { transform: scale(0.98); }

    /* Active Room View */
    .chat-view {
      display: none;
      flex-direction: column;
      height: 100%;
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 20px;
      overflow: hidden;
    }
    .chat-header {
      padding: 14px 18px;
      background: rgba(18, 21, 30, 0.95);
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    .chat-header-title { font-size: 15px; font-weight: 700; }
    .chat-header-sub { font-size: 12px; color: var(--text-muted); }
    .voice-controls {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .btn-voice {
      background: rgba(16, 185, 129, 0.15);
      border: 1px solid rgba(16, 185, 129, 0.35);
      color: var(--accent-emerald);
      padding: 6px 12px;
      border-radius: 10px;
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
    .btn-voice.active {
      background: var(--accent-emerald);
      color: #FFF;
    }

    /* Message Stream */
    .message-stream {
      flex: 1;
      padding: 16px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .msg-row {
      display: flex;
      flex-direction: column;
      max-width: 80%;
    }
    .msg-row.me { align-self: flex-end; }
    .msg-row.other { align-self: flex-start; }
    .msg-meta {
      font-size: 11px;
      color: var(--text-muted);
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .guest-pill {
      font-size: 9px;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 6px;
      background: rgba(99, 102, 241, 0.20);
      color: #818CF8;
      border: 1px solid rgba(99, 102, 241, 0.30);
    }
    .host-pill {
      font-size: 9px;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 6px;
      background: rgba(16, 185, 129, 0.20);
      color: #34D399;
      border: 1px solid rgba(16, 185, 129, 0.30);
    }
    .bubble {
      padding: 10px 14px;
      border-radius: 16px;
      font-size: 14px;
      line-height: 1.45;
      position: relative;
      word-break: break-word;
    }
    .msg-row.me .bubble {
      background: linear-gradient(135deg, var(--accent-indigo), #4F46E5);
      color: #FFF;
      border-bottom-right-radius: 4px;
    }
    .msg-row.other .bubble {
      background: var(--input-bg);
      border: 1px solid var(--border);
      color: var(--text);
      border-bottom-left-radius: 4px;
    }

    /* Whisper Frosted Bubble with Hold to Reveal */
    .bubble.whisper {
      position: relative;
      overflow: hidden;
      cursor: pointer;
      user-select: none;
      -webkit-user-select: none;
    }
    .bubble.whisper .whisper-content {
      filter: blur(10px);
      transition: filter 180ms cubic-bezier(0.4, 0, 0.2, 1);
    }
    .bubble.whisper.revealed .whisper-content {
      filter: blur(0px);
    }
    .whisper-veil {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 600;
      color: rgba(255, 255, 255, 0.9);
      background: rgba(0, 0, 0, 0.25);
      backdrop-filter: blur(2px);
      pointer-events: none;
      transition: opacity 180ms;
    }
    .bubble.whisper.revealed .whisper-veil {
      opacity: 0;
    }

    /* Chat Composer */
    .chat-composer {
      padding: 12px 16px;
      background: rgba(18, 21, 30, 0.95);
      border-top: 1px solid var(--border);
      display: flex;
      align-items: center;
      gap: 10px;
      position: relative;
    }
    .composer-input {
      flex: 1;
      background: var(--input-bg);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 10px 14px;
      color: var(--text);
      font-size: 14px;
      outline: none;
      font-family: inherit;
    }
    .composer-input:focus { border-color: var(--accent-indigo); }
    .btn-whisper-toggle {
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid var(--border);
      color: var(--text-muted);
      padding: 8px 12px;
      border-radius: 10px;
      font-size: 12px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s;
    }
    .btn-whisper-toggle.active {
      background: rgba(99, 102, 241, 0.25);
      color: #818CF8;
      border-color: #818CF8;
    }
    .btn-send {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: var(--accent-emerald);
      border: none;
      color: #FFF;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 16px;
      transition: transform 0.15s;
    }
    .btn-send:active { transform: scale(0.92); }
  </style>
</head>
<body>
  <div class="top-banner" id="topBanner">
    <span>✨ Enjoying Connecto? Get the mobile app to keep your chats, friends & HD voice calls</span>
    <div style="display:flex;align-items:center;gap:8px;">
      <a href="https://connecto.fun/download" target="_blank">Install App</a>
      <button class="banner-close" onclick="document.getElementById('topBanner').style.display='none'">✕</button>
    </div>
  </div>

  <div class="app-root">
    <!-- Unjoined Landing View -->
    <div class="landing-card" id="landingCard">
      <img id="hostAvatar" class="host-avatar" src="/uploads/avatars/chinnu_avatar_verified.webp" alt="Host">
      <div class="room-badge">
        <span class="dot"></span>
        <span id="activeCount">Zero-Install Guest Room</span>
      </div>
      <h1 class="room-title" id="roomTitle">Connecto Guest Room</h1>
      <p class="host-name" id="hostSubtitle">Hosted on Connecto</p>

      <div class="input-group">
        <label class="input-label">Your Display Name</label>
        <input type="text" id="nameInput" class="text-input" placeholder="e.g. Alex" maxlength="28" autocomplete="off">
      </div>

      <button class="btn-join" id="btnJoin" onclick="joinRoom()">Join Room</button>
    </div>

    <!-- Active In-Room Chat View -->
    <div class="chat-view" id="chatView">
      <div class="chat-header">
        <div>
          <div class="chat-header-title" id="headerRoomTitle">Guest Room</div>
          <div class="chat-header-sub" id="headerOnlineCount">🟢 Connected</div>
        </div>
        <div class="voice-controls">
          <button class="btn-voice" id="btnVoiceToggle" onclick="toggleVoice()">🎙️ Join Voice</button>
        </div>
      </div>

      <div class="message-stream" id="messageStream"></div>

      <div class="chat-composer">
        <button class="btn-whisper-toggle" id="btnWhisperToggle" onclick="toggleWhisperMode()">🫧 Whisper</button>
        <input type="text" class="composer-input" id="messageInput" placeholder="Type a message..." onkeydown="if(event.key==='Enter') sendMessage()">
        <button class="btn-send" id="btnSend" onclick="sendMessage()">➤</button>
      </div>
    </div>
  </div>

  <script>
    const roomCode = "${safeCode}";
    let guestToken = null;
    let guestUsername = null;
    let guestDisplayName = "Guest";
    let ws = null;
    let isWhisperActive = false;
    let isVoiceActive = false;
    let peerConn = null;
    let localStream = null;

    async function loadRoomDetails() {
      try {
        const res = await fetch('/api/guest-rooms/' + roomCode);
        const data = await res.json();
        if (data.success && data.room) {
          document.getElementById('roomTitle').textContent = data.room.title || 'Guest Room';
          document.getElementById('hostSubtitle').textContent = 'Hosted by @' + (data.room.host.display_name || data.room.host.username);
          if (data.room.host.avatar_url) {
            document.getElementById('hostAvatar').src = data.room.host.avatar_url;
          }
          document.getElementById('headerRoomTitle').textContent = data.room.title || 'Guest Room';
        } else {
          document.getElementById('roomTitle').textContent = 'Room Not Found';
          document.getElementById('hostSubtitle').textContent = data.error || 'This guest room has expired or ended.';
          document.getElementById('btnJoin').style.display = 'none';
        }
      } catch (e) {
        console.warn('Failed to load room info:', e);
      }
    }
    loadRoomDetails();

    async function joinRoom() {
      const name = (document.getElementById('nameInput').value || '').trim() || 'Guest';
      guestDisplayName = name;
      try {
        const res = await fetch('/api/guest-rooms/' + roomCode + '/join', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ display_name: name })
        });
        const data = await res.json();
        if (data.success && data.token) {
          guestToken = data.token;
          guestUsername = data.username;
          document.getElementById('landingCard').style.display = 'none';
          document.getElementById('chatView').style.display = 'flex';
          connectWebSocket();
        } else {
          alert(data.error || 'Unable to join guest room.');
        }
      } catch (err) {
        alert('Network error joining room.');
      }
    }

    function connectWebSocket() {
      const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = protocol + '//' + location.host + '/ws/room-' + roomCode + '?token=' + encodeURIComponent(guestToken) + '&username=' + encodeURIComponent(guestUsername) + '&is_guest=true';
      ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        document.getElementById('headerOnlineCount').textContent = '🟢 Online';
        setInterval(() => {
          if (ws && ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify({ type: 'ping' }));
        }, 25000);
      };

      ws.onmessage = (evt) => {
        try {
          const msg = JSON.parse(evt.data);
          if (msg.type === 'new_message' && msg.message) {
            appendMessage(msg.message);
          } else if (msg.type === 'room_ended') {
            alert('Host has ended this guest room.');
            location.reload();
          }
        } catch (_) {}
      };

      ws.onclose = () => {
        document.getElementById('headerOnlineCount').textContent = '⚪ Disconnected';
      };
    }

    function toggleWhisperMode() {
      isWhisperActive = !isWhisperActive;
      document.getElementById('btnWhisperToggle').classList.toggle('active', isWhisperActive);
    }

    function sendMessage() {
      const input = document.getElementById('messageInput');
      const text = input.value.trim();
      if (!text || !ws || ws.readyState !== WebSocket.OPEN) return;

      const payload = {
        type: 'message',
        channel_id: 'room-' + roomCode,
        content: text,
        text: text,
        isWhisper: isWhisperActive,
        is_whisper: isWhisperActive
      };

      ws.send(JSON.stringify(payload));
      input.value = '';
    }

    function appendMessage(msg) {
      const stream = document.getElementById('messageStream');
      const isMe = msg.authorId === guestUsername || msg.author_id === guestUsername || msg.sender === guestUsername;
      const isWhisper = Boolean(msg.isWhisper || msg.is_whisper);

      const row = document.createElement('div');
      row.className = 'msg-row ' + (isMe ? 'me' : 'other');

      const meta = document.createElement('div');
      meta.className = 'msg-meta';
      const nameSpan = document.createElement('span');
      nameSpan.textContent = msg.authorName || msg.sender_display_name || msg.author_name || 'Guest';
      const badge = document.createElement('span');
      badge.className = msg.is_guest ? 'guest-pill' : 'host-pill';
      badge.textContent = msg.is_guest ? 'GUEST' : 'HOST';
      meta.appendChild(nameSpan);
      meta.appendChild(badge);

      const bubble = document.createElement('div');
      bubble.className = 'bubble' + (isWhisper ? ' whisper' : '');

      if (isWhisper) {
        const contentSpan = document.createElement('span');
        contentSpan.className = 'whisper-content';
        contentSpan.textContent = msg.content || msg.text || '';
        const veil = document.createElement('div');
        veil.className = 'whisper-veil';
        veil.textContent = '🫧 Hold to reveal';

        bubble.appendChild(contentSpan);
        bubble.appendChild(veil);

        const reveal = () => {
          bubble.classList.add('revealed');
          if (navigator.vibrate) navigator.vibrate(15);
        };
        const hide = () => {
          bubble.classList.remove('revealed');
        };

        bubble.addEventListener('mousedown', reveal);
        bubble.addEventListener('mouseup', hide);
        bubble.addEventListener('mouseleave', hide);
        bubble.addEventListener('touchstart', (e) => { e.preventDefault(); reveal(); });
        bubble.addEventListener('touchend', hide);
        bubble.addEventListener('touchcancel', hide);
      } else {
        bubble.textContent = msg.content || msg.text || '';
      }

      row.appendChild(meta);
      row.appendChild(bubble);
      stream.appendChild(row);
      stream.scrollTop = stream.scrollHeight;
    }

    async function toggleVoice() {
      const btn = document.getElementById('btnVoiceToggle');
      if (isVoiceActive) {
        if (localStream) {
          localStream.getTracks().forEach(t => t.stop());
          localStream = null;
        }
        btn.classList.remove('active');
        btn.textContent = '🎙️ Join Voice';
        isVoiceActive = false;
      } else {
        try {
          localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
          btn.classList.add('active');
          btn.textContent = '🟢 In Voice';
          isVoiceActive = true;
        } catch (e) {
          alert('Microphone access is required for room voice chat.');
        }
      }
    }
  </script>
</body>
</html>`;
}
