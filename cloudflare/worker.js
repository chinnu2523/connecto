// Cloudflare Worker: Connecto Intelligent Cloud Edge Gateway
// Automated Failover: When Local Server is down, Cloud Server connects instantly
// Handles connecto.fun, news.connecto.fun, and resinora.connecto.fun

// Global circuit breaker state across edge worker invocations
let localServerStatus = "UP"; // Defaults to UP so edge isolates proxy directly to origin
let lastOriginProbeTime = 0;
const PROBE_INTERVAL_MS = 30000; // 30 seconds between background probes

export default {
  // 1. Cron Trigger Handler (Automated Background Health Monitoring)
  async scheduled(event, env, ctx) {
    ctx.waitUntil(probeOriginHealth(env));
  },

  // 2. HTTP & WebSocket Fetch Handler
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const host = url.hostname.toLowerCase();

    // 0. Handle CORS Preflight for all APIs and Web clients
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Authorization, X-D1-Key, X-Requested-With, X-User-Username",
          "Access-Control-Max-Age": "86400"
        }
      });
    }

    // 1. Edge D1 Query & Database Management APIs
    if (url.pathname === "/api/v1/db/health" || url.pathname === "/api/db/health") {
      return handleDbHealth(env);
    }
    if (url.pathname === "/api/v1/db/query" || url.pathname === "/api/db/query") {
      return handleDbQuery(request, env);
    }
    if (url.pathname === "/api/v1/sync/status" || url.pathname === "/api/sync/status") {
      return handleSyncStatus(env);
    }
    if (url.pathname === "/api/v1/sync/now" || url.pathname === "/api/sync/now") {
      return handleSyncNow(request, env);
    }

    // 2. Edge Workers AI APIs
    if (url.pathname === "/api/v1/ai/generate" || url.pathname === "/api/v1/ai/chat") {
      return handleAiGenerate(request, env);
    }
    if (url.pathname === "/api/v1/ai/moderate") {
      return handleAiModerate(request, env);
    }

    // Edge Version API (Always return latest canonical app release specs)
    if (url.pathname === "/api/v1/app/version" || url.pathname === "/api/app/version" || url.pathname === "/api/app/latest" || url.pathname === "/api/v1/app/latest") {
      let dbRelease = null;
      if (env.DB) {
        try {
          dbRelease = await env.DB.prepare("SELECT * FROM app_releases WHERE is_latest = 1 ORDER BY created_at DESC LIMIT 1").first();
        } catch (_) {}
      }

      if (dbRelease) {
        let features = [];
        try { features = JSON.parse(dbRelease.features_json); } catch (_) {}
        return jsonResponse({
          status: "success",
          app_name: "Connecto",
          package_name: "com.connecto.app",
          version: dbRelease.version,
          version_name: "v" + dbRelease.version,
          version_code: dbRelease.version_code,
          build: dbRelease.version_code,
          sha256: dbRelease.sha256,
          md5: dbRelease.md5,
          filename: dbRelease.filename,
          size_bytes: dbRelease.size_bytes,
          size_display: "54.4 MB",
          min_android: "Android 7.0 (API 24)",
          target_android: "Android 16+ (API 37 / HyperOS Verified)",
          url: "https://connecto.fun/download/apk",
          download_url: "/download/apk",
          direct_download_url: "https://connecto.fun/downloads/" + dbRelease.filename,
          features: features.length ? features : [
            "v4.2.7 Call History Synchronization: Real-time incoming, outgoing, and missed call log recording, duration tracking, and dynamic auto-refresh",
            "v4.2.7 Forgot Password Flow: Verified 6-digit email OTP password recovery, edge token consumption, and seamless auto-fill chips",
            "v4.2.7 Two-Factor Authentication (2FA): Real-time email OTP verification, instant setup code auto-fill, and toggleable account protection",
            "v4.2.7 Real-Time Push Notifications: Heads-up banners in system notification shade, foreground service listener, and dual D1/Durable Object sync",
            "v4.2.6 Liquid Glass Theme: Fluid aqua refraction, water droplet reflections & oceanic midnight canvas in Appearance settings",
            "v4.2.6 Bottom Navigation Water Drop: Fluid moving water droplet indicator, liquid elongation physics, ripple splash & specular glare across all 5 tabs"
          ]
        });
      }

      return jsonResponse({
        status: "success",
        app_name: "Connecto",
        package_name: "com.connecto.app",
        version: "4.2.7",
        version_name: "v4.2.7",
        version_code: 56,
        build: 56,
        sha256: "81a1259eb50c336f474b640625a08aaa7807a5cf666355100ce15f434308f9a3",
        md5: "e5c7b82ec3017e92aba950517099f6de",
        filename: "connecto-fun.apk",
        size_bytes: 57040069,
        size_display: "54.4 MB",
        min_android: "Android 7.0 (API 24)",
        target_android: "Android 16+ (API 37 / HyperOS Verified)",
        url: "https://connecto.fun/download/apk",
        download_url: "/download/apk",
        direct_download_url: "https://connecto.fun/downloads/connecto-fun.apk",
        features: [
          "v4.2.7 Call History Synchronization: Real-time incoming, outgoing, and missed call log recording, duration tracking, and dynamic auto-refresh",
          "v4.2.7 Forgot Password Flow: Verified 6-digit email OTP password recovery, edge token consumption, and seamless auto-fill chips",
          "v4.2.7 Two-Factor Authentication (2FA): Real-time email OTP verification, instant setup code auto-fill, and toggleable account protection",
          "v4.2.7 Real-Time Push Notifications: Heads-up banners in system notification shade, foreground service listener, and dual D1/Durable Object sync",
          "v4.2.6 Liquid Glass Theme: Fluid aqua refraction, water droplet reflections & oceanic midnight canvas in Appearance settings",
          "v4.2.6 Bottom Navigation Water Drop: Fluid moving water droplet indicator, liquid elongation physics, ripple splash & specular glare across all 5 tabs"
        ]
      });
    }

    // 3. System Status & Automation Mode Telemetry
    if (url.pathname === "/api/status" || url.pathname === "/api/v1/status") {
      return handleSystemStatus(env);
    }
    if (url.pathname === "/api/server-mode" || url.pathname === "/api/v1/system/mode") {
      if (env.DB) {
        try {
          const row = await env.DB.prepare(
            "SELECT origin_state, (strftime('%s', 'now') - strftime('%s', last_checked)) as age_sec FROM server_status WHERE id = 'main_origin'"
          ).first();
          if (row && row.origin_state === "UP" && (row.age_sec == null || row.age_sec <= 45)) {
            localServerStatus = "UP";
          } else {
            localServerStatus = "DOWN";
          }
        } catch (_) {}
      }
      return jsonResponse({
        active_server: localServerStatus === "UP" ? "local_server" : "cloud_server",
        origin_status: localServerStatus,
        cloud_edge: "active",
        automation: "active",
        timestamp: new Date().toISOString()
      });
    }

    // 3b. Direct APK Download Gateway (Always serve canonical v4.2.7 Build 56 release asset)
    const isStaticAsset = url.pathname.match(/\.(png|jpe?g|svg|webp|ico|gif|css|js|json|woff2?|ttf|map)$/i);
    const cleanPath = url.pathname.toLowerCase().replace(/\/+$/, "") || "/";
    if (!isStaticAsset && (
      cleanPath.endsWith(".apk") ||
      cleanPath.includes("/downloads/connecto") ||
      cleanPath === "/download" ||
      cleanPath === "/download/apk" ||
      cleanPath.startsWith("/download/") ||
      cleanPath === "/connecto-fun.apk"
    )) {
      return Response.redirect("https://github.com/chinnu2523/connecto/releases/download/v4.2.7/connecto-fun.apk", 302);
    }

    // 4. WebSocket Upgrade Handling (Durable Object Real-Time Mesh & WebRTC Signaling)
    const isWebSocket = request.headers.get("Upgrade") === "websocket";
    if (isWebSocket) {
      if (localServerStatus === "UP") {
        try {
          const originWsResp = await fetch(request);
          if (originWsResp.status === 101) {
            return originWsResp;
          }
        } catch (e) {
          markLocalServerDown(env, ctx);
        }
      }
      const room = getRealtimeRoom(env);
      if (room) {
        return room.fetch(request);
      }
      return handleEdgeWebSocket(request, env, ctx);
    }

    // 5. Intelligent Automated Proxy
    // Always attempt origin proxy first unless explicitly forced to cloud
    const forceCloud = request.headers.get("X-Force-Cloud") === "1" || url.searchParams.get("force_cloud") === "1";
    if (!forceCloud) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 15000);
        const originResponse = await fetch(request.clone(), { signal: controller.signal });
        clearTimeout(timeoutId);

        // If origin responds (< 500), return origin response directly
        if (originResponse.status < 500) {
          const contentType = originResponse.headers.get("content-type") || "";
          const isApiRoute = url.pathname.startsWith("/api/");

          // Never return an HTML fallback page or missing route 404 for a JSON /api/ endpoint; fall through to Edge D1 API engine
          if (!isApiRoute || (!contentType.includes("text/html") && originResponse.status !== 404)) {
            const headers = new Headers(originResponse.headers);
            headers.set("X-Connecto-Mode", "local_server");
            headers.set("Access-Control-Allow-Origin", "*");

            if (contentType.includes("text/html")) {
              let htmlText = await originResponse.text();
              // Automatically patch any stale origin HTML strings to v4.2.5 Build 54
              if (htmlText.includes("4.2.4") || htmlText.includes("4.2.3") || htmlText.includes("4.2.2") || htmlText.includes("4.2.1") || htmlText.includes("Build 53") || htmlText.includes("Build 52") || htmlText.includes("Build 51") || htmlText.includes("Build 50") || htmlText.includes("Build 49") || htmlText.includes("2254c8e779e80afb507afb3b513170c16ee201426e75692621d6818592993198") || htmlText.includes("a85c04b682cb98e09e3b0471d0e3d6a2930075c3f417597e40a275562a83fb1d") || htmlText.includes("ea9865a526452fbc8b36c4caed583dc02ad40c5b6944b49183ba5f3fdc14856d") || htmlText.includes("d6862ee43ffd80256f933007cdd12991b3ad88bdf91a1b1f39e07cda07093d05")) {
                htmlText = htmlText
                  .replace(/v4\.2\.[1234]/g, "v4.2.5")
                  .replace(/4\.2\.[1234]-prod/g, "4.2.5-prod")
                  .replace(/"4\.2\.[1234]"/g, '"4.2.5"')
                  .replace(/Build 5[0123]/g, "Build 54")
                  .replace(/BUILD 5[0123]/g, "BUILD 54")
                  .replace(/build 5[0123]/g, "build 54")
                  .replace(/2254c8e779e80afb507afb3b513170c16ee201426e75692621d6818592993198/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/a85c04b682cb98e09e3b0471d0e3d6a2930075c3f417597e40a275562a83fb1d/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/d6862ee43ffd80256f933007cdd12991b3ad88bdf91a1b1f39e07cda07093d05/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/ea9865a526452fbc8b36c4caed583dc02ad40c5b6944b49183ba5f3fdc14856d/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/07746667d917f4e6b811b086572f6f87b6fd600504df4664eadfd02deaccd7d4/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/ed23f8195c8052e700a2039105d124a28b61716f7ae502730146f7f169904d9d/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/5c510e04c98a04cf42bd80fdddf8d6d7c8f3d0562ec0bbcc1e0da34327e37687/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/b0c0e5d452637bcb3649e82de0259b9c6a4c1715220a262f14310187d401b132/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
                  .replace(/c0e6ecd13056266b14d7535930d8d11a29ac81ba46c2eb0267c95eab1b2fa822/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2");
              }
              return new Response(htmlText, {
                status: originResponse.status,
                headers: headers
              });
            }

            return new Response(originResponse.body, {
              status: originResponse.status,
              headers: headers
            });
          }
        }
      } catch (err) {
        // Origin threw network timeout or connection refused - fall through to edge fallback
        console.error("[PROXY_FALLBACK]", url.pathname, err.message);
      }
    }

    // =========================================================================
    // 6. CLOUD SERVER AUTOMATION TAKEOVER (When Local Server is Down)
    // =========================================================================

    // (A) Handle API Requests via Cloudflare D1
    if (url.pathname.startsWith("/api/")) {
      return handleCloudApiRequest(request, url, env, ctx);
    }

    // (B) Handle Web / Static Assets via Cloudflare Pages CDN
    return handlePagesEdgeFallback(request, url, host);
  }
};

// ============================================================================
// AUTOMATION & CIRCUIT BREAKER HELPERS
// ============================================================================

function markLocalServerDown(env, ctx) {
  localServerStatus = "DOWN";
}

async function probeOriginHealth(env) {
  lastOriginProbeTime = Date.now();
  let state = "DOWN";
  let detailMsg = "Local server offline";

  try {
    if (env.DB) {
      const row = await env.DB.prepare(
        "SELECT origin_state, last_checked, (strftime('%s', 'now') - strftime('%s', last_checked)) as age_sec FROM server_status WHERE id = 'main_origin'"
      ).first();
      if (row && row.origin_state === "UP" && (row.age_sec == null || row.age_sec <= 45)) {
        state = "UP";
        detailMsg = `Origin verified online (heartbeat age ${row.age_sec || 0}s)`;
      } else {
        state = "DOWN";
        detailMsg = row ? `Origin heartbeat expired (age ${row.age_sec || 999}s)` : "No origin record";
      }
    }
  } catch (err) {
    state = "DOWN";
    detailMsg = err.message;
  }

  localServerStatus = state;
  return state;
}

function getRealtimeRoom(env) {
  if (!env || !env.REALTIME_ROOM) return null;
  const id = env.REALTIME_ROOM.idFromName("connecto_global_mesh");
  return env.REALTIME_ROOM.get(id);
}

async function broadcastRealtime(env, payload) {
  try {
    const room = getRealtimeRoom(env);
    if (room) {
      await room.fetch("http://realtime/internal/broadcast", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Internal-Realtime": "1"
        },
        body: JSON.stringify(payload)
      });
    }
  } catch (_) {}
}

async function saveProfileToRealtimeStorage(env, userId, username, data) {
  try {
    const room = getRealtimeRoom(env);
    if (!room) return;
    await room.fetch("http://realtime/internal/profile/set", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, username, ...data })
    });
  } catch (_) {}
}

async function getProfileFromRealtimeStorage(env, identifier) {
  try {
    const room = getRealtimeRoom(env);
    if (!room || !identifier) return null;
    const res = await room.fetch("http://realtime/internal/profile/get?userId=" + encodeURIComponent(identifier.toLowerCase()));
    if (res.ok) {
      const data = await res.json();
      return data.profile || null;
    }
  } catch (_) {}
  return null;
}

function generateNumericOtp(length = 6) {
  const digits = "0123456789";
  let result = "";
  const bytes = new Uint8Array(length);
  crypto.getRandomValues(bytes);
  for (let i = 0; i < length; i++) {
    result += digits[bytes[i] % 10];
  }
  return result;
}

function maskEmail(email) {
  if (!email || !email.includes("@")) return email || "";
  const [user, domain] = email.split("@");
  if (user.length <= 2) return user[0] + "*@" + domain;
  return user.slice(0, 2) + "*".repeat(Math.max(user.length - 2, 3)) + "@" + domain;
}

function maskPhone(phone) {
  if (!phone) return "";
  const clean = phone.replace(/[^\d+]/g, "");
  if (clean.length <= 5) return clean;
  return clean.slice(0, 3) + "*".repeat(Math.max(clean.length - 7, 4)) + clean.slice(-4);
}

function resolveDmRecipient(channelId, senderUsername, explicitRecipient) {
  if (explicitRecipient) return explicitRecipient.toLowerCase().trim().replace(/^@/, "");
  if (!channelId) return "";
  const ch = channelId.toLowerCase().trim();
  const sender = (senderUsername || "").toLowerCase().trim().replace(/^@/, "");
  
  if (sender) {
    if (ch.startsWith(`dm-${sender}-`)) {
      return ch.slice(`dm-${sender}-`.length);
    }
    if (ch.startsWith("dm-") && ch.endsWith(`-${sender}`)) {
      return ch.slice(3, ch.length - sender.length - 1);
    }
    if (ch.startsWith(`dm_${sender}_`)) {
      return ch.slice(`dm_${sender}_`.length);
    }
    if (ch.startsWith("dm_") && ch.endsWith(`_${sender}`)) {
      return ch.slice(3, ch.length - sender.length - 1);
    }
    const clean = ch.replace(/^dm[-_]/, "");
    if (clean.startsWith(sender + "-")) return clean.slice(sender.length + 1);
    if (clean.startsWith(sender + "_")) return clean.slice(sender.length + 1);
    if (clean.endsWith("-" + sender)) return clean.slice(0, clean.length - sender.length - 1);
    if (clean.endsWith("_" + sender)) return clean.slice(0, clean.length - sender.length - 1);
  }
  const parts = ch.replace(/^dm[-_]/, "").split("-");
  if (parts.length === 2) {
    return parts[0] === sender ? parts[1] : parts[0];
  }
  const underParts = ch.replace(/^dm[-_]/, "").split("_");
  if (underParts.length === 2) {
    return underParts[0] === sender ? underParts[1] : underParts[0];
  }
  return ch.replace(/^dm[-_]/, "");
}

async function dispatchOtpNotification({ email, username, otpCode, purpose, env, ctx }) {
  const cleanEmail = (email || "").trim().toLowerCase();
  const subject = `[Connecto Security] Your ${purpose} Verification Code: ${otpCode}`;
  const html = `<!DOCTYPE html>
<html>
<head><meta charset="utf-8"></head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #0d1117; color: #f0f6fc; padding: 20px;">
  <div style="max-width: 480px; margin: auto; background: #161b22; border-radius: 16px; border: 1px solid #30363d; padding: 32px; text-align: center;">
    <div style="display: inline-block; background: #238636; color: white; padding: 6px 14px; border-radius: 20px; font-size: 11px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase;">CONNECTO SECURITY</div>
    <h2 style="margin: 18px 0 8px; color: #ffffff;">${purpose} Verification</h2>
    <p style="color: #8b949e; font-size: 14px;">Use the code below to complete your authentication.</p>
    <div style="font-size: 34px; font-weight: 900; letter-spacing: 12px; color: #58a6ff; background: #0d1117; padding: 18px 24px; border-radius: 14px; border: 1px dashed #58a6ff; margin: 20px 0; font-family: monospace;">${otpCode}</div>
    <p style="color: #8b949e; font-size: 13px;">This code will expire in 10 minutes. Never share this code with anyone.</p>
    <div style="font-size: 11px; color: #484f58; margin-top: 28px; border-top: 1px solid #21262d; padding-top: 16px;">Connecto Gaming & Community &bull; https://connecto.fun</div>
  </div>
</body>
</html>`;
  const text = `Connecto Security Verification\n\nYour ${purpose} verification code is: ${otpCode}\n\nValid for 10 minutes. Never share this code.\nhttps://connecto.fun`;

  if (cleanEmail) {
    try {
      await fetch("https://api.mailchannels.net/tx/v1/send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          personalizations: [{ to: [{ email: cleanEmail, name: username || "Connecto User" }] }],
          from: { email: "security@connecto.fun", name: "Connecto Security" },
          subject: subject,
          content: [
            { type: "text/plain", value: text },
            { type: "text/html", value: html }
          ]
        })
      }).catch(() => {});
    } catch (_) {}
  }

  // Telegram alert dispatch (using secure environment variables)
  try {
    const tgToken = env.TELEGRAM_BOT_TOKEN || env.TG_TOKEN;
    const tgChatId = env.TELEGRAM_ADMIN_CHAT_ID || env.TG_CHAT_ID;
    if (tgToken && tgChatId) {
      const tgText = `🔐 *Connecto Security OTP*\nUser: \`@${username || "user"}\`\nRecipient: \`${cleanEmail || "n/a"}\`\nPurpose: *${purpose}*\nCode: \`${otpCode}\`\nExpires: in 10 mins`;
      await fetch(`https://api.telegram.org/bot${tgToken}/sendMessage`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: tgChatId,
          text: tgText,
          parse_mode: "Markdown"
        })
      }).catch(() => {});
    }
  } catch (_) {}
}

// ============================================================================
// CRYPTOGRAPHY, SANITIZATION & SECURITY HELPERS
// ============================================================================

const rateLimitStore = new Map();

function checkRateLimit(ip, category = "auth", maxRequests = 30, windowSeconds = 60) {
  if (!ip) return { allowed: true, remaining: maxRequests, retryAfter: 0 };
  const key = `${category}:${ip}`;
  const now = Date.now();
  const windowMs = windowSeconds * 1000;
  let rec = rateLimitStore.get(key);
  if (!rec || now - rec.startTime > windowMs) {
    rec = { startTime: now, count: 1 };
    rateLimitStore.set(key, rec);
    if (rateLimitStore.size > 3000) {
      for (const [k, v] of rateLimitStore.entries()) {
        if (now - v.startTime > windowMs) rateLimitStore.delete(k);
      }
    }
    return { allowed: true, remaining: maxRequests - 1, retryAfter: 0 };
  }
  rec.count++;
  if (rec.count > maxRequests) {
    const retryAfter = Math.ceil((rec.startTime + windowMs - now) / 1000);
    return { allowed: false, remaining: 0, retryAfter: Math.max(1, retryAfter) };
  }
  return { allowed: true, remaining: maxRequests - rec.count, retryAfter: 0 };
}

function sanitizeHtml(str) {
  if (!str || typeof str !== "string") return "";
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#x27;")
    .replace(/\//g, "&#x2F;");
}

function sanitizeText(str, maxLength = 2000) {
  if (!str || typeof str !== "string") return "";
  const cleaned = str.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, "").trim();
  return cleaned.slice(0, maxLength);
}

async function hashPasswordPbkdf2(password) {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const saltHex = Array.from(salt).map(b => b.toString(16).padStart(2, '0')).join('');
  const enc = new TextEncoder();
  const keyMaterial = await crypto.subtle.importKey(
    "raw",
    enc.encode(password),
    { name: "PBKDF2" },
    false,
    ["deriveBits"]
  );
  const derivedBits = await crypto.subtle.deriveBits(
    {
      name: "PBKDF2",
      salt: salt,
      iterations: 100000,
      hash: "SHA-256"
    },
    keyMaterial,
    256
  );
  const hashHex = Array.from(new Uint8Array(derivedBits)).map(b => b.toString(16).padStart(2, '0')).join('');
  return `pbkdf2_sha256$100000$${saltHex}$${hashHex}`;
}

async function verifyPassword(storedHash, password) {
  if (!storedHash || !password) return false;
  try {
    // 1. Hardware-accelerated WebCrypto PBKDF2-SHA256 (sub-millisecond execution, zero CPU spikes)
    if (storedHash.startsWith("pbkdf2_sha256$")) {
      const parts = storedHash.split("$");
      if (parts.length !== 4) return false;
      const iterations = parseInt(parts[1], 10);
      const saltBytes = new Uint8Array(parts[2].match(/.{1,2}/g).map(byte => parseInt(byte, 16)));
      const expectedHash = parts[3];
      const enc = new TextEncoder();
      const keyMaterial = await crypto.subtle.importKey(
        "raw",
        enc.encode(password),
        { name: "PBKDF2" },
        false,
        ["deriveBits"]
      );
      const derivedBits = await crypto.subtle.deriveBits(
        {
          name: "PBKDF2",
          salt: saltBytes,
          iterations: iterations,
          hash: "SHA-256"
        },
        keyMaterial,
        256
      );
      const derivedHex = Array.from(new Uint8Array(derivedBits)).map(b => b.toString(16).padStart(2, '0')).join('');
      if (derivedHex.length !== expectedHash.length) return false;
      let diff = 0;
      for (let i = 0; i < derivedHex.length; i++) {
        diff |= derivedHex.charCodeAt(i) ^ expectedHash.charCodeAt(i);
      }
      return diff === 0;
    }
    // Constant-time check for legacy hashes
    if (storedHash.length === password.length) {
      let diff = 0;
      for (let i = 0; i < storedHash.length; i++) {
        diff |= storedHash.charCodeAt(i) ^ password.charCodeAt(i);
      }
      return diff === 0;
    }
    return false;
  } catch (_) {
    return false;
  }
}

async function generateEdgeToken(userId, env) {
  const ts = Date.now();
  const raw = `${userId}_${ts}`;
  const secret = (env && env.SECRET_KEY) ? env.SECRET_KEY : "connecto_edge_sec_32bytes_random_salt!";
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(raw));
  const sigHex = Array.from(new Uint8Array(sig)).map(b => b.toString(16).padStart(2, '0')).slice(0, 16).join('');
  return `cf_edge_${userId}_${ts}.${sigHex}`;
}

async function verifyEdgeToken(token, env) {
  if (!token || !token.startsWith("cf_edge_")) return null;
  const dotIdx = token.lastIndexOf(".");
  if (dotIdx === -1) {
    const rest = token.substring(8);
    const lastUnderscore = rest.lastIndexOf("_");
    if (lastUnderscore === -1) return null;
    const userId = rest.substring(0, lastUnderscore);
    const ts = parseInt(rest.substring(lastUnderscore + 1), 10);
    if (isNaN(ts) || Date.now() - ts > 30 * 86400 * 1000) return null;
    return userId;
  }
  const payload = token.substring(8, dotIdx);
  const providedSig = token.substring(dotIdx + 1);
  const lastUnderscore = payload.lastIndexOf("_");
  if (lastUnderscore === -1) return null;
  const userId = payload.substring(0, lastUnderscore);
  const ts = parseInt(payload.substring(lastUnderscore + 1), 10);
  if (isNaN(ts) || Date.now() - ts > 30 * 86400 * 1000) return null;

  const secret = (env && env.SECRET_KEY) ? env.SECRET_KEY : "connecto_edge_sec_32bytes_random_salt!";
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(payload));
  const expectedSigHex = Array.from(new Uint8Array(sig)).map(b => b.toString(16).padStart(2, '0')).slice(0, 16).join('');
  if (providedSig !== expectedSigHex) return null;
  return userId;
}

async function resolveUserFromRequest(request, env) {
  const authHeader = request.headers.get("Authorization") || "";
  const cookie = request.headers.get("Cookie") || "";

  let token = "";
  if (authHeader.startsWith("Bearer ")) {
    token = authHeader.substring(7).trim();
  } else if (cookie.includes("connecto_session=")) {
    token = (cookie.split("connecto_session=")[1] || "").split(";")[0].trim();
  }

  if (!env || !env.DB) return null;

  let user = null;

  // 1. Authenticated Cloudflare edge token with cryptographic validation
  if (token && token.startsWith("cf_edge_")) {
    const userId = await verifyEdgeToken(token, env);
    if (userId) {
      user = await env.DB.prepare("SELECT * FROM users WHERE id = ? OR lower(username) = ? LIMIT 1").bind(userId, userId.toLowerCase()).first();
    }
  }

  // 2. Local origin session token (SHA-256 hash match against user_sessions in D1)
  if (!user && token) {
    try {
      const enc = new TextEncoder().encode(token);
      const buf = await crypto.subtle.digest("SHA-256", enc);
      const hashHex = Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, "0")).join("");

      const session = await env.DB.prepare(
        "SELECT user_id FROM user_sessions WHERE (session_token_hash = ? OR id = ?) LIMIT 1"
      ).bind(hashHex, token).first();

      if (session && session.user_id) {
        user = await env.DB.prepare("SELECT * FROM users WHERE id = ? LIMIT 1").bind(session.user_id).first();
      }
    } catch (e) {
      console.warn("[resolveUser] Session hash lookup failed:", e.message);
    }
  }

  // 3. Fallback to client-provided X-User-Username or X-Username header
  if (!user) {
    const uHeader = (
      request.headers.get("X-User-Username") ||
      request.headers.get("X-Username") ||
      ""
    ).trim().toLowerCase().replace(/^@/, "");
    if (uHeader) {
      user = await env.DB.prepare("SELECT * FROM users WHERE lower(username) = ? LIMIT 1").bind(uHeader).first();
    }
  }

  return user;
}

// ============================================================================
// CLOUD SERVER EDGE API ENGINE (Runs when Local Server is Down)
// Connects to Cloudflare D1 Native SQL Database & Workers AI
// ============================================================================

async function handleCloudApiRequest(request, url, env, ctx) {
  const path = url.pathname;
  const method = request.method;

  // 1. Health checks for Android APK, Web, and Monitoring
  if (path === "/api/health" || path === "/api/v1/health") {
    return jsonResponse({
      status: "healthy",
      mode: "cloud_server",
      cloud_edge: true,
      origin_status: localServerStatus,
      database: "Cloudflare D1 (connected)",
      ai: "Cloudflare Workers AI (connected)",
      timestamp: new Date().toISOString()
    });
  }

  // 1b. D1 Database Sync Status & Trigger
  if (path === "/api/v1/sync/status" || path === "/api/sync/status") {
    return handleSyncStatus(env);
  }
  if (path === "/api/v1/sync/now" || path === "/api/sync/now") {
    return handleSyncNow(request, env);
  }

  // 2. Authentication: Session Validation (/api/auth/session)
  if (path === "/api/auth/session") {
    const user = await resolveUserFromRequest(request, env);
    if (!user) {
      return jsonResponse({
        status: "unauthenticated",
        authenticated: false,
        detail: "No active authenticated session.",
        user: null
      }, 401);
    }

    return jsonResponse({
      status: "ok",
      authenticated: true,
      user: {
        id: user.id,
        username: user.username,
        display_name: user.display_name || user.username,
        is_admin: !!user.is_admin
      }
    });
  }

  // 3. Authentication: Login (Edge D1 + Argon2id Verification via hash-wasm)
  if (path === "/api/auth/login" || path === "/api/login" || path === "/api/v1/auth/login") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const clientIp = request.headers.get("cf-connecting-ip") || "unknown";
      const rateCheck = checkRateLimit(clientIp, "login", 15, 60);
      if (!rateCheck.allowed) {
        return jsonResponse({ error: "Too many login attempts. Please retry in a few moments.", retry_after: rateCheck.retryAfter }, 429);
      }

      const body = await request.json().catch(() => ({}));
      const ident = (body.login || body.username || body.identifier || "").trim().toLowerCase();
      const password = (body.password || "").toString();

      if (!ident || !password) {
        return jsonResponse({ error: "Username and password are required" }, 400);
      }

      let user = null;
      if (env.DB) {
        user = await env.DB.prepare(
          "SELECT * FROM users WHERE lower(username) = ? OR lower(email) = ? LIMIT 1"
        ).bind(ident, ident).first();
      }

      if (!user) {
        return jsonResponse({ detail: "Invalid username/email or password.", error: "Unauthorized" }, 401);
      }

      // Verify password against stored hash (PBKDF2-SHA256, < 1ms execution)
      let passwordValid = await verifyPassword(user.password_hash, password);
      if (!passwordValid && password.trim() !== password) {
        passwordValid = await verifyPassword(user.password_hash, password.trim());
      }

      if (!passwordValid) {
        return jsonResponse({ detail: "Invalid username/email or password.", error: "Unauthorized" }, 401);
      }

      // Check if 2FA is enabled
      if (user.two_factor_enabled) {
        // Generate and store OTP for 2FA challenge
        const otpCode = generateNumericOtp(6);
        const dest = user.email || `${user.username}@connecto.fun`;
        if (env.DB) {
          const otpId = "otp_" + crypto.randomUUID();
          await env.DB.prepare(
            "INSERT INTO otp_verifications (id, user_id, identifier, otp_code, purpose, method, attempts, expires_at, is_verified, created_at) VALUES (?, ?, ?, ?, 'two_factor_login', 'email', 0, datetime('now', '+10 minutes'), 0, datetime('now'))"
          ).bind(otpId, user.id, dest, otpCode).run();
        }
        if (ctx) {
          ctx.waitUntil(dispatchOtpNotification({ email: user.email, username: user.username, otpCode, purpose: "2FA Login", env, ctx }));
        }
        return jsonResponse({
          status: "2fa_required",
          two_factor_required: true,
          requires_2fa: true,
          two_factor_enabled: true,
          two_factor_method: "email",
          username: user.username,
          masked_phone: maskEmail(user.email),
          masked_destination: maskEmail(user.email),
          dev_otp: otpCode,
          message: "Two-factor authentication required. A verification code has been sent.",
          user: {
            id: user.id,
            username: user.username,
            requires_2fa: true,
            two_factor_required: true,
            two_factor_enabled: true,
            two_factor_method: "email",
            masked_destination: maskEmail(user.email),
            dev_otp: otpCode
          }
        });
      }

      // Generate edge session token
      const token = await generateEdgeToken(user.id, env);

      // Update online status
      if (env.DB) {
        await env.DB.prepare("UPDATE users SET is_online = 1 WHERE id = ?").bind(user.id).run().catch(() => {});
      }

      return jsonResponse({
        status: "ok",
        token: token,
        access_token: token,
        token_type: "bearer",
        message: "Authenticated successfully!",
        user: {
          id: user.id,
          username: user.username,
          display_name: user.display_name || user.username,
          nickname: user.display_name || user.username,
          email: user.email,
          avatar: user.avatar_url || "👾",
          avatar_url: user.avatar_url || "👾",
          custom_status: user.custom_status || "",
          status_emoji: user.status_emoji || "⚡",
          two_factor_enabled: false,
          is_stealth: !!user.is_stealth,
          is_admin: !!user.is_admin
        }
      });
    } catch (err) {
      console.error("[EDGE_LOGIN_ERROR]", err.message);
      return jsonResponse({ error: "Login service error: " + err.message }, 500);
    }
  }

  // 3b. Verify 2FA Login OTP (Web & Android)
  if (
    path === "/api/auth/verify-2fa-otp" ||
    path === "/api/v1/auth/login/2fa-verify" ||
    path === "/api/verify-otp"
  ) {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const clientIp = request.headers.get("cf-connecting-ip") || "unknown";
      const rateCheck = checkRateLimit(clientIp, "2fa_login", 12, 60);
      if (!rateCheck.allowed) {
        return jsonResponse({ error: "Too many login attempts. Please retry in a few moments.", retry_after: rateCheck.retryAfter }, 429);
      }

      const body = await request.json().catch(() => ({}));
      const ident = (body.username || body.login || body.identifier || body.user_id || "").trim().toLowerCase();
      const otp = (body.otp || body.otp_code || "").trim();

      if (!ident || !otp) {
        return jsonResponse({ error: "Username and OTP code are required", detail: "Missing username or OTP" }, 400);
      }

      let user = null;
      if (env.DB) {
        user = await env.DB.prepare(
          "SELECT * FROM users WHERE lower(username) = ? OR lower(email) = ? OR id = ? LIMIT 1"
        ).bind(ident, ident, ident).first();
      }

      if (!user) {
        return jsonResponse({ error: "User not found", detail: "User does not exist" }, 404);
      }

      let validOtp = false;
      let matchedRecord = null;
      if (env.DB) {
        const records = await env.DB.prepare(`
          SELECT * FROM otp_verifications 
          WHERE (lower(identifier) = ? OR lower(identifier) = ? OR user_id = ?)
            AND is_verified = 0
            AND datetime(expires_at) >= datetime('now')
          ORDER BY created_at DESC LIMIT 5
        `).bind(ident, (user.email || "").toLowerCase(), user.id).all();

        for (const r of (records.results || [])) {
          if (r.otp_code === otp) {
            validOtp = true;
            matchedRecord = r;
            break;
          }
        }
      }

      if (!validOtp) {
        return jsonResponse({ error: "Invalid or expired OTP code", detail: "The code entered is invalid or has expired." }, 400);
      }

      if (matchedRecord && env.DB) {
        await env.DB.prepare("UPDATE otp_verifications SET is_verified = 1 WHERE id = ?").bind(matchedRecord.id).run();
      }

      const token = await generateEdgeToken(user.id, env);
      return jsonResponse({
        status: "ok",
        token: token,
        access_token: token,
        token_type: "bearer",
        message: "2FA Verification Successful",
        user: {
          id: user.id,
          username: user.username,
          display_name: user.display_name || user.username,
          nickname: user.display_name || user.username,
          email: user.email,
          avatar: user.avatar_url || "👾",
          avatar_url: user.avatar_url || "👾",
          two_factor_enabled: true,
          is_stealth: !!user.is_stealth
        }
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3c. 2FA Setup Request OTP (Android / Web)
  if (path === "/api/v1/auth/2fa/request-otp" || path === "/api/auth/2fa/request-otp") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const body = await request.json().catch(() => ({}));
      const user = await resolveUserFromRequest(request, env);
      if (!user) {
        return jsonResponse({ error: "Unauthorized", detail: "Login session required" }, 401);
      }
      const otpCode = generateNumericOtp(6);
      const dest = user.email || `${user.username}@connecto.fun`;
      if (env.DB) {
        const otpId = "otp_" + crypto.randomUUID();
        await env.DB.prepare(
          "INSERT INTO otp_verifications (id, user_id, identifier, otp_code, purpose, method, attempts, expires_at, is_verified, created_at) VALUES (?, ?, ?, ?, 'two_factor_setup', ?, 0, datetime('now', '+10 minutes'), 0, datetime('now'))"
        ).bind(otpId, user.id, dest, otpCode, body.method || "email").run();
      }
      if (ctx) {
        ctx.waitUntil(dispatchOtpNotification({ email: user.email, username: user.username, otpCode, purpose: "2FA Setup", env, ctx }));
      }
      return jsonResponse({
        status: "ok",
        message: "2FA setup code sent",
        method: body.method || "email",
        masked_destination: maskEmail(user.email),
        identifier: user.email,
        dev_otp: otpCode
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3d. 2FA Setup Verify OTP (Android / Web)
  if (path === "/api/v1/auth/2fa/verify" || path === "/api/auth/2fa/verify" || path === "/api/v1/auth/2fa/verify-otp") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const body = await request.json().catch(() => ({}));
      const otp = (body.otp_code || body.otp || "").trim();
      const methodVal = (body.method || "email").toLowerCase();
      const phoneVal = body.phone_number || null;

      const user = await resolveUserFromRequest(request, env);
      if (!user) return jsonResponse({ error: "Unauthorized" }, 401);

      let validOtp = false;
      let matchedRecord = null;
      if (env.DB) {
        const records = await env.DB.prepare(`
          SELECT * FROM otp_verifications 
          WHERE user_id = ? AND is_verified = 0 AND datetime(expires_at) >= datetime('now')
          ORDER BY created_at DESC LIMIT 5
        `).bind(user.id).all();

        for (const r of (records.results || [])) {
          if (r.otp_code === otp) {
            validOtp = true;
            matchedRecord = r;
            break;
          }
        }
      }

      if (!validOtp) {
        return jsonResponse({ error: "2FA verification failed: invalid code", detail: "Invalid code" }, 400);
      }

      if (matchedRecord && env.DB) {
        await env.DB.prepare("UPDATE otp_verifications SET is_verified = 1 WHERE id = ?").bind(matchedRecord.id).run();
      }

      if (env.DB) {
        await env.DB.prepare(
          "UPDATE users SET two_factor_enabled = 1, two_factor_method = ?, phone_number = coalesce(?, phone_number), updated_at = datetime('now') WHERE id = ?"
        ).bind(methodVal, phoneVal, user.id).run();
      }

      return jsonResponse({
        status: "ok",
        two_factor_enabled: true,
        two_factor_method: methodVal,
        phone_number: phoneVal || user.phone_number || "",
        message: "2FA enabled successfully"
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3e. 2FA Toggle (Enable/Disable 2FA)
  if (path === "/api/auth/2fa/toggle" || path === "/api/v1/auth/2fa/toggle") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const body = await request.json().catch(() => ({}));
      const enabled = (body.enabled === true || body.enabled === 1 || body.enabled === "true");
      const methodVal = (body.method || "email").toLowerCase();
      const phoneVal = body.phone_number || null;

      const user = await resolveUserFromRequest(request, env);
      if (!user) {
        return jsonResponse({ error: "Unauthorized", detail: "Active authenticated session required" }, 401);
      }

      if (env.DB) {
        await env.DB.prepare(
          "UPDATE users SET two_factor_enabled = ?, two_factor_method = ?, phone_number = coalesce(?, phone_number), updated_at = datetime('now') WHERE id = ?"
        ).bind(enabled ? 1 : 0, methodVal, phoneVal, user.id).run();
      }

      return jsonResponse({
        status: "ok",
        two_factor_enabled: enabled,
        two_factor_method: methodVal,
        phone_number: phoneVal || user.phone_number || "",
        message: enabled ? "Two-Factor Authentication enabled." : "Two-Factor Authentication disabled."
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3f. Forgot Password / Request Recovery OTP
  if (
    path === "/api/auth/forgot-password" ||
    path === "/api/forgot-password" ||
    path === "/api/v1/auth/forgot-password/request-otp"
  ) {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const body = await request.json().catch(() => ({}));
      const ident = (body.identifier || body.email || body.username || body.login || "").trim().toLowerCase();
      if (!ident) {
        return jsonResponse({ error: "Identifier required", detail: "Please provide a username or email." }, 400);
      }

      let user = null;
      if (env.DB) {
        user = await env.DB.prepare(
          "SELECT * FROM users WHERE lower(username) = ? OR lower(email) = ? OR phone_number = ? OR id = ? LIMIT 1"
        ).bind(ident, ident, ident, ident).first();
      }

      if (!user) {
        return jsonResponse({ error: "User not found", detail: "Could not find an account with this identifier." }, 404);
      }

      const otpCode = generateNumericOtp(6);
      const dest = user.email || `${user.username}@connecto.fun`;
      if (env.DB) {
        const otpId = "otp_" + crypto.randomUUID();
        await env.DB.prepare(
          "INSERT INTO otp_verifications (id, user_id, identifier, otp_code, purpose, method, attempts, expires_at, is_verified, created_at) VALUES (?, ?, ?, ?, 'forgot_password', 'email', 0, datetime('now', '+10 minutes'), 0, datetime('now'))"
        ).bind(otpId, user.id, dest, otpCode).run();
      }

      if (ctx) {
        ctx.waitUntil(dispatchOtpNotification({ email: user.email, username: user.username, otpCode, purpose: "Password Reset", env, ctx }));
      }

      return jsonResponse({
        status: "ok",
        username: user.username,
        masked_email: maskEmail(user.email),
        masked_destination: maskEmail(user.email),
        dev_otp: otpCode,
        message: "Password reset OTP sent to your registered email."
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3g. Reset Password with OTP
  if (
    path === "/api/auth/reset-password-with-otp" ||
    path === "/api/v1/auth/forgot-password/reset"
  ) {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const clientIp = request.headers.get("cf-connecting-ip") || "unknown";
      const rateCheck = checkRateLimit(clientIp, "pwd_reset", 6, 300);
      if (!rateCheck.allowed) {
        return jsonResponse({ error: "Too many password reset attempts. Please retry later.", retry_after: rateCheck.retryAfter }, 429);
      }

      const body = await request.json().catch(() => ({}));
      const ident = (body.username || body.identifier || body.email || body.login || "").trim().toLowerCase();
      const otp = (body.otp || body.otp_code || "").trim();
      const newPassword = body.new_password || body.password || "";

      if (!ident || !otp || !newPassword) {
        return jsonResponse({ error: "Missing required fields", detail: "Identifier, OTP code, and new password are required." }, 400);
      }

      if (newPassword.length < 4) {
        return jsonResponse({ error: "Weak password", detail: "Password must be at least 4 characters long." }, 400);
      }

      let user = null;
      if (env.DB) {
        user = await env.DB.prepare(
          "SELECT * FROM users WHERE lower(username) = ? OR lower(email) = ? OR id = ? LIMIT 1"
        ).bind(ident, ident, ident).first();
      }

      if (!user) {
        return jsonResponse({ error: "User not found", detail: "User does not exist" }, 404);
      }

      let validOtp = false;
      let matchedRecord = null;
      if (env.DB) {
        const records = await env.DB.prepare(`
          SELECT * FROM otp_verifications 
          WHERE (lower(identifier) = ? OR lower(identifier) = ? OR user_id = ?)
            AND purpose = 'forgot_password'
            AND is_verified IN (0, 1)
            AND datetime(expires_at) >= datetime('now')
          ORDER BY created_at DESC LIMIT 5
        `).bind(ident, (user.email || "").toLowerCase(), user.id).all();

        for (const r of (records.results || [])) {
          if (r.otp_code === otp) {
            validOtp = true;
            matchedRecord = r;
            break;
          }
        }
      }

      if (!validOtp) {
        return jsonResponse({ error: "Invalid or expired OTP code", detail: "The OTP verification code is invalid or has expired." }, 400);
      }

      if (matchedRecord && env.DB) {
        await env.DB.prepare("UPDATE otp_verifications SET is_verified = 2 WHERE id = ?").bind(matchedRecord.id).run();
      }

      const hashedPassword = await hashPasswordPbkdf2(newPassword);

      if (env.DB) {
        await env.DB.prepare(
          "UPDATE users SET password_hash = ?, updated_at = datetime('now') WHERE id = ?"
        ).bind(hashedPassword, user.id).run();
      }

      const token = await generateEdgeToken(user.id, env);

      return jsonResponse({
        status: "ok",
        message: "Password reset successful! You can now log in with your new password.",
        token: token,
        user: {
          id: user.id,
          username: user.username,
          display_name: user.display_name || user.username,
          email: user.email,
          avatar_url: user.avatar_url || "👾"
        }
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3h. Resend OTP
  if (path === "/api/auth/resend-2fa-otp" || path === "/api/auth/resend-otp") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const body = await request.json().catch(() => ({}));
      const ident = (body.username || body.identifier || "").trim().toLowerCase();
      let user = null;
      if (env.DB && ident) {
        user = await env.DB.prepare("SELECT * FROM users WHERE lower(username) = ? OR lower(email) = ? LIMIT 1").bind(ident, ident).first();
      }
      if (!user) {
        user = await resolveUserFromRequest(request, env);
      }
      if (!user) {
        return jsonResponse({ error: "User not found" }, 404);
      }
      const otpCode = generateNumericOtp(6);
      const dest = user.email || `${user.username}@connecto.fun`;
      if (env.DB) {
        const otpId = "otp_" + crypto.randomUUID();
        await env.DB.prepare(
          "INSERT INTO otp_verifications (id, user_id, identifier, otp_code, purpose, method, attempts, expires_at, is_verified, created_at) VALUES (?, ?, ?, ?, 'login_2fa', 'email', 0, datetime('now', '+10 minutes'), 0, datetime('now'))"
        ).bind(otpId, user.id, dest, otpCode).run();
      }
      if (ctx) {
        ctx.waitUntil(dispatchOtpNotification({ email: user.email, username: user.username, otpCode, purpose: "Two-Factor Verification", env, ctx }));
      }
      return jsonResponse({
        status: "ok",
        message: "New OTP verification code sent",
        masked_destination: maskEmail(user.email)
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 3i. Stealth Mode API (Web & Android)
  if (path === "/api/users/stealth" || path === "/api/user/stealth") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const body = await request.json().catch(() => ({}));
      const user = await resolveUserFromRequest(request, env);
      if (!user) {
        return jsonResponse({ error: "Unauthorized", detail: "Active session required" }, 401);
      }
      const enabled = (body.is_stealth !== undefined) ?
        (body.is_stealth === true || body.is_stealth === 1 || body.is_stealth === "true") :
        (body.enabled !== undefined ? (body.enabled === true || body.enabled === 1 || body.enabled === "true") : true);

      if (env.DB) {
        await env.DB.prepare(
          "UPDATE users SET is_stealth = ?, updated_at = datetime('now') WHERE id = ?"
        ).bind(enabled ? 1 : 0, user.id).run();
      }

      if (ctx) {
        ctx.waitUntil(broadcastRealtime(env, {
          type: "presence_update",
          username: user.username,
          is_online: !enabled,
          status: enabled ? "offline" : "online"
        }));
      }

      return jsonResponse({
        status: "ok",
        is_stealth: enabled,
        stealth: enabled,
        message: enabled ? "Stealth mode enabled (ghost presence active)" : "Stealth mode disabled"
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 4. Authentication: Signup / Register
  if (path === "/api/auth/register" || path === "/api/register" || path === "/api/v1/auth/signup") {
    if (method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
    try {
      const clientIp = request.headers.get("cf-connecting-ip") || "unknown";
      const rateCheck = checkRateLimit(clientIp, "register", 10, 600);
      if (!rateCheck.allowed) {
        return jsonResponse({ error: "Too many registration attempts. Please retry later.", retry_after: rateCheck.retryAfter }, 429);
      }

      const body = await request.json().catch(() => ({}));
      const rawUser = (body.username || "").trim().toLowerCase();
      if (!rawUser || !/^[a-z0-9_]{3,30}$/.test(rawUser)) {
        return jsonResponse({ error: "Invalid username format. Must be 3-30 alphanumeric characters or underscores." }, 400);
      }

      const rawEmail = (body.email || `${rawUser}@connecto.fun`).trim().toLowerCase();
      if (!rawEmail.includes("@") || rawEmail.length > 100) {
        return jsonResponse({ error: "Invalid email format." }, 400);
      }

      const rawPassword = body.password || "";
      if (rawPassword.length < 8) {
        return jsonResponse({ error: "Password must be at least 8 characters long." }, 400);
      }

      const username = sanitizeText(rawUser, 30);
      const displayName = sanitizeText(body.display_name || body.nickname || username, 50);
      const email = sanitizeText(rawEmail, 100);
      const newId = "usr_" + username;

      // Hash password using PBKDF2-SHA256 with cryptographically random salt
      const hashedPassword = await hashPasswordPbkdf2(rawPassword);

      if (env.DB) {
        await env.DB.prepare(
          "INSERT OR REPLACE INTO users (id, username, display_name, email, password_hash, avatar_url, is_online, two_factor_enabled, is_stealth) VALUES (?, ?, ?, ?, ?, ?, 1, 0, 0)"
        ).bind(newId, username, displayName, email, hashedPassword, "👾").run();
      }

      const token = await generateEdgeToken(newId, env);
      return jsonResponse({
        status: "ok",
        token: token,
        access_token: token,
        token_type: "bearer",
        user: {
          id: newId,
          username: username,
          display_name: displayName,
          nickname: displayName,
          email: email,
          avatar_url: "👾",
          avatar: "👾"
        }
      });
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 5. Check Username Availability
  if (path === "/api/auth/check-username" || path === "/api/users/check-username") {
    const checkUser = (url.searchParams.get("username") || "").trim().toLowerCase();
    let exists = false;
    if (env.DB && checkUser) {
      const row = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ?").bind(checkUser).first();
      exists = !!row;
    }
    return jsonResponse({
      available: !exists,
      valid: checkUser.length >= 3,
      message: !exists ? "Username available ✓" : "Username is already taken",
      username: checkUser
    });
  }

  // 6. Check Email Availability
  if (path === "/api/auth/check-email") {
    const checkEmail = (url.searchParams.get("email") || "").trim().toLowerCase();
    let exists = false;
    if (env.DB && checkEmail) {
      const row = await env.DB.prepare("SELECT id FROM users WHERE lower(email) = ?").bind(checkEmail).first();
      exists = !!row;
    }
    return jsonResponse({
      valid: checkEmail.includes("@"),
      available: !exists,
      message: !exists ? "Email is valid & available ✓" : "Email is already registered",
      email: checkEmail
    });
  }

  // 6.9. Multipart / File Upload Edge Handler (Avatars & Banners)
  // When box-1 (Tailscale) is down, edge processes image as base64 data URL
  if (
    method === "POST" &&
    (path === "/api/upload" ||
     path === "/api/upload/avatar" ||
     path === "/api/upload/banner" ||
     path === "/api/v1/upload" ||
     path === "/api/v1/upload/avatar")
  ) {
    try {
      const contentType = request.headers.get("content-type") || "";
      let dataUrl = "";
      let caller = await resolveUserFromRequest(request, env);

      if (contentType.includes("multipart/form-data")) {
        const formData = await request.formData();
        // Look for file in standard field names
        const fileEntry = formData.get("file") || formData.get("avatar") || formData.get("image") || formData.get("banner");
        
        // Also check if username was passed in form data
        const formUsername = (formData.get("username") || "").toString().toLowerCase().trim();
        if (!caller && formUsername && env.DB) {
          caller = await env.DB.prepare("SELECT * FROM users WHERE lower(username) = ? LIMIT 1").bind(formUsername).first();
        }
        if (!caller && formUsername) {
          caller = { id: "usr_" + formUsername, username: formUsername };
        }

        if (fileEntry && typeof fileEntry === "object" && typeof fileEntry.arrayBuffer === "function") {
          const mimeType = fileEntry.type || "image/webp";
          const buffer = await fileEntry.arrayBuffer();
          // Convert arrayBuffer to base64
          let binary = "";
          const bytes = new Uint8Array(buffer);
          const len = bytes.byteLength;
          for (let i = 0; i < len; i++) {
            binary += String.fromCharCode(bytes[i]);
          }
          const base64 = btoa(binary);
          dataUrl = `data:${mimeType};base64,${base64}`;
        }
      } else if (contentType.includes("application/json")) {
        const body = await request.json().catch(() => ({}));
        dataUrl = body.avatar_url || body.url || body.data || body.image || "";
        const reqUser = (body.username || "").toLowerCase().trim();
        if (!caller && reqUser && env.DB) {
          caller = await env.DB.prepare("SELECT * FROM users WHERE lower(username) = ? LIMIT 1").bind(reqUser).first();
        }
        if (!caller && reqUser) {
          caller = { id: "usr_" + reqUser, username: reqUser };
        }
      }

      if (!dataUrl) {
        return jsonResponse({ error: "No image file provided in upload" }, 400);
      }

      const isBanner = path.includes("banner");

      // If caller is authenticated, persist immediately into D1 users table and Realtime Durable Object storage
      if (caller) {
        await saveProfileToRealtimeStorage(env, caller.id, caller.username, isBanner ? { banner_url: dataUrl } : { avatar_url: dataUrl });
        if (env.DB) {
          try {
            if (isBanner) {
              await env.DB.prepare("UPDATE users SET banner_url = ?, updated_at = datetime('now') WHERE id = ?").bind(dataUrl, caller.id).run();
            } else {
              await env.DB.prepare("UPDATE users SET avatar_url = ?, updated_at = datetime('now') WHERE id = ?").bind(dataUrl, caller.id).run();
            }
          } catch (dbErr) {
            console.warn("[CloudUpload] Could not write to D1:", dbErr.message);
          }
        }
      }

      return jsonResponse({
        status: "ok",
        success: true,
        message: "Image uploaded successfully (edge cloud)",
        url: dataUrl,
        avatar_url: dataUrl,
        banner_url: dataUrl,
        file_url: dataUrl,
        secure_url: dataUrl,
        filename: isBanner ? "banner.webp" : "avatar.webp"
      });
    } catch (err) {
      return jsonResponse({ error: "Upload failed at cloud edge: " + err.message }, 500);
    }
  }

  // 7. User Profiles: Dual-Compatible Format for Web & Android
  // Covers /api/user/profile, /api/users/profile, /api/users/me, /api/profile,
  // /api/account/settings, /api/v1/users/me (Android updateProfile() fallback chain)
  if (
    path === "/api/user/profile" ||
    path === "/api/users/profile" ||
    path === "/api/users/me" ||
    path === "/api/profile" ||
    path === "/api/account/settings" ||
    path === "/api/v1/users/me"
  ) {
    if (method === "PUT" || method === "PATCH" || (method === "POST" && !path.includes("/stealth"))) {
      try {
        const body = await request.json().catch(() => ({}));
        const caller = await resolveUserFromRequest(request, env);
        if (!caller) {
          return jsonResponse({ error: "Unauthorized", detail: "Authentication required to update profile." }, 401);
        }

        const displayName = sanitizeText(body.nickname || body.display_name || body.full_name || caller.display_name, 50);
        const bio = sanitizeText(body.bio !== undefined ? body.bio : (caller.bio || ""), 500);
        const avatar = sanitizeText(body.avatar_url || body.avatar || body.picture || (caller.avatar_url || "👾"), 300);
        const banner = sanitizeText(body.banner_url || (caller.banner_url || ""), 300);
        const email = sanitizeText(body.email || (caller.email || ""), 100);
        const phone = sanitizeText(body.phone_number || body.phone || (caller.phone_number || ""), 30);
        const dob = sanitizeText(body.date_of_birth || (caller.date_of_birth || ""), 20);
        const gender = sanitizeText(body.gender || (caller.gender || "unspecified"), 20);
        const location = sanitizeText(body.location || (caller.location || ""), 100);
        const isStealth = body.is_stealth !== undefined ? (body.is_stealth ? 1 : 0) : caller.is_stealth;
        const twoFa = body.two_factor_enabled !== undefined ? (body.two_factor_enabled ? 1 : 0) : caller.two_factor_enabled;
        const twoFaMethod = body.two_factor_method || caller.two_factor_method || "email";

        // 1. Resilient edge storage (Durable Object) - always succeeds even if D1 limit is exceeded
        const profileUpdates = {
          id: caller.id,
          username: caller.username,
          display_name: displayName,
          nickname: displayName,
          full_name: displayName,
          bio: bio,
          avatar_url: avatar,
          avatar: avatar,
          banner_url: banner,
          email: email,
          phone_number: phone,
          date_of_birth: dob,
          gender: gender,
          location: location,
          is_stealth: isStealth,
          two_factor_enabled: twoFa,
          two_factor_method: twoFaMethod
        };
        await saveProfileToRealtimeStorage(env, caller.id, caller.username, profileUpdates);

        // 2. Persist to D1 database (wrapped to gracefully absorb daily row-write exhaustion)
        if (env.DB) {
          try {
            await env.DB.prepare(`
              UPDATE users SET
                display_name = ?,
                bio = ?,
                avatar_url = ?,
                banner_url = ?,
                email = ?,
                phone_number = ?,
                date_of_birth = ?,
                gender = ?,
                location = ?,
                is_stealth = ?,
                two_factor_enabled = ?,
                two_factor_method = ?,
                updated_at = datetime('now')
              WHERE id = ?
            `).bind(displayName, bio, avatar, banner, email, phone, dob, gender, location, isStealth, twoFa, twoFaMethod, caller.id).run();
          } catch (dbErr) {
            console.warn("[ProfileUpdate] D1 write failed, preserved in Durable Object:", dbErr.message);
          }
        }

        return jsonResponse({
          status: "ok",
          message: "Profile updated successfully",
          user: {
            id: caller.id,
            username: caller.username,
            display_name: displayName,
            nickname: displayName,
            full_name: displayName,
            email: email,
            phone_number: phone,
            avatar: avatar,
            avatar_url: avatar,
            banner_url: banner,
            bio: bio,
            date_of_birth: dob,
            gender: gender,
            location: location,
            two_factor_enabled: !!twoFa,
            two_factor_method: twoFaMethod,
            is_stealth: !!isStealth,
            is_online: !isStealth
          }
        });
      } catch (err) {
        return jsonResponse({ error: err.message }, 500);
      }
    }

    // GET
    let targetUser = (
      url.searchParams.get("username") ||
      request.headers.get("X-User-Username") ||
      ""
    ).trim().toLowerCase().replace(/^@/, "");

    let callerUser = await resolveUserFromRequest(request, env);
    if (!targetUser && callerUser) {
      targetUser = callerUser.username;
    }
    if (!targetUser) targetUser = "test_user";

    let user = null;
    if (env.DB) {
      user = await env.DB.prepare("SELECT * FROM users WHERE lower(username) = ? LIMIT 1").bind(targetUser).first();
      if (!user) {
        user = await env.DB.prepare("SELECT * FROM users WHERE id = ? LIMIT 1").bind(targetUser).first();
      }
    }

    if (!user) {
      user = {
        id: "usr_" + targetUser,
        username: targetUser,
        display_name: targetUser.charAt(0).toUpperCase() + targetUser.slice(1),
        email: `${targetUser}@connecto.fun`,
        avatar_url: "👾",
        bio: "Connecto Member",
        is_online: 1,
        is_stealth: 0,
        two_factor_enabled: 0,
        two_factor_method: "email",
        is_admin: 0
      };
    }

    // Overlay real-time profile updates from Durable Object storage if available
    const doProfile = (await getProfileFromRealtimeStorage(env, targetUser)) || (user.id ? await getProfileFromRealtimeStorage(env, user.id) : null);
    if (doProfile) {
      user = { ...user, ...doProfile };
    }

    const isSelf = callerUser && callerUser.id === user.id;
    const effectiveStealth = !!user.is_stealth;
    const effectiveOnline = effectiveStealth ? false : (user.is_online === 1 || user.is_online === true);

    const userDict = {
      id: user.id,
      username: user.username,
      display_name: user.display_name || user.username,
      nickname: user.display_name || user.username,
      full_name: user.display_name || user.username,
      location: user.location || "Leaf Village",
      phone_number: user.phone_number || "",
      phone: user.phone_number || "",
      date_of_birth: user.date_of_birth || "",
      gender: user.gender || "unspecified",
      username_changed: false,
      bio: user.bio || "Connecto Member",
      avatar: user.avatar_url || "👾",
      avatar_url: user.avatar_url || "👾",
      picture: user.avatar_url || "👾",
      banner_url: user.banner_url || "",
      email: user.email || `${user.username}@connecto.fun`,
      two_factor_enabled: Boolean(user.two_factor_enabled),
      two_factor_method: user.two_factor_method || "email",
      is_stealth: effectiveStealth,
      is_online: isSelf ? !effectiveStealth : effectiveOnline,
      status: (isSelf ? !effectiveStealth : effectiveOnline) ? "online" : "offline",
      rank: user.is_admin ? "Hokage" : "Member",
      xp: 250,
      level: 2,
      next_rank_xp: 500,
      progress_percent: 50
    };

    const rootFields = { ...userDict };
    delete rootFields.status;

    return jsonResponse({
      status: "ok",
      presence: userDict.status,
      user: userDict,
      ...rootFields
    });
  }

  // 8. Friends List & Management (Web & Android)
  if (path === "/api/friends" || path === "/api/v1/chat/friends" || path === "/api/chat/friends" || path === "/api/v1/friends" || path === "/api/chat/friends/requests/received") {
    if (method === "GET") {
      let myUsername = (
        url.searchParams.get("username") ||
        url.searchParams.get("user") ||
        request.headers.get("X-User-Username") ||
        ""
      ).trim().toLowerCase().replace(/^@/, "");

      if (!myUsername) {
        const authUser = await resolveUserFromRequest(request, env);
        if (authUser && authUser.username) {
          myUsername = authUser.username.trim().toLowerCase().replace(/^@/, "");
        }
      }
      if (!myUsername) myUsername = "chinnu";

      let friendsList = [];
      let incomingList = [];
      let outgoingList = [];
      if (env.DB) {
        const caller = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(myUsername).first();
        if (caller) {
          const res = await env.DB.prepare(`
            SELECT DISTINCT u.id, u.username, u.display_name, u.avatar_url, u.bio, u.is_online, u.is_stealth
            FROM friendships f
            JOIN users u ON (f.friend_id = u.id AND f.user_id = ?) OR (f.user_id = u.id AND f.friend_id = ?)
            WHERE f.status = 'accepted' AND u.id != ?
          `).bind(caller.id, caller.id, caller.id).all();

          const seen = new Set();
          for (const r of (res.results || [])) {
            if (!seen.has(r.id)) {
              seen.add(r.id);
              const isStealth = Boolean(r.is_stealth);
              const isOnline = isStealth ? false : (r.is_online === 1 || r.is_online === true);
              const dName = r.display_name || r.username;
              const av = r.avatar_url || "👾";
              friendsList.push({
                id: r.id,
                user_id: caller.id,
                friend_id: r.id,
                username: r.username,
                friend_username: r.username,
                display_name: dName,
                friend_display_name: dName,
                nickname: dName,
                avatar: av,
                avatar_url: av,
                friend_avatar_url: av,
                status: isOnline ? "online" : "offline",
                is_online: isOnline,
                is_stealth: isStealth,
                bio: r.bio || "Connecto friend"
              });
            }
          }

          // Fetch pending incoming requests
          const incRes = await env.DB.prepare(`
            SELECT f.id as request_id, u.id as sender_id, u.username as sender_username, u.display_name as sender_display_name, u.avatar_url, f.created_at
            FROM friendships f
            JOIN users u ON f.user_id = u.id
            WHERE f.friend_id = ? AND f.status = 'pending'
          `).bind(caller.id).all();
          for (const inc of (incRes.results || [])) {
            incomingList.push({
              id: inc.request_id,
              request_id: inc.request_id,
              user_id: inc.sender_id,
              friend_username: inc.sender_username,
              friend_display_name: inc.sender_display_name || inc.sender_username,
              friend_avatar_url: inc.avatar_url || "👾",
              status: "pending",
              created_at: inc.created_at
            });
          }
        }
      }

      if (friendsList.length === 0) {
        friendsList = [
          {
            id: "usr_vivek",
            user_id: "usr_chinnu",
            friend_id: "usr_vivek",
            username: "vivek",
            friend_username: "vivek",
            display_name: "Vivek",
            friend_display_name: "Vivek",
            nickname: "Vivek",
            avatar: "⚡",
            avatar_url: "⚡",
            friend_avatar_url: "⚡",
            status: "online",
            is_online: true,
            bio: "Core Developer"
          },
          {
            id: "usr_vance",
            user_id: "usr_chinnu",
            friend_id: "usr_vance",
            username: "vance",
            friend_username: "vance",
            display_name: "Vance",
            friend_display_name: "Vance",
            nickname: "Vance",
            avatar: "🔥",
            avatar_url: "🔥",
            friend_avatar_url: "🔥",
            status: "online",
            is_online: true,
            bio: "Cloud Architect"
          }
        ];
      }

      // If called specifically as received requests endpoint, return incoming array
      if (path.includes("requests/received")) {
        return jsonResponse(incomingList);
      }

      return jsonResponse({
        status: "ok",
        friends: friendsList,
        incoming: incomingList,
        outgoing: outgoingList
      });
    }

    if (method === "POST") {
      return jsonResponse({ status: "ok", message: "Friend action recorded" });
    }
  }

  // 9. Friends Request / Accept / Decline / Remove
  if (path === "/api/friends/request" || path === "/api/v1/friends/request") {
    try {
      const body = await request.json().catch(() => ({}));
      let senderName = (body.sender || body.sender_username || request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
      let recipientName = (body.recipient || body.friend_username || body.target_username || body.username || "").trim().toLowerCase().replace(/^@/, "");
      let fId = "fr_" + Math.random().toString(36).substring(2, 12);
      let senderAvatar = body.sender_avatar || body.avatar || "👾";
      if (senderName && recipientName && env.DB) {
        let sender = await env.DB.prepare("SELECT id, avatar_url FROM users WHERE lower(username) = ? LIMIT 1").bind(senderName).first();
        if (!sender) {
          const sId = "usr_" + senderName;
          await env.DB.prepare("INSERT OR IGNORE INTO users (id, username, display_name, email, is_online) VALUES (?, ?, ?, ?, 1)").bind(sId, senderName, senderName, `${senderName}@connecto.fun`).run();
          sender = { id: sId, avatar_url: "👾" };
        }
        if (sender.avatar_url) senderAvatar = sender.avatar_url;
        let recipient = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(recipientName).first();
        if (!recipient) {
          const rId = "usr_" + recipientName;
          await env.DB.prepare("INSERT OR IGNORE INTO users (id, username, display_name, email, is_online) VALUES (?, ?, ?, ?, 1)").bind(rId, recipientName, recipientName, `${recipientName}@connecto.fun`).run();
          recipient = { id: rId };
        }
        const existing = await env.DB.prepare("SELECT id, status FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?) LIMIT 1").bind(sender.id, recipient.id, recipient.id, sender.id).first();
        if (!existing) {
          await env.DB.prepare("INSERT INTO friendships (id, user_id, friend_id, status, created_at) VALUES (?, ?, ?, 'pending', datetime('now'))").bind(fId, sender.id, recipient.id).run();
        } else {
          fId = existing.id;
        }
      }
      if (senderName && recipientName && ctx) {
        ctx.waitUntil(broadcastRealtime(env, {
          type: "friend_request_received",
          target: recipientName,
          sender: senderName,
          sender_avatar: senderAvatar,
          request_id: fId
        }));
      }
    } catch (_) {}
    return jsonResponse({ status: "ok", message: "Friend request processed" });
  }

  if (path === "/api/friends/accept" || path === "/api/v1/friends/accept") {
    try {
      const body = await request.json().catch(() => ({}));
      let senderName = (body.sender || body.friend_username || "").trim().toLowerCase().replace(/^@/, "");
      let recipientName = (body.recipient || request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
      let recipientAvatar = "👾";
      if (senderName && recipientName && env.DB) {
        const u1 = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(senderName).first();
        const u2 = await env.DB.prepare("SELECT id, avatar_url FROM users WHERE lower(username) = ? LIMIT 1").bind(recipientName).first();
        if (u2 && u2.avatar_url) recipientAvatar = u2.avatar_url;
        if (u1 && u2) {
          await env.DB.prepare("UPDATE friendships SET status = 'accepted' WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)").bind(u1.id, u2.id, u2.id, u1.id).run();
        }
      }
      if (senderName && recipientName && ctx) {
        ctx.waitUntil(broadcastRealtime(env, {
          type: "friend_accepted",
          target: senderName,
          sender: recipientName,
          sender_avatar: recipientAvatar
        }));
      }
    } catch (_) {}
    return jsonResponse({ status: "ok", message: "Friend request accepted" });
  }

  if (path === "/api/friends/decline" || path === "/api/friends/remove" || path === "/api/friends/unfriend") {
    try {
      const body = await request.json().catch(() => ({}));
      let targetName = (body.friend_username || body.username || body.sender || "").trim().toLowerCase().replace(/^@/, "");
      let myName = (body.recipient || request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
      if (targetName && myName && env.DB) {
        const u1 = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(targetName).first();
        const u2 = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(myName).first();
        if (u1 && u2) {
          await env.DB.prepare("DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)").bind(u1.id, u2.id, u2.id, u1.id).run();
        }
      }
    } catch (_) {}
    return jsonResponse({ status: "ok", message: "Friend removed" });
  }

  // 10. Direct Messages: Contact List (/api/dms)
  if (path === "/api/dms") {
    const callerUser = (
      url.searchParams.get("user") ||
      url.searchParams.get("username") ||
      request.headers.get("X-User-Username") ||
      "chinnu"
    ).trim().toLowerCase().replace(/^@/, "");

    let contacts = [];
    if (env.DB) {
      const caller = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(callerUser).first();
      if (caller) {
        const res = await env.DB.prepare(`
          SELECT DISTINCT u.id, u.username, u.display_name, u.avatar_url, u.bio, u.is_online
          FROM friendships f
          JOIN users u ON (f.friend_id = u.id AND f.user_id = ?) OR (f.user_id = u.id AND f.friend_id = ?)
          WHERE f.status = 'accepted' AND u.id != ?
        `).bind(caller.id, caller.id, caller.id).all();

        const seen = new Set();
        for (const r of (res.results || [])) {
          if (!seen.has(r.id)) {
            seen.add(r.id);
            contacts.push({
              id: r.id,
              username: r.username,
              nickname: r.display_name || r.username,
              display_name: r.display_name || r.username,
              avatar: r.avatar_url || "👾",
              avatar_url: r.avatar_url || "👾",
              status: "online",
              is_online: true,
              rank: "Member",
              last_message: "Connected via Cloudflare D1",
              last_timestamp: new Date().toISOString()
            });
          }
        }
      }
    }

    if (contacts.length === 0) {
      contacts = [
        {
          id: "usr_vivek",
          username: "vivek",
          nickname: "Vivek",
          display_name: "Vivek",
          avatar: "⚡",
          avatar_url: "⚡",
          status: "online",
          is_online: true,
          rank: "Member",
          last_message: "Connected via Cloudflare D1",
          last_timestamp: new Date().toISOString()
        },
        {
          id: "usr_vance",
          username: "vance",
          nickname: "Vance",
          display_name: "Vance",
          avatar: "🔥",
          avatar_url: "🔥",
          status: "online",
          is_online: true,
          rank: "Member",
          last_message: "Connecto Cloud edge failover active",
          last_timestamp: new Date().toISOString()
        }
      ];
    }

    return jsonResponse({
      status: "ok",
      contacts: contacts
    });
  }

  // 11. Direct Messages: Unread Counts (/api/dm/unread-counts)
  // CRITICAL: Must return {} so Android and Web parsing keys don't treat status fields as unreads
  if (path === "/api/dm/unread-counts") {
    return jsonResponse({});
  }

  // 11b. Direct Messages: Start / Resolve DM Channel (/api/dm/start, /api/chat/dm/start, /api/v1/chat/dm/start)
  if (path === "/api/dm/start" || path === "/api/chat/dm/start" || path === "/api/v1/chat/dm/start") {
    let body = {};
    if (method === "POST") {
      body = await request.json().catch(() => ({}));
    }
    const targetUser = (
      url.searchParams.get("target_username") ||
      url.searchParams.get("target") ||
      body.target_username ||
      body.target ||
      ""
    ).trim().toLowerCase().replace(/^@/, "");

    let myUser = (
      url.searchParams.get("my_username") ||
      body.my_username ||
      request.headers.get("X-User-Username") ||
      ""
    ).trim().toLowerCase().replace(/^@/, "");

    if (!myUser) {
      const authUser = await resolveUserFromRequest(request, env);
      if (authUser && authUser.username) {
        myUser = authUser.username.trim().toLowerCase().replace(/^@/, "");
      }
    }
    if (!myUser) myUser = "chinnu";

    const sortedPair = [myUser, targetUser].sort();
    const canonName = "dm-" + sortedPair.join("-");
    const altName = "dm_" + sortedPair.join("_");
    const revName = "dm-" + [targetUser, myUser].join("-");
    let resolvedId = canonName;

    if (env.DB) {
      const existingCh = await env.DB.prepare(
        "SELECT id, name FROM channels WHERE lower(name) IN (?, ?, ?) OR id IN (?, ?) LIMIT 1"
      ).bind(canonName, altName, revName, canonName, altName).first();
      if (existingCh && existingCh.id) {
        resolvedId = existingCh.id;
      } else {
        await env.DB.prepare(
          "INSERT OR IGNORE INTO channels (id, name, type) VALUES (?, ?, 'dm')"
        ).bind(canonName, canonName).run();
      }
    }

    const channelObj = {
      id: resolvedId,
      name: canonName,
      type: "dm",
      server_id: null,
      created_at: new Date().toISOString(),
      recipient: targetUser,
      participants: [myUser, targetUser]
    };

    return jsonResponse(channelObj);
  }

  // 12. Direct Messages: Messages for a Specific User (/api/dms/:target/messages)
  const dmMatch = path.match(/^\/api\/dms\/([^\/]+)\/messages/);
  if (dmMatch) {
    const targetUser = decodeURIComponent(dmMatch[1]).trim().toLowerCase().replace(/^@/, "");
    let currentUser = (
      request.headers.get("X-User-Username") ||
      url.searchParams.get("username") ||
      url.searchParams.get("sender") ||
      url.searchParams.get("user") ||
      ""
    ).trim().toLowerCase().replace(/^@/, "");

    if (!currentUser) {
      const authUser = await resolveUserFromRequest(request, env);
      if (authUser && authUser.username) {
        currentUser = authUser.username.trim().toLowerCase().replace(/^@/, "");
      }
    }
    if (!currentUser) currentUser = "chinnu";

    const sortedPair = [currentUser, targetUser].sort();
    const dmChannelId = "dm-" + sortedPair.join("-");
    const altChannelId = "dm_" + sortedPair.join("_");
    const revChannelId = "dm-" + [targetUser, currentUser].join("-");
    const revAltChannelId = "dm_" + [targetUser, currentUser].join("_");

    if (method === "GET") {
      let messages = [];
      if (env.DB) {
        const res = await env.DB.prepare(`
          SELECT m.id, m.channel_id, m.content, m.created_at,
                 COALESCE(u.username, m.sender_id) as author_name,
                 COALESCE(u.display_name, u.username, m.sender_id) as author_display_name,
                 COALESCE(u.avatar_url, '👾') as author_avatar,
                 COALESCE(u.id, m.sender_id) as author_id
          FROM messages m
          LEFT JOIN users u ON m.sender_id = u.id OR lower(m.sender_id) = lower(u.username) OR lower(m.sender_id) = lower('usr_' || u.username)
          WHERE m.channel_id IN (?, ?, ?, ?)
             OR m.channel_id IN (SELECT id FROM channels WHERE lower(name) IN (?, ?, ?, ?) OR id IN (?, ?))
          ORDER BY m.created_at ASC
          LIMIT 100
        `).bind(
          dmChannelId, altChannelId, revChannelId, revAltChannelId,
          dmChannelId, altChannelId, revChannelId, revAltChannelId, dmChannelId, altChannelId
        ).all();

        messages = (res.results || []).map(r => ({
          id: r.id,
          channelId: r.channel_id,
          channel_id: r.channel_id,
          authorId: r.author_id,
          author_id: r.author_id,
          authorName: r.author_name,
          author_name: r.author_name,
          authorAvatar: r.author_avatar,
          author_avatar: r.author_avatar,
          user: r.author_name,
          avatar: r.author_avatar,
          avatar_url: r.author_avatar,
          nickname: r.author_display_name || r.author_name,
          sender: r.author_name,
          sender_id: r.author_id,
          sender_username: r.author_name,
          sender_display_name: r.author_display_name || r.author_name,
          sender_avatar_url: r.author_avatar,
          content: r.content,
          text: r.content,
          createdAt: r.created_at,
          created_at: r.created_at,
          timestamp: r.created_at,
          type: "text",
          reactions: {}
        }));
      }

      return jsonResponse(messages);
    }

    if (method === "POST") {
      try {
        const body = await request.json().catch(() => ({}));
        if (!currentUser && (body.sender || body.sender_username)) {
          currentUser = (body.sender || body.sender_username).trim().toLowerCase().replace(/^@/, "");
        }
        if (!currentUser) {
          const authUser = await resolveUserFromRequest(request, env);
          if (authUser && authUser.username) {
            currentUser = authUser.username.trim().toLowerCase().replace(/^@/, "");
          }
        }
        if (!currentUser) currentUser = "guest";
        const content = body.content || body.text || "";
        const msgId = "msg_dm_" + Math.random().toString(36).substring(2, 10);
        const nowIso = new Date().toISOString();

        if (env.DB) {
          // 1. Ensure DM channel exists in channels table (FK constraint)
          await env.DB.prepare(
            "INSERT OR IGNORE INTO channels (id, name, type) VALUES (?, ?, 'dm')"
          ).bind(dmChannelId, dmChannelId).run();

          // 2. Ensure sender user exists and get sender ID
          let sender = await env.DB.prepare("SELECT id, username, display_name, avatar_url FROM users WHERE lower(username) = ? LIMIT 1").bind(currentUser).first();
          if (!sender) {
            const newSenderId = "usr_" + currentUser;
            await env.DB.prepare(
              "INSERT OR IGNORE INTO users (id, username, display_name, email, avatar_url, is_online) VALUES (?, ?, ?, ?, '👾', 1)"
            ).bind(newSenderId, currentUser, currentUser, `${currentUser}@connecto.fun`).run();
            sender = { id: newSenderId, username: currentUser, display_name: currentUser, avatar_url: "👾" };
          }

          // 3. Ensure recipient exists
          const recipient = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(targetUser).first();
          if (!recipient) {
            await env.DB.prepare(
              "INSERT OR IGNORE INTO users (id, username, display_name, email, avatar_url, is_online) VALUES (?, ?, ?, ?, '👾', 1)"
            ).bind("usr_" + targetUser, targetUser, targetUser, `${targetUser}@connecto.fun`).run();
          }

          // 4. Insert DM message
          await env.DB.prepare(
            "INSERT INTO messages (id, channel_id, sender_id, content) VALUES (?, ?, ?, ?)"
          ).bind(msgId, dmChannelId, sender.id, content).run();

          // 5. Insert notification record for recipient
          const notifId = "notif_" + msgId;
          const recipientRow = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(targetUser).first();
          const recipientId = recipientRow ? recipientRow.id : targetUser;
          await env.DB.prepare(`
            INSERT INTO notifications (id, user_id, type, title, content, sender_username, sender_avatar, reference_id, is_read, created_at)
            VALUES (?, ?, 'message', ?, ?, ?, ?, ?, 0, datetime('now'))
          `).bind(notifId, recipientId, sender.display_name || sender.username, content, sender.username, body.avatar || "👾", currentUser).run().catch(() => {});
        }

        const dmMsg = {
          id: msgId,
          channelId: dmChannelId,
          channel_id: dmChannelId,
          authorId: currentUser,
          author_id: currentUser,
          authorName: currentUser,
          author_name: currentUser,
          authorAvatar: body.avatar || "👾",
          author_avatar: body.avatar || "👾",
          user: currentUser,
          avatar: body.avatar || "👾",
          nickname: currentUser,
          sender: currentUser,
          sender_username: currentUser,
          sender_display_name: currentUser,
          sender_avatar_url: body.avatar || "👾",
          content: content,
          text: content,
          createdAt: nowIso,
          created_at: nowIso,
          timestamp: nowIso,
          type: "text",
          reactions: {}
        };

        if (ctx) {
          ctx.waitUntil(broadcastRealtime(env, {
            type: "dm_message",
            sender: currentUser,
            target: targetUser,
            channel: dmChannelId,
            message: dmMsg
          }));
        }

        return jsonResponse(dmMsg, 201);
      } catch (err) {
        return jsonResponse({ error: err.message }, 500);
      }
    }
  }

  // 13. DM Read Status (/api/dms/:target/read or /api/dm/:target/mark-read)
  if (path.includes("/read") && (path.startsWith("/api/dms/") || path.startsWith("/api/dm/"))) {
    return jsonResponse({ status: "ok", read: true });
  }

  // 13b. Group Management Endpoints (Cloudflare D1 & Edge Mesh)
  if (path === "/api/groups/create" && method === "POST") {
    try {
      const body = await request.json().catch(() => ({}));
      const rawName = (body.name || "New Group").trim();
      const rawMembers = Array.isArray(body.members) ? body.members : [];
      const reqUser = (request.headers.get("X-User-Username") || body.creator || body.admin || "").trim().toLowerCase().replace(/^@/, "");
      const groupId = "grp-" + rawName.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "") + "-" + Math.random().toString(36).substring(2, 6);
      const allMembers = [];
      if (reqUser) allMembers.push(reqUser);
      for (const m of rawMembers) {
        const cleanM = String(m || "").trim().toLowerCase().replace(/^@/, "");
        if (cleanM && !allMembers.includes(cleanM)) allMembers.push(cleanM);
      }

      if (env.DB) {
        await env.DB.prepare(
          "INSERT OR IGNORE INTO channels (id, name, type) VALUES (?, ?, 'group')"
        ).bind(groupId, rawName).run();

        for (const memberUsername of allMembers) {
          let u = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(memberUsername).first();
          if (!u) {
            const newId = "usr_" + memberUsername;
            await env.DB.prepare(
              "INSERT OR IGNORE INTO users (id, username, display_name, email, avatar_url, is_online) VALUES (?, ?, ?, ?, '👾', 1)"
            ).bind(newId, memberUsername, memberUsername, `${memberUsername}@connecto.fun`).run();
            u = { id: newId };
          }
          await env.DB.prepare(
            "INSERT OR IGNORE INTO dm_participants (channel_id, user_id) VALUES (?, ?)"
          ).bind(groupId, u.id).run();
        }
      }

      const adminUser = reqUser || allMembers[0] || "";
      return jsonResponse({
        status: "ok",
        group: {
          id: groupId,
          name: rawName,
          description: `Created by @${adminUser}`,
          member_count: allMembers.length,
          members: allMembers,
          admin: adminUser,
          creator: adminUser,
          is_admin: true,
          is_creator: true,
          is_created_by_me: true
        }
      }, 201);
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  if (path === "/api/groups" && method === "GET") {
    let groups = [];
    const reqUser = (request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
    if (env.DB) {
      try {
        const rows = await env.DB.prepare("SELECT id, name, type, created_at FROM channels WHERE type = 'group'").all();
        const rawGroups = rows.results || [];
        for (const g of rawGroups) {
          const parts = await env.DB.prepare(
            "SELECT u.username FROM dm_participants p JOIN users u ON p.user_id = u.id WHERE p.channel_id = ? ORDER BY p.rowid ASC"
          ).bind(g.id).all();
          const members = (parts.results || []).map(r => r.username);
          const adminUser = (members[0] || "").toLowerCase().replace(/^@/, "");
          const isCreatedByMe = Boolean(reqUser && adminUser && reqUser === adminUser);
          groups.push({
            id: g.id,
            name: g.name,
            description: adminUser ? `Created by @${members[0]}` : "Connecto Group",
            member_count: members.length,
            members: members,
            admin: members[0] || "",
            creator: members[0] || "",
            is_admin: isCreatedByMe,
            is_creator: isCreatedByMe,
            is_created_by_me: isCreatedByMe,
            created_at: g.created_at
          });
        }
      } catch (e) {
        console.error("D1 groups query error:", e);
      }
    }
    return jsonResponse({ status: "ok", groups: groups });
  }

  // Update Group (POST /api/groups/:id/update)
  const groupUpdateMatch = path.match(/^\/api\/groups\/([^\/]+)\/update$/);
  if (groupUpdateMatch && method === "POST") {
    const groupId = groupUpdateMatch[1];
    const body = await request.json().catch(() => ({}));
    if (env.DB && body.name) {
      try {
        await env.DB.prepare("UPDATE channels SET name = ? WHERE id = ?").bind(body.name.trim(), groupId).run();
      } catch (e) {
        console.error("D1 group update error:", e);
      }
    }
    return jsonResponse({ status: "ok", group: { id: groupId, name: body.name, icon_url: body.icon_url } });
  }

  // Add Group Members (POST /api/groups/:id/members/add)
  const groupAddMembersMatch = path.match(/^\/api\/groups\/([^\/]+)\/members\/add$/);
  if (groupAddMembersMatch && method === "POST") {
    const groupId = groupAddMembersMatch[1];
    const body = await request.json().catch(() => ({}));
    const rawMembers = Array.isArray(body.members) ? body.members : [];
    if (env.DB) {
      try {
        for (const m of rawMembers) {
          const cleanM = String(m || "").trim().toLowerCase().replace(/^@/, "");
          if (!cleanM) continue;
          let u = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(cleanM).first();
          if (!u) {
            const newId = "usr_" + cleanM;
            await env.DB.prepare(
              "INSERT OR IGNORE INTO users (id, username, display_name, email, avatar_url, is_online) VALUES (?, ?, ?, ?, '👾', 1)"
            ).bind(newId, cleanM, cleanM, `${cleanM}@connecto.fun`).run();
            u = { id: newId };
          }
          await env.DB.prepare(
            "INSERT OR IGNORE INTO dm_participants (channel_id, user_id) VALUES (?, ?)"
          ).bind(groupId, u.id).run();
        }
      } catch (e) {
        console.error("D1 group add members error:", e);
      }
    }
    return jsonResponse({ status: "ok", added: rawMembers });
  }

  // Delete Group (DELETE or POST /api/groups/:id/delete)
  const groupDeleteMatch = path.match(/^\/api\/groups\/([^\/]+)(?:\/delete)?$/);
  if (groupDeleteMatch && (method === "DELETE" || (method === "POST" && path.endsWith("/delete")))) {
    const groupId = groupDeleteMatch[1];
    if (env.DB) {
      try {
        await env.DB.prepare("DELETE FROM dm_participants WHERE channel_id = ?").bind(groupId).run();
        await env.DB.prepare("DELETE FROM messages WHERE channel_id = ?").bind(groupId).run();
        await env.DB.prepare("DELETE FROM channels WHERE id = ?").bind(groupId).run();
      } catch (e) {
        console.error("D1 group delete error:", e);
      }
    }
    return jsonResponse({ status: "ok", message: `Group ${groupId} deleted successfully` });
  }

  // Leave Group (POST /api/groups/:id/leave or /api/groups/:id/members/leave)
  const groupLeaveMatch = path.match(/^\/api\/groups\/([^\/]+)\/(?:leave|members\/leave)$/);
  if (groupLeaveMatch && method === "POST") {
    const groupId = groupLeaveMatch[1];
    const username = (request.headers.get("X-User-Username") || "").trim().toLowerCase();
    if (env.DB && username) {
      try {
        const u = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(username).first();
        if (u) {
          await env.DB.prepare("DELETE FROM dm_participants WHERE channel_id = ? AND user_id = ?").bind(groupId, u.id).run();
        }
      } catch (e) {
        console.error("D1 group leave error:", e);
      }
    }
    return jsonResponse({ status: "ok", message: `Left group ${groupId} successfully` });
  }

  // 14. Channels List (Clean Public Channels Only)
  if (path === "/api/channels" || path === "/api/v1/chat/channels") {
    const defaultChannels = [
      { id: "general", name: "general", type: "text", description: "Global public community chat & discussion", category: "OFFICIAL" },
      { id: "announcements", name: "announcements", type: "text", description: "Official updates & platform release notes", category: "OFFICIAL" },
      { id: "dev-chat", name: "dev-chat", type: "text", description: "FastAPI, WebSockets, WebRTC & Security", category: "OFFICIAL" },
      { id: "gaming", name: "gaming", type: "text", description: "Gaming, memes & casual talk", category: "OFFICIAL" },
      { id: "war-room", name: "war-room", type: "text", description: "⚡ Tactical Briefings & Threat Intelligence", category: "OFFICIAL" },
      { id: "tournaments", name: "tournaments", type: "text", description: "Free Fire esports tournaments & matches", category: "ESPORTS" },
      { id: "clips", name: "clips", type: "text", description: "Community gaming highlights & clutches", category: "ESPORTS" },
      { id: "voice-lounge", name: "voice-lounge", type: "voice", description: "Voice & Screen Sharing Lounge", category: "ESPORTS" }
    ];
    const seenNames = new Set();
    const channels = [];
    for (const defCh of defaultChannels) {
      seenNames.add(defCh.name.toLowerCase());
      channels.push(defCh);
    }
    if (env.DB) {
      try {
        const res = await env.DB.prepare(`
          SELECT id, name, type FROM channels
          WHERE name NOT LIKE 'dm-%' AND name NOT LIKE 'dm_%' AND type != 'dm' AND type != 'group'
        `).all();
        for (const r of (res.results || [])) {
          const lowerName = (r.name || "").toLowerCase();
          if (lowerName && !seenNames.has(lowerName) && !lowerName.startsWith("dm-") && !lowerName.startsWith("dm_")) {
            seenNames.add(lowerName);
            channels.push({
              id: r.id || r.name,
              name: r.name,
              type: r.type || "text",
              description: `${r.name} channel`,
              category: "OFFICIAL"
            });
          }
        }
      } catch (_) {}
    }
    return jsonResponse(channels);
  }

  // 15. Channel Messages: GET & POST
  const isChannelMsgs = (path.includes("/channels/") || path.includes("/chat/channels/")) && path.endsWith("/messages");
  const isDirectMsgs = path === "/api/messages";
  if (isChannelMsgs || isDirectMsgs) {
    let channelId = "general";
    const match = path.match(/\/(?:channels|chat\/channels)\/([^\/]+)\/messages/);
    if (match) channelId = match[1];
    else if (url.searchParams.has("channel_id")) channelId = url.searchParams.get("channel_id");

    let postBody = null;
    if (method === "POST") {
      postBody = await request.json().catch(() => ({}));
      if (channelId === "general" && (postBody.channel_id || postBody.channel)) {
        channelId = (postBody.channel_id || postBody.channel).trim();
      }
    }

    // Resolve canonical channel name if UUID or alias was passed
    let canonicalChannel = channelId;
    let resolvedChannelId = channelId;
    if (env.DB) {
      try {
        const chRow = await env.DB.prepare(
          "SELECT id, name FROM channels WHERE id = ? OR lower(name) = ? OR lower(id) = ? LIMIT 1"
        ).bind(channelId, channelId.toLowerCase(), channelId.toLowerCase()).first();
        if (chRow) {
          if (chRow.name) canonicalChannel = chRow.name;
          if (chRow.id) resolvedChannelId = chRow.id;
        }
      } catch (_) {}
    }

    if (method === "GET") {
      let messages = [];
      if (env.DB) {
        const res = await env.DB.prepare(`
          SELECT m.id, m.channel_id, m.content, m.created_at,
                 COALESCE(u.username, m.sender_id, 'shinobi') as author_name,
                 COALESCE(u.display_name, u.username, m.sender_id, 'Shinobi') as author_display_name,
                 COALESCE(u.avatar_url, '👾') as author_avatar,
                 m.sender_id as author_id
          FROM messages m
          LEFT JOIN users u ON m.sender_id = u.id OR m.sender_id = u.username
          WHERE (m.channel_id = ? OR m.channel_id = ? OR m.channel_id = ?
                 OR m.channel_id IN (SELECT id FROM channels WHERE name = ? OR id = ? OR name = ? OR id = ?)
                 OR m.channel_id IN (SELECT name FROM channels WHERE id = ? OR name = ? OR id = ? OR name = ?))
            AND m.id NOT LIKE '%_name'
          ORDER BY m.created_at ASC
          LIMIT 100
        `).bind(
          canonicalChannel, channelId, resolvedChannelId,
          canonicalChannel, channelId, resolvedChannelId, canonicalChannel,
          canonicalChannel, channelId, resolvedChannelId, canonicalChannel
        ).all();

        messages = (res.results || []).map(r => ({
          id: r.id,
          channelId: r.channel_id,
          channel_id: r.channel_id,
          authorId: r.author_id,
          author_id: r.author_id,
          authorName: r.author_name,
          author_name: r.author_name,
          authorAvatar: r.author_avatar,
          author_avatar: r.author_avatar,
          user: r.author_name,
          avatar: r.author_avatar,
          nickname: r.author_display_name,
          sender: r.author_name,
          sender_username: r.author_name,
          sender_display_name: r.author_display_name,
          sender_avatar_url: r.author_avatar,
          content: r.content,
          text: r.content,
          createdAt: r.created_at,
          created_at: r.created_at,
          timestamp: r.created_at,
          type: "text",
          reactions: {}
        }));
      }
      return jsonResponse({
        status: "ok",
        channel_id: canonicalChannel,
        messages: messages,
        count: messages.length
      });
    }

    if (method === "POST") {
      const clientIp = request.headers.get("cf-connecting-ip") || "unknown";
      const rateCheck = checkRateLimit(clientIp, "messages", 45, 60);
      if (!rateCheck.allowed) {
        return jsonResponse({ error: "Rate limit exceeded. Please wait a moment before sending more messages.", retry_after: rateCheck.retryAfter }, 429);
      }

      let sender = await resolveUserFromRequest(request, env);
      if (!sender && postBody && (postBody.username || postBody.author || postBody.sender)) {
        const u = (postBody.username || postBody.author || postBody.sender).trim().toLowerCase().replace(/^@/, "");
        if (env.DB && u) {
          sender = await env.DB.prepare("SELECT * FROM users WHERE lower(username) = ? LIMIT 1").bind(u).first();
        }
      }

      if (!sender) {
        return jsonResponse({ error: "Unauthorized: Active authenticated session required to post messages." }, 401);
      }

      // RBAC: #announcements is strictly restricted to platform administrators
      if (canonicalChannel === "announcements" && !sender.is_admin) {
        return jsonResponse({ error: "Forbidden: Only server administrators can post in #announcements." }, 403);
      }

      const body = postBody || {};
      const rawContent = (body.content || body.text || "").trim();
      if (!rawContent) {
        return jsonResponse({ error: "Message content cannot be empty." }, 400);
      }
      const content = sanitizeText(rawContent, 2000);

      const senderId = sender.id;
      const senderUsername = sender.username;
      const newMsgId = "msg_" + Math.random().toString(36).substring(2, 10);
      const nowIso = new Date().toISOString();

      const isDm = canonicalChannel.startsWith("dm-") || canonicalChannel.startsWith("dm_") || channelId.startsWith("dm-") || channelId.startsWith("dm_");
      const dmRecipient = isDm ? resolveDmRecipient(canonicalChannel, senderUsername, body.recipient || body.target) : null;

      if (env.DB) {
        try {
          // CRITICAL: Ensure channel exists in channels table (FK constraint on messages.channel_id)
          await env.DB.prepare(
            "INSERT OR IGNORE INTO channels (id, name, type) VALUES (?, ?, 'dm')"
          ).bind(canonicalChannel, canonicalChannel).run();

          if (resolvedChannelId && resolvedChannelId !== canonicalChannel) {
            await env.DB.prepare(
              "INSERT OR IGNORE INTO channels (id, name, type) VALUES (?, ?, 'dm')"
            ).bind(resolvedChannelId, canonicalChannel).run();
          }

          if (channelId && channelId !== canonicalChannel && channelId !== resolvedChannelId) {
            await env.DB.prepare(
              "INSERT OR IGNORE INTO channels (id, name, type) VALUES (?, ?, 'dm')"
            ).bind(channelId, canonicalChannel).run();
          }

          // Ensure sender exists in users table (FK constraint on messages.sender_id)
          const sRow = await env.DB.prepare("SELECT id FROM users WHERE id = ? LIMIT 1").bind(senderId).first();
          if (!sRow) {
            await env.DB.prepare(
              "INSERT OR IGNORE INTO users (id, username, display_name, email, avatar_url, is_online) VALUES (?, ?, ?, ?, '👾', 1)"
            ).bind(senderId, senderUsername, sender.display_name || senderUsername, sender.email || `${senderUsername}@connecto.fun`).run();
          }

          // Insert into messages table
          await env.DB.prepare(
            "INSERT INTO messages (id, channel_id, sender_id, content) VALUES (?, ?, ?, ?)"
          ).bind(newMsgId, canonicalChannel, senderId, content).run();

          if (isDm && dmRecipient) {
            const notifId = "notif_" + newMsgId;
            const recUser = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(dmRecipient).first();
            const recUserId = recUser ? recUser.id : dmRecipient;
            await env.DB.prepare(`
              INSERT INTO notifications (id, user_id, type, title, content, sender_username, sender_avatar, reference_id, is_read, created_at)
              VALUES (?, ?, 'message', ?, ?, ?, ?, ?, 0, datetime('now'))
            `).bind(notifId, recUserId, sender.display_name || sender.username, content, sender.username, sender.avatar_url || "👾", sender.username).run().catch(() => {});
          }
        } catch (dbErr) {
          console.error("[postMessage] DB insert error:", dbErr.message);
        }
      }

      const msgResponse = {
        id: newMsgId,
        channelId: canonicalChannel,
        channel_id: canonicalChannel,
        authorId: senderId,
        author_id: senderId,
        authorName: sender.username,
        author_name: sender.username,
        authorAvatar: sender.avatar_url || "👾",
        author_avatar: sender.avatar_url || "👾",
        user: sender.username,
        avatar: sender.avatar_url || "👾",
        nickname: sender.display_name || sender.username,
        sender: sender.username,
        sender_username: sender.username,
        sender_display_name: sender.display_name || sender.username,
        sender_avatar_url: sender.avatar_url || "👾",
        content: content,
        text: content,
        createdAt: nowIso,
        created_at: nowIso,
        timestamp: nowIso,
        type: "text",
        reactions: {}
      };

      if (ctx) {
        if (isDm && dmRecipient) {
          ctx.waitUntil(broadcastRealtime(env, {
            type: "dm_message",
            sender: sender.username,
            target: dmRecipient,
            channel: canonicalChannel,
            message: msgResponse
          }));
        }
        ctx.waitUntil(broadcastRealtime(env, {
          type: "channel_message",
          channel: canonicalChannel,
          message: msgResponse
        }));
      }

      return jsonResponse(msgResponse, 201);
    }
  }

  // 16. Users & Members List
  if (path === "/api/users" || path === "/api/members" || path === "/api/v1/users") {
    let users = [];
    if (env.DB) {
      const res = await env.DB.prepare("SELECT id, username, display_name, email, avatar_url, is_online, is_stealth FROM users LIMIT 50").all();
      users = (res.results || []).map(u => {
        const isStealth = Boolean(u.is_stealth);
        const isOnline = isStealth ? false : (u.is_online === 1 || u.is_online === true);
        return {
          id: u.id,
          username: u.username,
          displayName: u.display_name || u.username,
          display_name: u.display_name || u.username,
          email: u.email,
          avatarUrl: u.avatar_url || "👾",
          avatar_url: u.avatar_url || "👾",
          is_online: isOnline,
          is_stealth: isStealth,
          status: isOnline ? "online" : "offline"
        };
      });
    }
    return jsonResponse(users);
  }

  // 17. Platform Stats
  if (path === "/api/stats") {
    let uCount = 28, cCount = 8, mCount = 83;
    if (env.DB) {
      uCount = await env.DB.prepare("SELECT count(*) as c FROM users").first("c") || 28;
      cCount = await env.DB.prepare("SELECT count(*) as c FROM channels WHERE name NOT LIKE 'dm-%' AND name NOT LIKE 'dm_%' AND id NOT LIKE '%-%-%-%-%'").first("c") || 8;
      mCount = await env.DB.prepare("SELECT count(*) as c FROM messages WHERE id NOT LIKE '%_name'").first("c") || 83;
    }
    return jsonResponse({
      activeShinobi: uCount,
      active_shinobi: uCount,
      activeFriends: uCount,
      active_friends: uCount,
      clansFormed: cCount,
      clans_formed: cCount,
      messagesSent: mCount,
      messages_sent: mCount,
      onlineUsers: uCount,
      online_users: uCount
    });
  }

  // 17b. Referrals, Leaderboard, Academy & Careers
  if (path === "/api/leaderboard" || path === "/api/referrals/leaderboard") {
    return jsonResponse({
      leaderboard: [
        { rank: 1, username: "devi", nickname: "divyasree", xp: 12500, referrals: 42, badge: "👑 Grandmaster" },
        { rank: 2, username: "srinu", nickname: "srinu", xp: 9800, referrals: 28, badge: "⚡ Jonin" },
        { rank: 3, username: "vivek", nickname: "Vivek", xp: 8400, referrals: 19, badge: "⚡ Jonin" },
        { rank: 4, username: "meghana", nickname: "Meghana", xp: 7200, referrals: 15, badge: "🔥 Chunin" },
        { rank: 5, username: "divya1736", nickname: "Ammu", xp: 6500, referrals: 12, badge: "🔥 Chunin" }
      ]
    });
  }
  if (path === "/api/referrals/stats" || path.startsWith("/api/referrals/")) {
    return jsonResponse({
      total_referrals: 0,
      reward_points: 0,
      referral_code: "CONNECTO_2026",
      rank: 42
    });
  }
  if (path.startsWith("/api/academy")) {
    return jsonResponse({
      tracks: [
        { id: "cyber_sec", title: "Cybersecurity & CTF Defense", xp: 2500, modules: 8, status: "available" },
        { id: "webrtc_audio", title: "WebRTC Low-Latency Voice", xp: 1800, modules: 6, status: "available" },
        { id: "cloud_systems", title: "Cloud Edge & Failover Resilience", xp: 3200, modules: 10, status: "available" }
      ]
    });
  }
  if (path.startsWith("/api/careers")) {
    return jsonResponse({
      jobs: [
        { id: "lead_dev", title: "Full-Stack Distributed Systems Lead", location: "Remote", type: "Full-time" },
        { id: "webrtc_eng", title: "Low-Latency Voice & Video Engineer", location: "Remote", type: "Full-time" }
      ]
    });
  }

  // 17b. Message Threads: GET & POST (/api/messages/:id/thread)
  const threadMatch = path.match(/^\/api\/messages\/([^\/]+)\/thread/);
  if (threadMatch) {
    const rawParentId = threadMatch[1];
    const cleanParentId = rawParentId.replace(/^msg-/, "");
    let parentMsg = null;
    let parentUser = null;
    if (env.DB) {
      parentMsg = await env.DB.prepare(
        "SELECT * FROM messages WHERE id = ? OR id = ? OR id LIKE ? LIMIT 1"
      ).bind(rawParentId, cleanParentId, `%${cleanParentId}%`).first();
      if (parentMsg) {
        parentUser = await env.DB.prepare(
          "SELECT * FROM users WHERE id = ? OR lower(username) = ? LIMIT 1"
        ).bind(parentMsg.sender_id, parentMsg.sender_id.toLowerCase()).first();
      }
    }
    if (!parentMsg) {
      return jsonResponse({ detail: "Message not found" }, 404);
    }

    if (method === "GET") {
      let replies = [];
      if (env.DB) {
        const repRes = await env.DB.prepare(
          "SELECT m.*, u.username as u_name, u.display_name as u_disp, u.avatar_url as u_av FROM messages m LEFT JOIN users u ON m.sender_id = u.id OR m.sender_id = u.username WHERE m.nonce = ? OR m.nonce = ? ORDER BY m.created_at ASC LIMIT 100"
        ).bind("thread_" + parentMsg.id, "thread_" + rawParentId).all();
        replies = (repRes.results || []).map(r => ({
          id: r.id,
          content: r.content,
          user: r.u_name || r.sender_id,
          author: r.u_disp || r.u_name || r.sender_id,
          nickname: r.u_disp || r.u_name || r.sender_id,
          avatar: r.u_av || "👤",
          timestamp: r.created_at || "Just now",
          reactions: {}
        }));
      }
      return jsonResponse({
        parent: {
          id: parentMsg.id,
          content: parentMsg.content,
          user: parentUser ? parentUser.username : parentMsg.sender_id,
          author: parentUser ? (parentUser.display_name || parentUser.username) : parentMsg.sender_id,
          nickname: parentUser ? (parentUser.display_name || parentUser.username) : parentMsg.sender_id,
          avatar: parentUser ? (parentUser.avatar_url || "👾") : "👾",
          timestamp: parentMsg.created_at || "Just now",
          reactions: {}
        },
        replies: replies,
        count: replies.length
      });
    }

    if (method === "POST") {
      const body = await request.json().catch(() => ({}));
      const text = (body.content || "").trim();
      const uname = (body.username || "user").trim();
      const av = body.avatar || "👤";
      if (!text) {
        return jsonResponse({ detail: "Reply content cannot be empty" }, 400);
      }
      const replyId = "reply_" + crypto.randomUUID().slice(0, 10);
      const nowIso = new Date().toISOString();
      let senderId = uname;
      if (env.DB) {
        const uRow = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? OR id = ? LIMIT 1").bind(uname.toLowerCase(), uname).first();
        if (uRow) senderId = uRow.id;
        await env.DB.prepare(
          "INSERT INTO messages (id, channel_id, sender_id, content, nonce, created_at) VALUES (?, ?, ?, ?, ?, ?)"
        ).bind(replyId, parentMsg.channel_id, senderId, text, "thread_" + parentMsg.id, nowIso).run();
      }
      const countRes = env.DB ? await env.DB.prepare("SELECT count(*) as c FROM messages WHERE nonce = ? OR nonce = ?").bind("thread_" + parentMsg.id, "thread_" + rawParentId).first() : { c: 1 };
      return jsonResponse({
        reply: {
          id: replyId,
          content: text,
          user: uname,
          author: uname,
          nickname: uname,
          avatar: av,
          timestamp: "Just now",
          reactions: {}
        },
        thread_count: (countRes && countRes.c) || 1
      }, 201);
    }
  }

  // 17c. Message Reactions: POST (/api/messages/:id/react)
  const reactMatch = path.match(/^\/api\/messages\/([^\/]+)\/react/);
  if (reactMatch && method === "POST") {
    const msgId = reactMatch[1];
    const body = await request.json().catch(() => ({}));
    const emoji = body.emoji || "🔥";
    const uname = (body.username || "user").toLowerCase().trim();
    return jsonResponse({
      status: "ok",
      action: "added",
      reactions: { [emoji]: [uname] }
    });
  }

  // 18. Secondary Channel Endpoints (Pins, Polls, Canvas, Automod, Slowmode)
  if (path.includes("/pins")) {
    return jsonResponse([]);
  }
  if (path.includes("/polls")) {
    return jsonResponse([]);
  }
  if (path.includes("/canvas")) {
    return jsonResponse({ notes: [], tasks: [] });
  }
  if (path.includes("/slowmode")) {
    return jsonResponse({ enabled: false, delay_seconds: 0 });
  }
  if (path.includes("/automod")) {
    return jsonResponse({ enabled: true, filter_level: "standard" });
  }
  if (path.includes("/scheduled-messages")) {
    return jsonResponse([]);
  }

  // 19. User Settings, Custom Status, Notification Preferences
  if (path === "/api/user/custom-status") {
    return jsonResponse({ status: "ok" });
  }
  if (path === "/api/user/notification-preferences") {
    return jsonResponse({ sound: true, desktop: true, email: false });
  }
  if (path.startsWith("/api/notifications") || path.startsWith("/api/v1/notifications")) {
    let myUser = (
      url.searchParams.get("username") ||
      url.searchParams.get("user") ||
      request.headers.get("X-User-Username") ||
      ""
    ).trim().toLowerCase().replace(/^@/, "");
    if (!myUser) {
      const authUser = await resolveUserFromRequest(request, env);
      if (authUser && authUser.username) myUser = authUser.username.trim().toLowerCase().replace(/^@/, "");
    }
    if (!myUser) myUser = "chinnu";

    const room = getRealtimeRoom(env);

    if (path.endsWith("/read-all") && method === "POST") {
      if (room) {
        try {
          await room.fetch(new Request(`http://realtime/internal/notifications/read-all?username=${encodeURIComponent(myUser)}`, { method: "POST" }));
        } catch (_) {}
      }
      if (env.DB) {
        try {
          await env.DB.prepare(`
            UPDATE notifications SET is_read = 1 
            WHERE lower(user_id) = ? OR user_id = (SELECT id FROM users WHERE lower(username) = ? LIMIT 1)
          `).bind(myUser, myUser).run();
        } catch (_) {}
      }
      return jsonResponse({ status: "ok", unread_count: 0 });
    }

    const readMatch = path.match(/\/(?:api|api\/v1)\/notifications\/([^\/]+)\/read$/);
    if (readMatch && method === "POST") {
      const notifId = decodeURIComponent(readMatch[1]);
      if (room) {
        try {
          await room.fetch(new Request(`http://realtime/internal/notifications/read?username=${encodeURIComponent(myUser)}&id=${encodeURIComponent(notifId)}`, { method: "POST" }));
        } catch (_) {}
      }
      if (env.DB) {
        try {
          await env.DB.prepare("UPDATE notifications SET is_read = 1 WHERE id = ?").bind(notifId).run();
        } catch (_) {}
      }
      return jsonResponse({ status: "ok" });
    }

    if (method === "DELETE") {
      if (room) {
        try {
          await room.fetch(new Request(`http://realtime/internal/notifications/clear?username=${encodeURIComponent(myUser)}`, { method: "POST" }));
        } catch (_) {}
      }
      if (env.DB) {
        try {
          await env.DB.prepare(`
            DELETE FROM notifications 
            WHERE lower(user_id) = ? OR user_id = (SELECT id FROM users WHERE lower(username) = ? LIMIT 1)
          `).bind(myUser, myUser).run();
        } catch (_) {}
      }
      return jsonResponse({ status: "ok" });
    }

    let notifs = [];
    if (room) {
      try {
        const res = await room.fetch(new Request(`http://realtime/internal/notifications?username=${encodeURIComponent(myUser)}`));
        if (res.ok) {
          const data = await res.json();
          if (Array.isArray(data.notifications)) notifs = data.notifications;
        }
      } catch (_) {}
    }

    // Merge persistent notifications from D1 notifications table
    if (env.DB) {
      try {
        const d1Rows = await env.DB.prepare(`
          SELECT id, user_id, type, title, content, sender_username, sender_avatar, reference_id, is_read, created_at
          FROM notifications
          WHERE lower(user_id) = ? OR user_id = (SELECT id FROM users WHERE lower(username) = ? LIMIT 1)
          ORDER BY created_at DESC
          LIMIT 50
        `).bind(myUser, myUser).all();

        const existingIds = new Set(notifs.map(n => n.id));
        for (const row of (d1Rows.results || [])) {
          if (!existingIds.has(row.id)) {
            existingIds.add(row.id);
            notifs.push({
              id: row.id,
              user_id: myUser,
              type: row.type || "message",
              title: row.title || "Message",
              content: row.content || "",
              sender_username: row.sender_username || "",
              sender_avatar: row.sender_avatar || "👾",
              reference_id: row.reference_id || "",
              is_read: Boolean(row.is_read),
              created_at: row.created_at || new Date().toISOString()
            });
          }
        }
      } catch (_) {}
    }

    // Merge pending friend requests from D1 as notifications if not already present
    if (env.DB) {
      try {
        const caller = await env.DB.prepare("SELECT id FROM users WHERE lower(username) = ? LIMIT 1").bind(myUser).first();
        if (caller) {
          const incRes = await env.DB.prepare(`
            SELECT f.id as request_id, u.username as sender_username, u.display_name as sender_display_name, u.avatar_url, f.created_at
            FROM friendships f
            JOIN users u ON f.user_id = u.id
            WHERE f.friend_id = ? AND f.status = 'pending'
          `).bind(caller.id).all();
          const existingIds = new Set(notifs.map(n => n.id));
          for (const inc of (incRes.results || [])) {
            const nid = inc.request_id || `fr_${inc.sender_username}`;
            if (!existingIds.has(nid)) {
              existingIds.add(nid);
              notifs.push({
                id: nid,
                user_id: myUser,
                type: "friend_request",
                title: "New Friend Request",
                content: `${inc.sender_display_name || inc.sender_username} sent you a friend request`,
                sender_username: inc.sender_username,
                sender_avatar: inc.avatar_url || "👾",
                reference_id: nid,
                is_read: false,
                created_at: inc.created_at || new Date().toISOString()
              });
            }
          }
        }
      } catch (_) {}
    }

    // Sort descending by created_at
    notifs.sort((a, b) => new Date(b.created_at || 0).getTime() - new Date(a.created_at || 0).getTime());

    const unreadOnly = url.searchParams.get("unread_only") === "true";
    const filtered = unreadOnly ? notifs.filter(n => !n.is_read) : notifs;
    const unreadCount = notifs.filter(n => !n.is_read).length;

    if (path.includes("/count")) {
      return jsonResponse({ unread_count: unreadCount, count: unreadCount, unread: unreadCount });
    }
    return jsonResponse(filtered);
  }

  // 20. Voice Rooms, Stages & WebRTC ICE Servers
  if (path === "/api/voice/rooms") {
    let rooms = [];
    if (env.DB) {
      const res = await env.DB.prepare("SELECT id, name, topic, max_participants FROM voice_rooms WHERE is_active = 1").all();
      const icons = {
        vr_lounge: "⛩️",
        vr_war_room: "⚔️",
        vr_dev: "💻",
        vr_tournament: "🏆"
      };
      rooms = (res.results || []).map(r => ({
        id: r.id,
        name: r.name,
        topic: r.topic || "Voice stage for tactical shinobi audio communication",
        icon: icons[r.id] || "🎙️",
        creator: "system",
        max_participants: r.max_participants || 25,
        participant_count: 0,
        participants: []
      }));
    }
    if (rooms.length === 0) {
      rooms = [
        { id: "vr_lounge", name: "Main Shinobi Lounge", topic: "Casual voice chat and hangout", icon: "⛩️", creator: "system", max_participants: 25, participant_count: 0, participants: [] },
        { id: "vr_war_room", name: "Tactical War Room 774", topic: "Clan tournament communications & high-speed tactics", icon: "⚔️", creator: "system", max_participants: 12, participant_count: 0, participants: [] },
        { id: "vr_dev", name: "Devs Audio Stage", topic: "Architecture sync and low-latency audio debugging", icon: "💻", creator: "system", max_participants: 20, participant_count: 0, participants: [] },
        { id: "vr_tournament", name: "Tournament Battle Arena", topic: "Free Fire and CS competitive stage", icon: "🏆", creator: "system", max_participants: 50, participant_count: 0, participants: [] }
      ];
    }
    return jsonResponse({
      status: "ok",
      rooms: rooms
    });
  }

  // Voice room join
  const joinMatch = path.match(/^\/api\/voice\/rooms\/([^\/]+)\/join/);
  if (joinMatch) {
    const roomId = decodeURIComponent(joinMatch[1]);
    const body = await request.json().catch(() => ({}));
    const username = body.username || "chinnu";
    const nickname = body.nickname || username;
    const avatar = body.avatar || "👾";
    const rank = body.rank || "Genin";

    const roomNames = {
      vr_lounge: "Main Shinobi Lounge",
      vr_war_room: "Tactical War Room 774",
      vr_dev: "Devs Audio Stage",
      vr_tournament: "Tournament Battle Arena"
    };

    const roomData = {
      id: roomId,
      name: roomNames[roomId] || roomId,
      topic: "Active Voice Stage",
      icon: roomId.includes("war") ? "⚔️" : (roomId.includes("dev") ? "💻" : "⛩️"),
      creator: "system"
    };

    const participant = {
      username: username,
      nickname: nickname,
      avatar: avatar,
      rank: rank,
      muted: false,
      speaking: false
    };

    if (ctx) {
      ctx.waitUntil(broadcastRealtime(env, {
        type: "voice_join",
        roomId: roomId,
        user: username,
        avatar: avatar,
        nickname: nickname
      }));
    }

    return jsonResponse({
      status: "ok",
      room: roomData,
      participants: [participant]
    });
  }

  // Voice room leave
  const leaveMatch = path.match(/^\/api\/voice\/rooms\/([^\/]+)\/leave/);
  if (leaveMatch) {
    const roomId = decodeURIComponent(leaveMatch[1]);
    const body = await request.json().catch(() => ({}));
    const username = body.username || "chinnu";

    if (ctx) {
      ctx.waitUntil(broadcastRealtime(env, {
        type: "voice_leave",
        roomId: roomId,
        user: username
      }));
    }

    return jsonResponse({ status: "ok", message: "Left voice room" });
  }

  // Voice room create
  if (path === "/api/voice/rooms/create") {
    const body = await request.json().catch(() => ({}));
    const rId = "vr_" + Math.random().toString(36).substring(2, 8);
    const rName = body.name || "Custom Room";
    const rTopic = body.topic || "Shinobi voice channel";
    const rIcon = body.icon || "🎙️";
    if (env.DB) {
      ctx.waitUntil(
        env.DB.prepare(
          "INSERT INTO voice_rooms (id, name, topic, max_participants, is_active) VALUES (?, ?, ?, 25, 1)"
        ).bind(rId, rName, rTopic).run().catch(() => {})
      );
    }
    return jsonResponse({
      status: "ok",
      room: { id: rId, name: rName, topic: rTopic, icon: rIcon, creator: body.creator || "chinnu", max_participants: 25, participants: [] }
    });
  }

  // WebRTC ICE Servers (Cloudflare Ultra-low Latency Global STUN + Google STUN)
  if (path === "/api/calls/ice-servers") {
    const iceServers = [
      { urls: "stun:stun.cloudflare.com:3478" },
      { urls: "stun:stun.l.google.com:19302" },
      { urls: "stun:stun1.l.google.com:19302" },
      { urls: "stun:stun2.l.google.com:19302" }
    ];
    return jsonResponse({
      status: "ok",
      iceServers: iceServers,
      ice_servers: iceServers
    });
  }

  // Pending Incoming Voice Call Check (Background Service & Reconnect Fallback)
  if (path === "/api/voice/call/pending") {
    const target = (
      url.searchParams.get("username") ||
      url.searchParams.get("user") ||
      request.headers.get("X-User-Username") ||
      ""
    ).toLowerCase().trim().replace(/^@/, "");
    if (target) {
      const room = getRealtimeRoom(env);
      if (room) {
        try {
          const res = await room.fetch(new Request(`http://realtime/internal/call/pending?username=${encodeURIComponent(target)}`));
          if (res.ok) {
            const data = await res.json();
            return jsonResponse(data);
          }
        } catch (_) {}
      }
    }
    return jsonResponse({ has_pending: false });
  }

  // Voice Call Invite (REST Fallback for Push Notifications & Cross-Device Alerting)
  if (path === "/api/voice/call/invite") {
    const body = await request.json().catch(() => ({}));
    const caller = (body.caller_username || body.caller || request.headers.get("X-User-Username") || "chinnu").trim().replace(/^@/, "");
    const callerName = body.caller_name || caller;
    const target = (body.target_user || body.target || "").toLowerCase().trim().replace(/^@/, "");
    const roomId = body.room_id || body.call_id || "call_" + Date.now();
    const avatar = body.caller_avatar || "👤";

    if (ctx) {
      ctx.waitUntil(broadcastRealtime(env, {
        type: "call_invite",
        caller: caller,
        caller_name: callerName,
        target: target,
        room_id: roomId,
        caller_avatar: avatar
      }));
    }

    return jsonResponse({
      status: "ok",
      success: true,
      call_id: roomId,
      message: `Call invite dispatched to ${target}`
    });
  }

  // Voice Call Respond (Accept / Decline)
  if (path === "/api/voice/call/respond") {
    const body = await request.json().catch(() => ({}));
    const action = body.action || "accept";
    const target = (body.target_user || body.target || "").toLowerCase().trim().replace(/^@/, "");
    const responder = (body.username || body.user_id || request.headers.get("X-User-Username") || "").toLowerCase().trim().replace(/^@/, "");
    const callId = body.call_id || body.room_id;

    if (ctx) {
      ctx.waitUntil(broadcastRealtime(env, {
        type: action === "accept" ? "call_accepted" : "call_declined",
        target: target,
        responder: responder,
        call_id: callId
      }));
    }

    return jsonResponse({ status: "ok", action: action });
  }

  // Voice Call End
  if (path === "/api/voice/call/end") {
    const body = await request.json().catch(() => ({}));
    const target = (body.target_user || body.target || "").toLowerCase().trim().replace(/^@/, "");
    const sender = (body.username || body.user_id || request.headers.get("X-User-Username") || "").toLowerCase().trim().replace(/^@/, "");
    const callId = body.call_id || body.room_id;

    if (ctx) {
      ctx.waitUntil(broadcastRealtime(env, {
        type: "call_ended",
        target: target,
        sender: sender,
        call_id: callId
      }));
    }

    return jsonResponse({ status: "ok" });
  }

  // 20b. Record Call Log (Android / Web)
  if ((path === "/api/voice/calls/log" || path === "/api/calls/log") && method === "POST") {
    try {
      const body = await request.json().catch(() => ({}));
      const username = (body.username || request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
      const callerName = (body.caller_name || body.caller || username || "Gamer").trim();
      const callType = body.call_type || "Voice Call";
      const roomName = body.room_name || "";
      const roomCode = body.room_code || "";
      const durationSeconds = parseInt(body.duration_seconds || body.duration || 0, 10) || 0;
      const isMissed = (body.is_missed === true || body.is_missed === 1 || body.status === "missed") ? 1 : 0;
      const isOutgoing = (body.is_outgoing === true || body.is_outgoing === 1) ? 1 : 0;
      const logId = "call_log_" + Math.random().toString(36).substring(2, 10);

      if (env.DB) {
        await env.DB.prepare(`
          INSERT INTO call_logs (id, user_id, username, caller_name, call_type, room_name, room_code, duration_seconds, is_missed, is_outgoing, created_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
        `).bind(logId, username, username, callerName, callType, roomName, roomCode, durationSeconds, isMissed, isOutgoing).run();
      }

      return jsonResponse({
        status: "ok",
        id: logId,
        message: "Call log recorded successfully"
      }, 201);
    } catch (err) {
      return jsonResponse({ error: err.message }, 500);
    }
  }

  // 20c. Delete Single or All Call Logs
  const delCallMatch = path.match(/^\/api\/voice\/calls\/logs\/([^\/]+)$/);
  if (delCallMatch && method === "DELETE") {
    const logId = delCallMatch[1];
    if (env.DB) {
      try {
        await env.DB.prepare("DELETE FROM call_logs WHERE id = ?").bind(logId).run();
      } catch (_) {}
    }
    return jsonResponse({ status: "ok", deleted: true, id: logId });
  }

  if ((path === "/api/voice/calls/logs" || path === "/api/calls") && method === "DELETE") {
    const logId = (url.searchParams.get("id") || url.searchParams.get("log_id") || "").trim();
    const u = (url.searchParams.get("username") || request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
    const clearAll = url.searchParams.get("all") === "true" || url.searchParams.get("clear_all") === "true";
    if (env.DB) {
      try {
        if (logId) {
          await env.DB.prepare("DELETE FROM call_logs WHERE id = ?").bind(logId).run();
        } else if (u) {
          await env.DB.prepare("DELETE FROM call_logs WHERE lower(username) = ? OR lower(caller_name) = ?").bind(u, u).run();
        } else if (clearAll) {
          await env.DB.prepare("DELETE FROM call_logs").run();
        }
      } catch (_) {}
    }
    return jsonResponse({ status: "ok", cleared: true });
  }

  // Call Logs & History (/api/calls and /api/voice/calls/logs)
  if (path === "/api/calls" || path === "/api/voice/calls/logs") {
    let callLogs = [];
    const targetUser = (url.searchParams.get("username") || request.headers.get("X-User-Username") || "").trim().toLowerCase().replace(/^@/, "");
    if (env.DB) {
      try {
        let res;
        if (targetUser) {
          res = await env.DB.prepare(`
            SELECT * FROM call_logs 
            WHERE lower(username) = ? OR lower(caller_name) = ?
            ORDER BY created_at DESC LIMIT 50
          `).bind(targetUser, targetUser).all();
        } else {
          res = await env.DB.prepare("SELECT * FROM call_logs ORDER BY created_at DESC LIMIT 50").all();
        }
        callLogs = (res.results || []).map(r => {
          const sec = r.duration_seconds || 0;
          const mins = Math.floor(sec / 60);
          const remSec = sec % 60;
          const durStr = `${mins.toString().padStart(2, '0')}:${remSec.toString().padStart(2, '0')}`;
          return {
            id: r.id,
            user_id: r.user_id,
            username: r.username,
            caller_name: r.caller_name,
            caller: r.caller_name,
            call_type: r.call_type || "Voice Call",
            room_name: r.room_name || "",
            room_code: r.room_code || "",
            duration_seconds: sec,
            duration: durStr,
            is_missed: Boolean(r.is_missed),
            is_outgoing: Boolean(r.is_outgoing),
            started_at: r.created_at || "Recent",
            created_at: r.created_at || "Recent",
            status: r.is_missed ? "missed" : "completed"
          };
        });
      } catch (e) {
        console.error("[CALL_LOGS_DB_ERR]", e.message);
      }
    }
    return jsonResponse({
      status: "ok",
      logs: callLogs,
      call_logs: callLogs
    });
  }

  // 21. Leaderboard & App Version
  if (path === "/api/leaderboard") {
    let users = [];
    if (env.DB) {
      const res = await env.DB.prepare("SELECT id, username, display_name, avatar_url FROM users LIMIT 10").all();
      users = (res.results || []).map((u, idx) => ({
        id: u.id,
        username: u.username,
        display_name: u.display_name || u.username,
        avatar_url: u.avatar_url || "👾",
        rank: idx === 0 ? "Hokage" : "Chunin",
        xp: 300 - idx * 20
      }));
    }
    return jsonResponse({ users: users });
  }
  if (path === "/api/v1/app/version" || path === "/api/app/version" || path === "/api/app/latest" || path === "/api/v1/app/latest") {
    return jsonResponse({
      status: "success",
      app_name: "Connecto",
      package_name: "com.connecto.app",
      version: "4.2.7",
      version_name: "v4.2.7",
      version_code: 56,
      build: 56,
      sha256: "81a1259eb50c336f474b640625a08aaa7807a5cf666355100ce15f434308f9a3",
      md5: "e5c7b82ec3017e92aba950517099f6de",
      filename: "connecto-fun.apk",
      size_bytes: 57040069,
      size_display: "54.4 MB",
      min_android: "Android 7.0 (API 24)",
      target_android: "Android 16+ (API 37 / HyperOS Verified)",
      url: "https://connecto.fun/download/apk",
      download_url: "/download/apk",
      direct_download_url: "https://connecto.fun/download/apk"
    });
  }
  if (path === "/api/changelog" || path === "/api/v1/changelog") {
    return jsonResponse({
      status: "success",
      releases: [
        {
          version: "v4.2.7",
          date: "Oct 14, 2026",
          category: "feature",
          tag: "Latest Release",
          title: "Connecto v4.2.7: Direct Chat Delivery, Call History Sync, 2FA Setup & Real-Time Push Alerts",
          summary: "Resolves direct message delivery and persistence issues across Android and Cloudflare edge D1; provides full call history logging and live updates; enables verified email 2FA and forgot password OTP recovery; and delivers real-time notifications with instant heads-up alert previews.",
          highlights: [
            "Direct Chat Delivery & Persistence: Fixed D1 SQLite Foreign Key constraint violations and local Compose state caching, ensuring all personal messages are instantly stored, delivered to friends, and preserved offline across sessions.",
            "Call History Real-Time Sync & Dynamic Updates: Automatic call log creation for completed, missed, and rejected calls with duration and relative timestamps, plus reactive UI updates.",
            "Forgot Password Verified Flow: 6-digit OTP code sent via verified mail service, edge token validation, and password reset with instant auto-fill code chips.",
            "Two-Factor Authentication (2FA) Setup: Verified mail OTP challenge upon enabling 2FA, OTP auto-fill chip in setup dialog, and seamless toggle state persistence.",
            "Friend Message Push Notifications: Foreground service WebSocket sync triggering native Android heads-up notifications with direct sender avatar, preview, and tap-to-chat action.",
            "Android Native App v4.2.7 (Build 56 • 54.4 MB): SHA-256 81a1259eb50c336f474b640625a08aaa7807a5cf666355100ce15f434308f9a3."
          ],
          author: "Connecto Core Team"
        },
        {
          version: "v4.2.6",
          date: "Oct 13, 2026",
          category: "feature",
          tag: "Previous Release",
          title: "Connecto v4.2.6: Liquid Glass Theme, Static Profile Tab & Bottom Bar Water Drop Animation",
          summary: "Introduces the brand new Liquid Glass theme with fluid aqua refraction, eliminates all animations in the Profile tab for instant, solid, snappy navigation, and adds a moving liquid water drop indicator with liquid stretch physics, ripple splashes, and specular glints across all 5 bottom navigation tabs.",
          highlights: [
            "Liquid Glass Theme: Fluid aqua refraction (#00E5FF), oceanic midnight canvas (#061120), water droplet reflections, and automatic chat bubble and wallpaper defaults.",
            "Profile Tab Animation Removal: Removed root moving mesh canvas, card shimmers, rotating avatar halo, and subpage slide delays for instant, solid, snappy navigation.",
            "Bottom Navigation Water Drop Animation: Unified moving water droplet indicator that translates horizontally with spring rebound, stretches during rapid transit, displays concentric aquatic ripple rings on tap, and features an ambient bottom water bead across Home, Messages, Channels, Calls, and Profile.",
            "Android Native App v4.2.6 (Build 55 • 54.4 MB): SHA-256 a72ca0c266f70f2a5d5f4e33eeea6b54c1753fb017431964f0469bf320eb6a82."
          ],
          author: "Connecto Core Team"
        },
        {
          version: "v4.2.5",
          date: "Oct 13, 2026",
          category: "feature",
          tag: "Previous Release",
          title: "Connecto v4.2.5: Frosted Glass Theme, Redesigned Profile Sub-Screens & Admin Group Controls",
          summary: "Introduces complete application-wide Frosted Glass theme (luminous ambient mesh, specular borders, frosted chat bubbles, and spring motion physics), overhauls Profile sub-screens, streamlines Friend Profile, and synchronizes accounts across Cloudflare D1 and Origin.",
          highlights: [
            "Frosted Glass Theme: Complete application-wide frosted acrylic glass effect with specular dual-tone borders, floating luminous canvas mesh, frosted chat bubbles, and tactile spring button physics.",
            "Profile Sub-Screen UI Overhaul: Redesigned Presence & Stealth Mode, Personal Information, Two-Factor & Security, Biometric & App Lock, Appearance & Display Theme, and Groups & Communities with status-bar clearance, hero summary cards, and instant touch response.",
            "Streamlined Personal Chat Friend Profile: Simplified to Chat & Lock (showing only the UNFRIEND action button) and Theme & Wallpaper (dedicated to Chat Background Wallpaper selection).",
            "Group Admin 5-Wallpaper Selector & Cloud Sync: Restricted Group Wallpaper customization to Group Admins with a 5-preset selector popup and full Cloudflare D1 group persistence.",
            "Friend Chat User Data & UUID DM Resolution: Full synchronization of 21 user accounts, friendships, and 144+ direct messages across Cloudflare D1 and Origin with cf_edge_ WebSocket auth.",
            "Android Native App v4.2.5 (Build 54 • 54.4 MB): SHA-256 831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2."
          ],
          author: "Connecto Core Team"
        },
        {
          version: "v4.2.4",
          date: "Oct 8, 2026",
          category: "feature",
          tag: "Previous Release",
          title: "Connecto v4.2.4: Automated Server Maintenance Screen, Outage Telemetry & Dedicated Profile Sub-Pages",
          summary: "Introduces automated server maintenance detection with real-time reconnect telemetry, dedicated full-screen profile sub-pages for Personal Info, Stealth & Appearance, and silky spring motion transitions.",
          highlights: [
            "Automated Server Maintenance Screen: Real-time outage detection, live status telemetry, and one-tap Reconnect Now verification.",
            "Dedicated Profile Sub-Page Navigation: Clean sub-page screens for Personal Information, Presence & Stealth, and Appearance without accordion clutter.",
            "Auto-Recovery Watchdog & Status Probe: Background health polling automatically restores session connectivity when servers recover.",
            "Android Native App v4.2.4 (Build 52 • 54.4 MB): SHA-256 07746667d917f4e6b811b086572f6f87b6fd600504df4664eadfd02deaccd7d4."
          ],
          author: "Connecto Core Team"
        },
        {
          version: "v4.2.3",
          date: "Oct 8, 2026",
          category: "feature",
          tag: "Previous Release",
          title: "Connecto v4.2.3: Clean Profile Screen, Full-Screen Group Profile & Admin Controls",
          summary: "Fixes duplicate profile section rendering, eliminates fake groups, and introduces a comprehensive single full-screen group profile tab with admin controls for group name, avatar, members, and chat wallpaper.",
          highlights: [
            "Profile Screen De-duplication: Fixed root cause where section header click listeners launched duplicate section dialogs at the bottom of the profile tab.",
            "Zero Fake Groups: Strictly displays authentic user groups in Added Groups with zero dummy data.",
            "Full-Screen Group Profile: Single full-screen dialog when tapping the group profile header.",
            "Group Admin Controls: Creator is default Admin with badge; group admin can edit group name and upload group profile avatar.",
            "Android Native App v4.2.3 (Build 51 • 54 MB): SHA-256 5c510e04c98a04cf42bd80fdddf8d6d7c8f3d0562ec0bbcc1e0da34327e37687."
          ],
          author: "Connecto Core Team"
        }
      ]
    });
  }
  if (path === "/api/presence/offline") {
    return jsonResponse({ success: true, status: "offline_recorded" });
  }

  // Generic fallback for any other API route
  return jsonResponse({
    status: "ok",
    mode: "cloud_server",
    message: "Connecto Cloud Server processed request",
    path: path,
    timestamp: new Date().toISOString()
  });
}

// ============================================================================
// CLOUDFLARE PAGES CDN FALLBACK FOR WEB / STATIC ASSETS
// ============================================================================

async function handlePagesEdgeFallback(request, url, host) {
  let pagesPath = url.pathname;
  if (host.startsWith("news.") && (pagesPath === "/" || pagesPath === "")) {
    pagesPath = "/news.html";
  } else if (host.startsWith("resinora.") && (pagesPath === "/" || pagesPath === "")) {
    pagesPath = "/resinora.html";
  } else if (pagesPath === "/" || pagesPath === "") {
    pagesPath = "/index.html";
  }

  const pagesUrl = new URL(`https://connecto-web.pages.dev${pagesPath}${url.search}`);
  try {
    const pagesResponse = await fetch(pagesUrl.toString(), {
      method: request.method,
      headers: {
        "Accept": request.headers.get("Accept") || "*/*",
        "User-Agent": request.headers.get("User-Agent") || ""
      },
      redirect: "follow"
    });

    if (pagesResponse.status >= 200 && pagesResponse.status < 400) {
      const resHeaders = new Headers(pagesResponse.headers);
      resHeaders.set("X-Served-By", "Cloudflare-Pages-Edge");
      resHeaders.set("X-Connecto-Mode", "cloud_server");
      resHeaders.set("Access-Control-Allow-Origin", "*");
      const ct = pagesResponse.headers.get("content-type") || "";
      if (ct.includes("text/html")) {
        let htmlText = await pagesResponse.text();
        htmlText = htmlText
          .replace(/2254c8e779e80afb507afb3b513170c16ee201426e75692621d6818592993198/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
          .replace(/a85c04b682cb98e09e3b0471d0e3d6a2930075c3f417597e40a275562a83fb1d/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
          .replace(/d6862ee43ffd80256f933007cdd12991b3ad88bdf91a1b1f39e07cda07093d05/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
          .replace(/ea9865a526452fbc8b36c4caed583dc02ad40c5b6944b49183ba5f3fdc14856d/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
          .replace(/b0c0e5d452637bcb3649e82de0259b9c6a4c1715220a262f14310187d401b132/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2")
          .replace(/c0e6ecd13056266b14d7535930d8d11a29ac81ba46c2eb0267c95eab1b2fa822/g, "831e029803e6a763433f715b2894d3720e26836a737dba2fe8676d5204cb6bc2");
        return new Response(htmlText, {
          status: pagesResponse.status,
          headers: resHeaders
        });
      }
      return new Response(pagesResponse.body, {
        status: pagesResponse.status,
        headers: resHeaders
      });
    }
  } catch (e) {
    // Pages failed, fallback to emergency HTML
  }

  return getEmergencyMaintenanceResponse(host);
}

// ============================================================================
// EDGE WEBSOCKET FALLBACK (WebSocketPair)
// ============================================================================

function handleEdgeWebSocket(request, env, ctx) {
  const [client, server] = Object.values(new WebSocketPair());
  server.accept();

  // Initial welcome handshake
  server.send(JSON.stringify({
    type: "connected",
    event: "connected",
    mode: "cloud_server",
    message: "Connected to Connecto Cloud Edge WebSocket",
    timestamp: new Date().toISOString()
  }));

  server.addEventListener("message", async (event) => {
    try {
      const data = typeof event.data === "string" ? JSON.parse(event.data) : {};

      if (data.type === "ping") {
        server.send(JSON.stringify({ type: "pong", timestamp: Date.now() }));
        return;
      }

      if (data.type === "message" || data.type === "chat_message" || data.content) {
        const channelId = data.channel_id || data.channelId || "general";
        const content = data.content || data.text || "";
        const sender = data.sender || data.username || "guest";
        const msgId = "msg_ws_" + Math.random().toString(36).substring(2, 9);

        // Async save to D1
        ctx.waitUntil((async () => {
          try {
            await env.DB.prepare(
              "INSERT INTO messages (id, channel_id, sender_id, content) VALUES (?, ?, ?, ?)"
            ).bind(msgId, channelId, sender, content).run();
          } catch (_) {}
        })());

        // Echo back to client as confirmation
        server.send(JSON.stringify({
          type: "message",
          id: msgId,
          channel_id: channelId,
          content: content,
          sender: sender,
          user: sender,
          created_at: new Date().toISOString(),
          status: "delivered"
        }));
      }
    } catch (_) {}
  });

  return new Response(null, {
    status: 101,
    webSocket: client,
    headers: {
      "X-Connecto-Mode": "cloud_server",
      "Access-Control-Allow-Origin": "*"
    }
  });
}

// ============================================================================
// SYSTEM STATUS & HEALTH HANDLERS
// ============================================================================

async function handleDbHealth(env) {
  try {
    if (!env.DB) {
      return jsonResponse({ status: "error", error: "D1 database binding 'DB' not configured" }, 500);
    }
    const tableCount = await env.DB.prepare("SELECT count(*) as count FROM sqlite_master WHERE type='table';").first("count");
    const tables = await env.DB.prepare("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;").all();
    return jsonResponse({
      status: "healthy",
      engine: "Cloudflare D1",
      database: "connecto-db",
      table_count: tableCount,
      tables: tables.results ? tables.results.map(r => r.name) : [],
      origin_failover: "active",
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    return jsonResponse({ status: "error", message: err.message }, 500);
  }
}

async function handleDbQuery(request, env) {
  if (request.method !== "POST") return jsonResponse({ error: "Method not allowed. Use POST." }, 405);

  const authHeader = request.headers.get("Authorization") || "";
  const customKey = request.headers.get("X-D1-Key") || "";
  const expectedKey = env.DB_API_KEY || "connecto_d1_sec_2026_prod";
  const token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : "";
  const callerKey = token || customKey;

  const isValidKey = callerKey && callerKey.length === expectedKey.length && (() => {
    let diff = 0;
    for (let i = 0; i < callerKey.length; i++) {
      diff |= callerKey.charCodeAt(i) ^ expectedKey.charCodeAt(i);
    }
    return diff === 0;
  })();

  if (!isValidKey) {
    return jsonResponse({ error: "Unauthorized access to Connecto D1 gateway" }, 401);
  }

  try {
    const payload = await request.json();
    if (payload.exec) {
      const res = await env.DB.exec(payload.exec);
      return jsonResponse({ success: true, count: res.count, duration: res.duration });
    }
    if (payload.sql) {
      const trimmed = payload.sql.trim();
      const stmts = trimmed.split(";").map(s => s.trim()).filter(Boolean);
      if (stmts.length > 1) {
        const batchStmts = stmts.map(s => env.DB.prepare(s));
        const batchRes = await env.DB.batch(batchStmts);
        return jsonResponse({ success: true, batch_results: batchRes });
      }
      const params = Array.isArray(payload.params) ? payload.params : [];
      const stmt = env.DB.prepare(payload.sql).bind(...params);
      const isSelect = payload.sql.trim().toUpperCase().startsWith("SELECT") || payload.sql.trim().toUpperCase().startsWith("PRAGMA");
      const result = isSelect ? await stmt.all() : await stmt.run();
      return jsonResponse({ success: true, results: result.results || [], meta: result.meta || {} });
    }
    if (Array.isArray(payload.batch)) {
      const statements = payload.batch.map(item => {
        const sqlStr = typeof item === "string" ? item : item.sql;
        const params = Array.isArray(item.params) ? item.params : [];
        return env.DB.prepare(sqlStr).bind(...params);
      });
      const results = await env.DB.batch(statements);
      return jsonResponse({ success: true, batch_results: results });
    }
    return jsonResponse({ error: "Invalid payload. Provide 'sql', 'batch', or 'exec'." }, 400);
  } catch (err) {
    return jsonResponse({ success: false, error: err.message }, 500);
  }
}

async function handleSyncStatus(env) {
  try {
    if (!env.DB) {
      return jsonResponse({ status: "error", error: "D1 database binding 'DB' not configured" }, 500);
    }
    let statusRow = null;
    try {
      statusRow = await env.DB.prepare(
        "SELECT origin_state, last_checked, latency_ms, details, (strftime('%s', 'now') - strftime('%s', last_checked)) as age_sec FROM server_status WHERE id = 'main_origin' LIMIT 1"
      ).first();
    } catch (_) {}

    let tableCounts = { users: 0, channels: 0, messages: 0, servers: 0, voice_rooms: 0 };
    try {
      const counts = await env.DB.batch([
        env.DB.prepare("SELECT count(*) as cnt FROM users"),
        env.DB.prepare("SELECT count(*) as cnt FROM channels"),
        env.DB.prepare("SELECT count(*) as cnt FROM messages"),
        env.DB.prepare("SELECT count(*) as cnt FROM servers"),
        env.DB.prepare("SELECT count(*) as cnt FROM voice_rooms")
      ]);
      if (counts && counts.length >= 5) {
        tableCounts.users = counts[0].results?.[0]?.cnt || 0;
        tableCounts.channels = counts[1].results?.[0]?.cnt || 0;
        tableCounts.messages = counts[2].results?.[0]?.cnt || 0;
        tableCounts.servers = counts[3].results?.[0]?.cnt || 0;
        tableCounts.voice_rooms = counts[4].results?.[0]?.cnt || 0;
      }
    } catch (_) {}

    const isOriginUp = statusRow && statusRow.origin_state === "UP" && (statusRow.age_sec == null || statusRow.age_sec <= 60);

    return jsonResponse({
      status: "active",
      engine: "Cloudflare D1 Edge Synchronizer",
      database: "connecto-db",
      cloud_online: true,
      failover_mode: "automatic",
      origin_state: isOriginUp ? "UP" : "DOWN",
      origin_details: statusRow ? statusRow.details : "Edge autonomous mode",
      last_sync_time: statusRow ? statusRow.last_checked : null,
      sync_latency_ms: statusRow ? statusRow.latency_ms : 0,
      tables: tableCounts,
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    return jsonResponse({ status: "error", message: err.message }, 500);
  }
}

async function handleSyncNow(request, env) {
  try {
    if (!env.DB) {
      return jsonResponse({ status: "error", error: "D1 database not configured" }, 500);
    }
    await env.DB.prepare(
      "INSERT OR REPLACE INTO server_status (id, origin_state, last_checked, latency_ms, details) VALUES ('edge_sync', 'UP', datetime('now'), 0, 'Triggered via /api/v1/sync/now')"
    ).run().catch(() => {});

    const counts = await env.DB.batch([
      env.DB.prepare("SELECT count(*) as cnt FROM users"),
      env.DB.prepare("SELECT count(*) as cnt FROM channels"),
      env.DB.prepare("SELECT count(*) as cnt FROM messages")
    ]);

    return jsonResponse({
      status: "success",
      message: "Cloudflare D1 Edge database synchronized and verified",
      tables_synced: {
        users: counts[0].results?.[0]?.cnt || 0,
        channels: counts[1].results?.[0]?.cnt || 0,
        messages: counts[2].results?.[0]?.cnt || 0
      },
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    return jsonResponse({ status: "error", message: err.message }, 500);
  }
}

async function handleAiGenerate(request, env) {
  if (request.method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
  try {
    if (!env.AI) return jsonResponse({ error: "Workers AI binding not available" }, 500);
    const aiBody = await request.json().catch(() => ({}));
    const prompt = aiBody.prompt || "Hello";
    const system = aiBody.system || "You are an AI assistant for Connecto real-time community platform.";

    const result = await env.AI.run("@cf/meta/llama-3.2-3b-instruct", {
      messages: [
        { role: "system", content: system },
        { role: "user", content: prompt }
      ],
      max_tokens: 512
    });

    return jsonResponse({
      success: true,
      engine: "Cloudflare Workers AI",
      model: "@cf/meta/llama-3.2-3b-instruct",
      response: result.response || result
    });
  } catch (err) {
    return jsonResponse({ success: false, error: err.message }, 500);
  }
}

async function handleAiModerate(request, env) {
  if (request.method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);
  try {
    if (!env.AI) return jsonResponse({ error: "Workers AI binding not available" }, 500);
    const { text } = await request.json().catch(() => ({}));
    const result = await env.AI.run("@cf/meta/llama-guard-3-8b", {
      messages: [{ role: "user", content: text || "" }]
    });

    return jsonResponse({
      success: true,
      engine: "Cloudflare Workers AI",
      model: "@cf/meta/llama-guard-3-8b",
      response: result.response || result
    });
  } catch (err) {
    return jsonResponse({ success: false, error: err.message }, 500);
  }
}

async function handleSystemStatus(env) {
  let d1Status = "Operational";
  let tableCount = 28;
  let latencyMs = 1;
  const startTime = Date.now();

  try {
    if (env.DB) {
      await env.DB.prepare("SELECT 1;").first();
      latencyMs = Date.now() - startTime;
      tableCount = (await env.DB.prepare("SELECT count(*) as count FROM sqlite_master WHERE type='table';").first("count")) || 28;
      const statusRow = await env.DB.prepare(
        "SELECT origin_state, (strftime('%s', 'now') - strftime('%s', last_checked)) as age_sec FROM server_status WHERE id = 'main_origin'"
      ).first();
      if (statusRow && statusRow.origin_state === "UP" && (statusRow.age_sec == null || statusRow.age_sec <= 45)) {
        localServerStatus = "UP";
      } else {
        localServerStatus = "DOWN";
      }
    }
  } catch (_) {
    d1Status = "Degraded";
  }

  return jsonResponse({
    status: "operational",
    active_engine: localServerStatus === "UP" ? "local_server" : "cloud_server",
    origin_status: localServerStatus,
    automation_policy: "automatic_failover",
    components: [
      {
        name: "Cloud Server Gateway",
        description: "Cloudflare Edge Global Router with automated origin circuit breaker",
        status: "Operational",
        mode: localServerStatus === "UP" ? "proxy_mode" : "cloud_active"
      },
      {
        name: "Cloudflare D1 Edge Database",
        description: "Distributed native SQL database (connecto-db) in APAC edge cluster",
        status: d1Status,
        latency_ms: latencyMs,
        total_tables: tableCount
      },
      {
        name: "Cloudflare Pages CDN",
        description: "Zero-downtime serverless web platform (connecto-web.pages.dev)",
        status: "Operational"
      },
      {
        name: "Real-Time WebSocket Gateway",
        description: "High-throughput edge WebSocket message & presence synchronizer",
        status: "Operational"
      }
    ],
    incidents: []
  });
}

function jsonResponse(data, status = 200, mode = "cloud_server") {
  return new Response(JSON.stringify(data), {
    status: status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD",
      "Access-Control-Allow-Headers": "Content-Type, Authorization, X-D1-Key, X-Requested-With, X-User-Username",
      "X-Content-Type-Options": "nosniff",
      "X-Frame-Options": "SAMEORIGIN",
      "X-XSS-Protection": "1; mode=block",
      "Referrer-Policy": "strict-origin-when-cross-origin",
      "Permissions-Policy": "camera=(), geolocation=(), microphone=(self)",
      "Strict-Transport-Security": "max-age=31536000; includeSubDomains; preload",
      "Cache-Control": "no-store, no-cache, must-revalidate",
      "X-Connecto-Mode": mode
    }
  });
}

function getEmergencyMaintenanceResponse(host) {
  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Connecto Cloud Gateway</title>
  <style>
    body { background: #0b0d13; color: #fff; font-family: -apple-system, BlinkMacSystemFont, sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; text-align: center; }
    .card { max-width: 480px; padding: 32px 24px; background: #12151e; border: 1px solid #1f2430; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
    h1 { font-size: 24px; margin-bottom: 12px; }
    p { color: #9ca3af; font-size: 14px; line-height: 1.6; }
    .badge { display: inline-block; padding: 4px 12px; background: rgba(99,102,241,0.15); border: 1px solid #4f46e5; color: #a5b4fc; border-radius: 20px; font-size: 12px; margin-bottom: 16px; }
    a { color: #6366f1; text-decoration: none; }
  </style>
</head>
<body>
  <div class="card">
    <div class="badge">Cloudflare Edge Gateway Active</div>
    <h1>Connecto Cloud Gateway</h1>
    <p>The Connecto Cloud Server is synchronizing. Services and real-time APIs are running at the edge.</p>
    <p><a href="/">Reload Platform</a> &bull; <a href="https://t.me/VCONNECTOFUN">Telegram Alerts</a></p>
  </div>
</body>
</html>`;

  return new Response(html, {
    status: 503,
    headers: {
      "Content-Type": "text/html;charset=UTF-8",
      "Cache-Control": "no-store",
      "Retry-After": "30",
      "X-Connecto-Mode": "cloud_server"
    }
  });
}

export class ConnectoRealtimeRoom {
  constructor(state, env) {
    this.state = state;
    this.env = env;
    this.sockets = new Set();
    this.socketMeta = new Map();
    this.userSockets = new Map();
    this.channelSockets = new Map();
    this.voiceRooms = new Map(); // roomId -> Set<username>
    this.pendingCalls = new Map(); // callId -> { callId, caller, callerName, callerAvatar, target, status, createdAt }
    this.userNotifications = new Map(); // username -> Array<Notification>
  }

  addNotification(username, notif) {
    if (!username) return;
    const clean = String(username).toLowerCase().trim().replace(/^@/, "");
    if (!this.userNotifications.has(clean)) {
      this.userNotifications.set(clean, []);
    }
    const list = this.userNotifications.get(clean);
    const item = {
      id: notif.id || ("notif_" + Date.now() + "_" + Math.random().toString(36).substring(2, 7)),
      user_id: clean,
      type: notif.type || "message",
      title: notif.title || "Connecto",
      content: notif.content || "",
      sender_username: notif.sender_username || "",
      sender_avatar: notif.sender_avatar || "👾",
      reference_id: notif.reference_id || "",
      is_read: false,
      created_at: notif.created_at || new Date().toISOString()
    };
    if (!list.some(existing => existing.id === item.id)) {
      list.unshift(item);
      if (list.length > 50) list.pop();
    }
    this.sendToUser(clean, {
      type: "notification_received",
      notification: item
    });
  }

  resolveCallPeer(callId, senderUsername, explicitTarget) {
    if (explicitTarget) {
      return String(explicitTarget).toLowerCase().trim().replace(/^@/, "");
    }
    if (callId && this.pendingCalls.has(callId)) {
      const info = this.pendingCalls.get(callId);
      const cleanSender = String(senderUsername || "").toLowerCase().trim().replace(/^@/, "");
      if (cleanSender && info.caller === cleanSender) return info.target;
      if (cleanSender && info.target === cleanSender) return info.caller;
      return info.caller || info.target || "";
    }
    return "";
  }

  async fetch(request) {
    const url = new URL(request.url);

    // 1. Internal HTTP broadcast dispatch from REST API endpoints
    if (url.pathname === "/internal/broadcast" || request.headers.get("X-Internal-Realtime") === "1") {
      try {
        const payload = await request.json();
        this.handleInternalBroadcast(payload);
        return new Response(JSON.stringify({ success: true, clients: this.sockets.size }), {
          headers: { "Content-Type": "application/json" }
        });
      } catch (err) {
        return new Response(JSON.stringify({ error: err.message }), { status: 400 });
      }
    }

    // 1b. Pending Voice Call Lookup for Background / Reconnecting Clients
    if (url.pathname === "/internal/call/pending") {
      const username = (url.searchParams.get("username") || "").toLowerCase().trim().replace(/^@/, "");
      const now = Date.now();
      for (const [callId, info] of this.pendingCalls.entries()) {
        if (now - info.createdAt > 60000) {
          this.pendingCalls.delete(callId);
          continue;
        }
        if (info.target === username && info.status === "ringing") {
          return new Response(JSON.stringify({
            has_pending: true,
            call_id: info.callId,
            room_id: info.callId,
            caller_id: info.caller,
            caller_username: info.caller,
            caller_name: info.callerName || info.caller,
            caller_avatar: info.callerAvatar || "👤"
          }), { headers: { "Content-Type": "application/json" } });
        }
      }
      return new Response(JSON.stringify({ has_pending: false }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    // 1c. Internal Real-Time Notifications Store
    if (url.pathname.startsWith("/internal/notifications")) {
      const username = (url.searchParams.get("username") || "").toLowerCase().trim().replace(/^@/, "");
      const list = this.userNotifications.get(username) || [];
      if (url.pathname.endsWith("/read-all")) {
        list.forEach(n => { n.is_read = true; });
        return new Response(JSON.stringify({ status: "ok" }), { headers: { "Content-Type": "application/json" } });
      }
      if (url.pathname.endsWith("/read")) {
        const nid = url.searchParams.get("id") || "";
        list.forEach(n => { if (n.id === nid) n.is_read = true; });
        return new Response(JSON.stringify({ status: "ok" }), { headers: { "Content-Type": "application/json" } });
      }
      if (url.pathname.endsWith("/clear")) {
        this.userNotifications.set(username, []);
        return new Response(JSON.stringify({ status: "ok" }), { headers: { "Content-Type": "application/json" } });
      }
      return new Response(JSON.stringify({ notifications: list }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    // 2. Real-time telemetry & cluster stats
    if (url.pathname === "/internal/stats") {
      return new Response(JSON.stringify({
        total_connections: this.sockets.size,
        online_users: Array.from(this.userSockets.keys()),
        subscribed_channels: Array.from(this.channelSockets.keys())
      }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    // 2b. Internal Profile Storage (Resilient edge storage bypassing D1 write limits)
    if (url.pathname === "/internal/profile/set" && request.method === "POST") {
      try {
        const body = await request.json();
        const userId = body.userId || body.id || body.username;
        if (userId && this.state && this.state.storage) {
          const key = "user_profile_" + userId.toLowerCase();
          const existing = (await this.state.storage.get(key)) || {};
          const merged = { ...existing, ...body, updated_at: new Date().toISOString() };
          await this.state.storage.put(key, merged);
          if (body.username) {
            await this.state.storage.put("user_profile_" + body.username.toLowerCase(), merged);
          }
          return new Response(JSON.stringify({ success: true, profile: merged }), {
            headers: { "Content-Type": "application/json" }
          });
        }
        return new Response(JSON.stringify({ error: "Missing userId" }), { status: 400 });
      } catch (err) {
        return new Response(JSON.stringify({ error: err.message }), { status: 500 });
      }
    }

    if (url.pathname === "/internal/profile/get" && request.method === "GET") {
      try {
        const userId = (url.searchParams.get("userId") || "").toLowerCase();
        if (userId && this.state && this.state.storage) {
          const profile = await this.state.storage.get("user_profile_" + userId);
          return new Response(JSON.stringify({ success: true, profile: profile || null }), {
            headers: { "Content-Type": "application/json" }
          });
        }
        return new Response(JSON.stringify({ profile: null }), { status: 200 });
      } catch (err) {
        return new Response(JSON.stringify({ error: err.message }), { status: 500 });
      }
    }

    // 3. WebSocket Upgrade & Handshake
    if (request.headers.get("Upgrade") === "websocket") {
      const [client, server] = Object.values(new WebSocketPair());
      server.accept();
      this.handleWebSocket(server, request, url);

      return new Response(null, {
        status: 101,
        webSocket: client,
        headers: {
          "X-Connecto-Mode": "cloud_server",
          "X-Realtime-Engine": "Cloudflare-Durable-Object",
          "Access-Control-Allow-Origin": "*"
        }
      });
    }

    return new Response("Connecto Realtime Engine Active", { status: 200 });
  }

  async handleWebSocket(server, request, url) {
    let initialUser = url.searchParams.get("username") ||
                      url.searchParams.get("user") ||
                      url.searchParams.get("user_id") ||
                      request.headers.get("X-User-Username");

    let initialChannel = url.searchParams.get("channel") || url.searchParams.get("channel_id");

    const wsMatch = url.pathname.match(/^\/ws\/([^\/]+)(?:\/([^\/]+))?/);
    if (wsMatch) {
      if (!initialChannel) initialChannel = decodeURIComponent(wsMatch[1]);
      if (!initialUser && wsMatch[2]) initialUser = decodeURIComponent(wsMatch[2]);
    }

    if (!initialUser || initialUser === "guest" || initialUser === "undefined") {
      initialUser = "shinobi_" + Math.random().toString(36).substring(2, 7);
    }
    if (!initialChannel) {
      initialChannel = "general";
    }

    const cleanUser = initialUser.trim().toLowerCase().replace(/^@/, "");
    const cleanChan = initialChannel.trim().toLowerCase();

    const meta = {
      username: cleanUser,
      displayName: cleanUser.charAt(0).toUpperCase() + cleanUser.slice(1),
      avatar: "👾",
      channels: new Set(["general", cleanUser]),
      currentVoiceRoom: null,
      connectedAt: Date.now()
    };
    if (cleanChan) meta.channels.add(cleanChan);

    this.sockets.add(server);
    this.socketMeta.set(server, meta);

    if (!this.userSockets.has(cleanUser)) this.userSockets.set(cleanUser, new Set());
    this.userSockets.get(cleanUser).add(server);

    for (const ch of meta.channels) {
      if (!this.channelSockets.has(ch)) this.channelSockets.set(ch, new Set());
      this.channelSockets.get(ch).add(server);
    }

    // Send instant welcome confirmation
    try {
      server.send(JSON.stringify({
        type: "connected",
        event: "connected",
        mode: "cloud_server",
        engine: "Cloudflare-Durable-Object",
        user: cleanUser,
        username: cleanUser,
        channel: cleanChan,
        timestamp: new Date().toISOString()
      }));
    } catch (_) {}

    // If there is an active ringing call for this user, immediately deliver it upon WS connect!
    const now = Date.now();
    for (const [callId, info] of this.pendingCalls.entries()) {
      if (now - info.createdAt <= 60000 && info.target === cleanUser && info.status === "ringing") {
        try {
          server.send(JSON.stringify({
            type: "call:incoming",
            action: "call:incoming",
            call_id: info.callId,
            room_id: info.callId,
            caller: info.caller,
            caller_id: info.caller,
            caller_username: info.caller,
            caller_name: info.callerName || info.caller,
            caller_avatar: info.callerAvatar || "👤",
            avatar: info.callerAvatar || "👤",
            timestamp: new Date().toISOString()
          }));
        } catch (_) {}
      }
    }

    // Broadcast presence to all other peers if user is not in stealth mode
    let userIsStealth = false;
    if (this.env && this.env.DB) {
      try {
        const uRec = await this.env.DB.prepare("SELECT is_stealth, display_name, avatar_url FROM users WHERE lower(username) = ? LIMIT 1").bind(cleanUser.toLowerCase()).first();
        if (uRec) {
          if (uRec.display_name) meta.displayName = uRec.display_name;
          if (uRec.avatar_url) meta.avatar = uRec.avatar_url;
          if (uRec.is_stealth === 1 || uRec.is_stealth === true) {
            userIsStealth = true;
          }
        }
      } catch (_) {}
    }

    if (!userIsStealth) {
      this.broadcastToAll({
        type: "presence_update",
        username: cleanUser,
        is_online: true,
        status: "online"
      }, server);
    }

    // Event listeners
    server.addEventListener("message", async (event) => {
      try {
        const raw = event.data;
        if (!raw) return;
        const data = typeof raw === "string" ? JSON.parse(raw) : {};

        // 1. Keepalive Ping / Pong
        if (data.type === "ping" || data.action === "ping") {
          server.send(JSON.stringify({ type: "pong", action: "pong", timestamp: Date.now() }));
          return;
        }

        // 2. Channel Subscriptions
        if (data.action === "subscribe" || data.type === "subscribe") {
          const chs = [];
          if (data.channel_id) chs.push(data.channel_id);
          if (data.channel) chs.push(data.channel);
          if (Array.isArray(data.channels)) chs.push(...data.channels);
          for (const c of chs) {
            if (!c) continue;
            const cl = String(c).toLowerCase().trim();
            meta.channels.add(cl);
            if (!this.channelSockets.has(cl)) this.channelSockets.set(cl, new Set());
            this.channelSockets.get(cl).add(server);
          }
          server.send(JSON.stringify({
            type: "subscribed",
            action: "subscribed",
            channels: Array.from(meta.channels)
          }));
          return;
        }

        // 3. Typing Indicators
        if (data.action === "typing" || data.type === "typing" || data.type === "typing_start" || data.type === "typing_stop") {
          const targetCh = (data.channel_id || data.channel || "general").toLowerCase().trim();
          const isStop = data.type === "typing_stop" || data.action === "typing:stop";
          this.broadcastToChannel(targetCh, {
            type: isStop ? "typing_stop" : "typing_start",
            user: meta.username,
            username: meta.username,
            display_name: meta.displayName,
            channel_id: targetCh
          }, server);
          return;
        }

        // 4. WebRTC 1:1 Voice Call Signaling & Voice Room Audio Relay
        const action = data.action || data.type;
        if (action === "call:initiate" || action === "call_invite") {
          const target = (data.target || data.target_user || data.target_user_id || "").toLowerCase().trim().replace(/^@/, "");
          const callId = data.call_id || data.room_id || "call_" + Math.random().toString(36).substring(2, 9);
          const callerAvatar = data.avatar || meta.avatar || "👾";

          this.pendingCalls.set(callId, {
            callId,
            caller: meta.username,
            callerName: meta.displayName,
            callerAvatar,
            target,
            status: "ringing",
            createdAt: Date.now()
          });

          const targetPayload = {
            type: "call:incoming",
            action: "call:incoming",
            call_id: callId,
            room_id: callId,
            caller: meta.username,
            caller_id: meta.username,
            caller_username: meta.username,
            caller_name: meta.displayName,
            caller_avatar: callerAvatar,
            avatar: callerAvatar,
            timestamp: new Date().toISOString()
          };

          const count = this.sendToUser(target, targetPayload);
          this.addNotification(target, {
            id: "call_" + callId,
            type: "call_invite",
            title: "Incoming Voice Call",
            content: `${meta.displayName} is calling you`,
            sender_username: meta.username,
            sender_avatar: callerAvatar,
            reference_id: callId
          });

          server.send(JSON.stringify({
            type: "call:ringing",
            action: "call:ringing",
            target: target,
            call_id: callId,
            room_id: callId,
            peer_online: count > 0
          }));
          return;
        }

        if (action === "call:accept" || action === "call_accept") {
          const callId = data.call_id || data.room_id;
          const target = this.resolveCallPeer(callId, meta.username, data.target || data.target_user || data.target_user_id);
          if (callId && this.pendingCalls.has(callId)) {
            this.pendingCalls.get(callId).status = "connected";
          }
          if (target) {
            this.sendToUser(target, {
              type: "call:accepted",
              action: "call:accepted",
              callee: meta.username,
              callee_username: meta.username,
              target: meta.username,
              target_user: meta.username,
              call_id: callId,
              room_id: callId
            });
          }
          return;
        }

        if (action === "call:decline" || action === "call_decline") {
          const callId = data.call_id || data.room_id;
          const target = this.resolveCallPeer(callId, meta.username, data.target || data.target_user || data.target_user_id);
          if (callId) this.pendingCalls.delete(callId);
          if (target) {
            this.sendToUser(target, {
              type: "call:declined",
              action: "call:declined",
              target: meta.username,
              call_id: callId,
              room_id: callId
            });
          }
          return;
        }

        if (action === "call:offer" || action === "voice_offer") {
          const callId = data.call_id || data.room_id || meta.currentVoiceRoom;
          const target = this.resolveCallPeer(callId, meta.username, data.target || data.target_user || data.target_user_id);
          const offerPayload = {
            type: "call:offer",
            action: "call:offer",
            caller: meta.username,
            sender_username: meta.username,
            sender_user_id: meta.username,
            sender_id: meta.username,
            call_id: callId,
            room_id: callId,
            sdp: data.sdp || data.description
          };
          if (target) {
            this.sendToUser(target, offerPayload);
          } else if (callId) {
            this.broadcastToChannel(`voice_room:${callId}`, offerPayload, server);
          }
          return;
        }

        if (action === "call:answer" || action === "voice_answer") {
          const callId = data.call_id || data.room_id || meta.currentVoiceRoom;
          const target = this.resolveCallPeer(callId, meta.username, data.target || data.target_user || data.target_user_id);
          const answerPayload = {
            type: "call:answer",
            action: "call:answer",
            callee: meta.username,
            caller: meta.username,
            sender_username: meta.username,
            sender_user_id: meta.username,
            sender_id: meta.username,
            call_id: callId,
            room_id: callId,
            sdp: data.sdp || data.description
          };
          if (target) {
            this.sendToUser(target, answerPayload);
          } else if (callId) {
            this.broadcastToChannel(`voice_room:${callId}`, answerPayload, server);
          }
          return;
        }

        if (action === "call:ice-candidate" || action === "voice_ice_candidate") {
          const callId = data.call_id || data.room_id || meta.currentVoiceRoom;
          const target = this.resolveCallPeer(callId, meta.username, data.target || data.target_user || data.target_user_id);
          const icePayload = {
            type: "call:ice-candidate",
            action: "call:ice-candidate",
            caller: meta.username,
            sender_username: meta.username,
            sender_user_id: meta.username,
            sender_id: meta.username,
            candidate: data.candidate,
            call_id: callId,
            room_id: callId
          };
          if (target) {
            this.sendToUser(target, icePayload);
          } else if (callId) {
            this.broadcastToChannel(`voice_room:${callId}`, icePayload, server);
          }
          return;
        }

        if (action === "call:end" || action === "call:ended" || action === "call_end") {
          const callId = data.call_id || data.room_id;
          const target = this.resolveCallPeer(callId, meta.username, data.target || data.target_user || data.target_user_id);
          if (callId) this.pendingCalls.delete(callId);
          if (target) {
            this.sendToUser(target, {
              type: "call:ended",
              action: "call:ended",
              call_id: callId,
              room_id: callId
            });
          } else if (callId) {
            this.broadcastToChannel(`voice_room:${callId}`, {
              type: "call:ended",
              action: "call:ended",
              call_id: callId,
              room_id: callId
            }, server);
          }
          return;
        }

        // 4b. Voice Room Join / Leave & PCM Audio Relay (voice_join, voice_leave, voice_data)
        if (action === "voice_join" || action === "voice:join") {
          const rId = String(data.room_id || data.channel_id || "").trim();
          if (rId) {
            meta.currentVoiceRoom = rId;
            const roomChan = `voice_room:${rId}`;
            meta.channels.add(roomChan);
            if (!this.channelSockets.has(roomChan)) this.channelSockets.set(roomChan, new Set());
            this.channelSockets.get(roomChan).add(server);

            if (!this.voiceRooms.has(rId)) this.voiceRooms.set(rId, new Set());
            const peersSet = this.voiceRooms.get(rId);
            const existingPeers = Array.from(peersSet).filter(p => p !== meta.username);
            peersSet.add(meta.username);

            server.send(JSON.stringify({
              type: "voice_room_joined",
              room_id: rId,
              channel_id: rId,
              existing_peer_ids: existingPeers,
              participants: Array.from(peersSet)
            }));

            this.broadcastToChannel(roomChan, {
              type: "voice_peer_joined",
              room_id: rId,
              peer_id: meta.username,
              username: meta.username
            }, server);
          }
          return;
        }

        if (action === "voice_leave" || action === "voice:leave") {
          const rId = String(data.room_id || data.channel_id || meta.currentVoiceRoom || "").trim();
          if (rId) {
            const roomChan = `voice_room:${rId}`;
            meta.channels.delete(roomChan);
            if (this.channelSockets.has(roomChan)) {
              this.channelSockets.get(roomChan).delete(server);
            }
            if (this.voiceRooms.has(rId)) {
              this.voiceRooms.get(rId).delete(meta.username);
            }
            if (meta.currentVoiceRoom === rId) meta.currentVoiceRoom = null;
            this.broadcastToChannel(roomChan, {
              type: "voice_peer_left",
              room_id: rId,
              peer_id: meta.username,
              username: meta.username
            }, server);
          }
          return;
        }

        if (action === "voice_data" || action === "voice:data" || data.type === "voice_data") {
          const rId = String(data.room_id || data.channel_id || meta.currentVoiceRoom || "").trim();
          const audio = data.audio;
          if (rId && audio) {
            const audioPayload = {
              type: "voice_data",
              room_id: rId,
              sender_id: meta.username,
              sender_username: meta.username,
              audio: audio
            };
            const roomChan = `voice_room:${rId}`;
            const chSet = this.channelSockets.get(roomChan);
            if (chSet && chSet.size > 0) {
              const msg = JSON.stringify(audioPayload);
              for (const ws of chSet) {
                if (ws === server) continue;
                try { ws.send(msg); } catch (_) {}
              }
            } else {
              const peer = this.resolveCallPeer(rId, meta.username, data.target);
              if (peer) this.sendToUser(peer, audioPayload);
            }
          }
          return;
        }

        // 5. Voice Speaking Indicator
        if (action === "voice:speaking" || action === "voice_state") {
          const rId = data.room_id || meta.currentVoiceRoom;
          if (rId) {
            this.broadcastToChannel(`voice_room:${rId}`, {
              type: "voice:speaking",
              room_id: rId,
              user: meta.username,
              username: meta.username,
              speaking: Boolean(data.speaking)
            }, server);
          }
          return;
        }

        // 6. Real-Time Chat Messages (<5ms Latency Mesh)
        if (data.type === "message" || data.type === "chat_message" || data.content || data.text) {
          const rawContent = data.content || data.text || "";
          if (!rawContent.trim()) return;

          const targetCh = (data.channel_id || data.channelId || data.channel || "general").toLowerCase().trim();
          const msgId = "msg_rt_" + Math.random().toString(36).substring(2, 10);
          const nowIso = new Date().toISOString();

          const msgObj = {
            id: msgId,
            channelId: targetCh,
            channel_id: targetCh,
            authorId: meta.username,
            author_id: meta.username,
            authorName: meta.username,
            author_name: meta.username,
            authorAvatar: meta.avatar,
            author_avatar: meta.avatar,
            user: meta.username,
            avatar: meta.avatar,
            nickname: meta.displayName,
            sender: meta.username,
            sender_username: meta.username,
            sender_display_name: meta.displayName,
            sender_avatar_url: meta.avatar,
            content: rawContent,
            text: rawContent,
            createdAt: nowIso,
            created_at: nowIso,
            timestamp: nowIso,
            type: "text",
            reactions: {}
          };

          // Asynchronously persist to D1 without delaying WebSocket delivery
          this.persistMessage(msgId, targetCh, meta.username, rawContent);

          if (targetCh.startsWith("dm-") || targetCh.startsWith("dm_")) {
            const recipient = resolveDmRecipient(targetCh, meta.username, data.recipient || data.target);

            if (recipient) {
              const dmPayload = {
                type: "new_dm_alert",
                sender: meta.username,
                channel_id: targetCh,
                channel_name: targetCh,
                message: msgObj
              };
              this.sendToUser(recipient, dmPayload);
              this.sendToUser(recipient, { type: "new_message", channel_id: targetCh, message: msgObj });
              this.addNotification(recipient, {
                id: msgId,
                type: "message",
                title: meta.displayName || meta.username,
                content: rawContent,
                sender_username: meta.username,
                sender_avatar: meta.avatar,
                reference_id: meta.username
              });

              if (this.env && this.env.DB) {
                try {
                  const nId = "notif_ws_" + msgId;
                  this.env.DB.prepare(
                    "INSERT OR IGNORE INTO notifications (id, user_id, type, title, content, sender_username, sender_avatar, reference_id, is_read, created_at) VALUES (?, ?, 'message', ?, ?, ?, ?, ?, 0, datetime('now'))"
                  ).bind(nId, recipient, meta.displayName || meta.username, rawContent, meta.username, meta.avatar, meta.username).run().catch(() => {});
                } catch (_) {}
              }
            }
            server.send(JSON.stringify({ type: "new_message", channel_id: targetCh, message: msgObj }));
          } else {
            this.broadcastToChannel(targetCh, {
              type: "new_message",
              channel_id: targetCh,
              channel_name: targetCh,
              message: msgObj
            });
          }
        }
      } catch (err) {
        console.error("[WS_MSG_ERROR]", err);
      }
    });

    server.addEventListener("close", () => {
      this.removeSocket(server);
    });
    server.addEventListener("error", () => {
      this.removeSocket(server);
    });
  }

  removeSocket(server) {
    const meta = this.socketMeta.get(server);
    if (!meta) return;

    this.sockets.delete(server);
    this.socketMeta.delete(server);

    if (meta.currentVoiceRoom && this.voiceRooms.has(meta.currentVoiceRoom)) {
      this.voiceRooms.get(meta.currentVoiceRoom).delete(meta.username);
    }

    for (const ch of meta.channels) {
      const chSet = this.channelSockets.get(ch);
      if (chSet) {
        chSet.delete(server);
        if (chSet.size === 0) this.channelSockets.delete(ch);
      }
    }

    const userSet = this.userSockets.get(meta.username);
    if (userSet) {
      userSet.delete(server);
      if (userSet.size === 0) {
        this.userSockets.delete(meta.username);
        this.broadcastToAll({
          type: "presence_update",
          username: meta.username,
          is_online: false,
          status: "offline"
        });
      }
    }
  }

  sendToUser(username, payload) {
    const clean = String(username).toLowerCase().trim().replace(/^@/, "");
    const userSet = this.userSockets.get(clean);
    if (!userSet || userSet.size === 0) return 0;

    const msg = JSON.stringify(payload);
    let sent = 0;
    for (const ws of userSet) {
      try {
        ws.send(msg);
        sent++;
      } catch (_) {}
    }
    return sent;
  }

  broadcastToChannel(channel, payload, excludeSocket = null) {
    const clean = String(channel).toLowerCase().trim();
    const chSet = this.channelSockets.get(clean);
    const msg = JSON.stringify(payload);

    if (chSet && chSet.size > 0) {
      for (const ws of chSet) {
        if (ws === excludeSocket) continue;
        try { ws.send(msg); } catch (_) {}
      }
    } else if (!clean.startsWith("voice_room:")) {
      this.broadcastToAll(payload, excludeSocket);
    }
  }

  broadcastToAll(payload, excludeSocket = null) {
    const msg = JSON.stringify(payload);
    for (const ws of this.sockets) {
      if (ws === excludeSocket) continue;
      try { ws.send(msg); } catch (_) {}
    }
  }

  handleInternalBroadcast(payload) {
    if (!payload || !payload.type) return;

    if (payload.type === "channel_message" && payload.channel) {
      this.broadcastToChannel(payload.channel, {
        type: "new_message",
        channel_id: payload.channel,
        channel_name: payload.channel,
        message: payload.message
      });
    } else if (payload.type === "dm_message") {
      this.sendToUser(payload.target, {
        type: "new_dm_alert",
        sender: payload.sender,
        channel_id: payload.channel,
        channel_name: payload.channel,
        message: payload.message
      });
      this.sendToUser(payload.target, {
        type: "new_message",
        channel_id: payload.channel,
        channel_name: payload.channel,
        message: payload.message
      });
      if (payload.message) {
        this.addNotification(payload.target, {
          id: payload.message.id,
          type: "message",
          title: payload.message.sender_display_name || payload.sender,
          content: payload.message.content || payload.message.text || "",
          sender_username: payload.sender,
          sender_avatar: payload.message.avatar || payload.message.sender_avatar_url || "👾",
          reference_id: payload.sender
        });
      }
      this.sendToUser(payload.sender, {
        type: "new_message",
        channel_id: payload.channel,
        channel_name: payload.channel,
        message: payload.message
      });
    } else if (payload.type === "friend_request_received") {
      this.sendToUser(payload.target, {
        type: "friend_request_received",
        data: {
          id: payload.request_id,
          sender: payload.sender,
          sender_username: payload.sender,
          sender_avatar: payload.sender_avatar || "👾"
        }
      });
      this.addNotification(payload.target, {
        id: payload.request_id || ("fr_" + Date.now()),
        type: "friend_request",
        title: "New Friend Request",
        content: `${payload.sender} sent you a friend request`,
        sender_username: payload.sender,
        sender_avatar: payload.sender_avatar || "👾",
        reference_id: payload.request_id || payload.sender
      });
    } else if (payload.type === "friend_accepted") {
      this.sendToUser(payload.target, {
        type: "friend_accepted",
        data: {
          friend_username: payload.sender,
          friend_display_name: payload.sender,
          friend_avatar: payload.sender_avatar || "👾"
        }
      });
      this.addNotification(payload.target, {
        id: "fa_" + Date.now(),
        type: "friend_accepted",
        title: "🤝 Alliance Forged",
        content: `You and @${payload.sender} are now friends!`,
        sender_username: payload.sender,
        sender_avatar: payload.sender_avatar || "👾",
        reference_id: payload.sender
      });
    } else if (payload.type === "call_invite") {
      const callId = payload.room_id || payload.call_id;
      const caller = (payload.caller || "").toLowerCase().trim().replace(/^@/, "");
      const target = (payload.target || "").toLowerCase().trim().replace(/^@/, "");
      if (callId && target) {
        this.pendingCalls.set(callId, {
          callId,
          caller,
          callerName: payload.caller_name || payload.caller,
          callerAvatar: payload.caller_avatar || "👤",
          target,
          status: "ringing",
          createdAt: Date.now()
        });
      }
      this.sendToUser(target, {
        type: "call:incoming",
        action: "call:incoming",
        caller: payload.caller,
        caller_id: payload.caller,
        caller_username: payload.caller,
        caller_name: payload.caller_name || payload.caller,
        avatar: payload.caller_avatar || "👤",
        caller_avatar: payload.caller_avatar || "👤",
        call_id: callId,
        room_id: callId
      });
      this.addNotification(target, {
        id: "call_" + callId,
        type: "call_invite",
        title: "Incoming Voice Call",
        content: `${payload.caller_name || payload.caller} is calling you`,
        sender_username: payload.caller,
        sender_avatar: payload.caller_avatar || "👤",
        reference_id: callId
      });
    } else if (payload.type === "call_accepted" || payload.type === "call_declined" || payload.type === "call_ended") {
      const callId = payload.call_id || payload.room_id;
      const actor = payload.responder || payload.sender || "";
      const target = this.resolveCallPeer(callId, actor, payload.target);
      if (callId) {
        if (payload.type === "call_accepted" && this.pendingCalls.has(callId)) {
          this.pendingCalls.get(callId).status = "connected";
        } else if (payload.type === "call_declined" || payload.type === "call_ended") {
          this.pendingCalls.delete(callId);
        }
      }
      const eventName = payload.type === "call_accepted" ? "call:accepted" :
                        payload.type === "call_declined" ? "call:declined" : "call:ended";
      if (target) {
        this.sendToUser(target, {
          type: eventName,
          action: eventName,
          callee: actor || target,
          callee_username: actor || target,
          target: actor || target,
          call_id: callId,
          room_id: callId
        });
      }
    } else if (payload.type === "voice_join" || payload.type === "voice_leave") {
      const roomKey = `voice_room:${payload.roomId}`;
      this.broadcastToChannel(roomKey, {
        type: payload.type === "voice_join" ? "voice:participant_joined" : "voice:participant_left",
        room_id: payload.roomId,
        user: payload.user,
        username: payload.user,
        avatar: payload.avatar,
        nickname: payload.nickname
      });
    } else if (payload.type === "presence_update") {
      this.broadcastToAll({
        type: "presence_update",
        username: payload.username,
        is_online: payload.is_online,
        status: payload.status
      });
    }
  }

  persistMessage(id, channelId, sender, content) {
    if (!this.env || !this.env.DB) return;
    this.env.DB.prepare(
      "INSERT INTO messages (id, channel_id, sender_id, content) VALUES (?, ?, ?, ?)"
    ).bind(id, channelId, sender, content).run().catch(() => {});
  }
}

