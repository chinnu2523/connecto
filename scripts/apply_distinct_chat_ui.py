#!/usr/bin/env python3
import re
import sys

INDEX_PATH = "/Users/madarauchiha/connecto/website/static/index.html"
APP_PATH = "/Users/madarauchiha/connecto/backend/app/index.html"
BACKEND_STATIC_PATH = "/Users/madarauchiha/connecto/backend/static/index.html"

with open(INDEX_PATH, "r", encoding="utf-8") as f:
    content = f.read()

# 1. CSS Injection
css_to_add = '''
/* ==================== DISTINCT MESSAGE UI: GENERAL CHAT vs PERSONAL CHAT ==================== */

/* 1. GENERAL CHAT (PUBLIC CHANNELS) STREAM & CARDS */
.messages-area.channel-mode-active {
  padding: 16px 20px 24px 20px !important;
  gap: 12px !important;
}

.channel-message-card {
  position: relative;
  width: 100% !important;
  border-radius: 16px !important;
  padding: 14px 18px !important;
  margin: 4px 0 !important;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1) !important;
  box-sizing: border-box;
  display: block !important;
}

/* General Channel Message Card - Noir (Dark) */
html[data-theme="noir"] .channel-message-card {
  background: rgba(16, 18, 27, 0.88) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.35) !important;
}
html[data-theme="noir"] .channel-message-card:hover {
  background: rgba(22, 25, 38, 0.96) !important;
  border-color: rgba(255, 255, 255, 0.18) !important;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.5) !important;
}
html[data-theme="noir"] .channel-message-card.mine {
  background: rgba(18, 22, 34, 0.95) !important;
  border: 1px solid rgba(0, 206, 201, 0.3) !important;
  box-shadow: 0 4px 20px rgba(0, 206, 201, 0.08) !important;
}
html[data-theme="noir"] .channel-message-card.mine:hover {
  border-color: rgba(0, 206, 201, 0.5) !important;
}
html[data-theme="noir"] .channel-message-card .msg-card-author {
  color: #ffffff !important;
  font-weight: 700 !important;
  font-size: 14px !important;
}
html[data-theme="noir"] .channel-message-card .msg-card-handle {
  color: #71717a !important;
  font-size: 12px !important;
}
html[data-theme="noir"] .channel-message-card .msg-card-time {
  color: #71717a !important;
  font-size: 11.5px !important;
}
html[data-theme="noir"] .channel-message-card .msg-card-body {
  color: #e4e4e7 !important;
  font-size: 13.5px !important;
  line-height: 1.55 !important;
}

/* General Channel Message Card - Parchment (Light) */
html[data-theme="parchment"] .channel-message-card {
  background: #ffffff !important;
  border: 1px solid rgba(68, 64, 60, 0.14) !important;
  box-shadow: 0 2px 8px rgba(68, 64, 60, 0.04) !important;
}
html[data-theme="parchment"] .channel-message-card:hover {
  background: #ffffff !important;
  border-color: rgba(68, 64, 60, 0.28) !important;
  box-shadow: 0 4px 16px rgba(68, 64, 60, 0.08) !important;
}
html[data-theme="parchment"] .channel-message-card.mine {
  background: #faf8f5 !important;
  border: 1px solid rgba(79, 70, 229, 0.28) !important;
  box-shadow: 0 4px 16px rgba(79, 70, 229, 0.06) !important;
}
html[data-theme="parchment"] .channel-message-card .msg-card-author {
  color: #141312 !important;
  font-weight: 700 !important;
  font-size: 14px !important;
}
html[data-theme="parchment"] .channel-message-card .msg-card-handle {
  color: #78716c !important;
  font-size: 12px !important;
}
html[data-theme="parchment"] .channel-message-card .msg-card-time {
  color: #a8a29e !important;
  font-size: 11.5px !important;
}
html[data-theme="parchment"] .channel-message-card .msg-card-body {
  color: #292524 !important;
  font-size: 13.5px !important;
  line-height: 1.55 !important;
}


/* 2. PERSONAL CHAT (DIRECT MESSAGES) BUBBLE UI */
.messages-area.dm-mode-active {
  padding: 16px 20px 24px 20px !important;
  gap: 8px !important;
  display: flex !important;
  flex-direction: column !important;
}

/* Personal DM Message Row */
.personal-dm-row {
  position: relative;
  display: flex;
  width: 100%;
  margin: 3px 0;
  box-sizing: border-box;
}

/* Outgoing Messages (Sent by Me) -> Align Right */
.personal-dm-row.dm-row-mine {
  justify-content: flex-end;
}
.dm-row-mine .personal-dm-bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  max-width: 74%;
  position: relative;
}

/* Incoming Messages (Received from Friend) -> Align Left */
.personal-dm-row.dm-row-theirs {
  justify-content: flex-start;
  align-items: flex-end;
  gap: 10px;
}
.dm-row-theirs .dm-bubble-avatar-col {
  flex-shrink: 0;
  margin-bottom: 4px;
  cursor: pointer;
}
.dm-row-theirs .personal-dm-bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 74%;
  position: relative;
}

/* Personal DM Bubble Base */
.personal-dm-bubble {
  position: relative;
  padding: 10px 16px 8px 16px;
  font-size: 13.5px;
  line-height: 1.5;
  word-break: break-word;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  overflow: visible;
}

/* Outgoing Bubble (Mine) - Asymmetric tail at bottom-right */
.personal-dm-bubble.dm-bubble-mine {
  border-radius: 18px 18px 4px 18px !important;
}

/* Incoming Bubble (Theirs) - Asymmetric tail at bottom-left */
.personal-dm-bubble.dm-bubble-theirs {
  border-radius: 18px 18px 18px 4px !important;
}

/* Sender Name Header in Incoming Bubble */
.dm-sender-header {
  margin-bottom: 3px;
  line-height: 1.2;
}
.dm-sender-name {
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
}

/* Bubble Body & Text */
.dm-bubble-body {
  position: relative;
}

/* Bubble Footer (Timestamp & Status) */
.dm-bubble-footer {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 10.5px;
  line-height: 1;
}
.dm-bubble-mine .dm-bubble-footer {
  justify-content: flex-end;
}
.dm-bubble-theirs .dm-bubble-footer {
  justify-content: flex-start;
}
.dm-bubble-status {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: -1px;
}

/* Quoted Reply inside DM Bubble */
.personal-dm-bubble .msg-quoted-reply {
  padding: 5px 10px;
  border-radius: 8px;
  margin-bottom: 6px;
  font-size: 11.5px;
}

/* Reactions inside / below DM Bubble */
.personal-dm-bubble .msg-reactions-row {
  margin-top: 6px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.personal-dm-bubble .msg-rx-chip {
  padding: 2px 7px;
  border-radius: 12px;
  font-size: 11px;
  cursor: pointer;
  transition: 0.15s;
}

/* Floating Hover Actions for Personal DM Bubbles */
.dm-bubble-hover-actions {
  position: absolute;
  top: -16px;
  display: none;
  align-items: center;
  gap: 2px;
  padding: 3px 6px;
  border-radius: 18px;
  z-index: 10;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
  animation: framerFadeIn 0.15s ease-out;
}
.dm-actions-mine {
  right: 6px;
}
.dm-actions-theirs {
  left: 6px;
}
.personal-dm-bubble-wrap:hover .dm-bubble-hover-actions {
  display: flex;
}
.dm-act-icon {
  background: none;
  border: none;
  font-size: 13px;
  padding: 3px 5px;
  cursor: pointer;
  border-radius: 6px;
  line-height: 1;
  transition: transform 0.12s, background 0.12s;
}
.dm-act-icon:hover {
  transform: scale(1.18);
}


/* ==================== DUAL THEME STYLES FOR PERSONAL DM BUBBLES ==================== */

/* --- NOIR (Pure Black OLED) --- */
/* Outgoing DM Bubble - Cyber Gradient with Crisp Border & Glow */
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-mine {
  background: linear-gradient(135deg, #0284c7 0%, #4f46e5 100%) !important;
  border: 1px solid rgba(56, 189, 248, 0.45) !important;
  color: #ffffff !important;
  box-shadow: 0 4px 18px rgba(2, 132, 199, 0.28), 0 1px 3px rgba(0, 0, 0, 0.4) !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-mine .dm-bubble-time {
  color: rgba(255, 255, 255, 0.82) !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-mine .dm-bubble-status {
  color: #7dd3fc !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-mine .msg-quoted-reply {
  background: rgba(0, 0, 0, 0.25) !important;
  border-left: 3px solid #38bdf8 !important;
  color: #f1f5f9 !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-mine .msg-rx-chip {
  background: rgba(0, 0, 0, 0.3) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #ffffff !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-mine .msg-rx-chip.active {
  background: rgba(255, 255, 255, 0.25) !important;
  border-color: #ffffff !important;
}

/* Incoming DM Bubble - Obsidian Glass with Muted Highlights */
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-theirs {
  background: linear-gradient(135deg, #181a26 0%, #12131d 100%) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #e4e4e7 !important;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.45) !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-theirs .dm-sender-name {
  color: #38bdf8 !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-theirs .dm-bubble-time {
  color: #71717a !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-theirs .msg-quoted-reply {
  background: rgba(255, 255, 255, 0.05) !important;
  border-left: 3px solid #6366f1 !important;
  color: #cbd5e1 !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-theirs .msg-rx-chip {
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #e4e4e7 !important;
}
html[data-theme="noir"] .personal-dm-bubble.dm-bubble-theirs .msg-rx-chip.active {
  background: rgba(56, 189, 248, 0.18) !important;
  border-color: #38bdf8 !important;
}

/* Hover Action Pill - Noir */
html[data-theme="noir"] .dm-bubble-hover-actions {
  background: rgba(14, 16, 25, 0.96) !important;
  border: 1px solid rgba(255, 255, 255, 0.14) !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6) !important;
}
html[data-theme="noir"] .dm-act-icon {
  color: #e4e4e7 !important;
}
html[data-theme="noir"] .dm-act-icon:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}


/* --- PARCHMENT (Ivory Paper) --- */
/* Outgoing DM Bubble - Imperial Indigo */
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-mine {
  background: linear-gradient(135deg, #4338ca 0%, #6366f1 100%) !important;
  border: 1px solid #3730a3 !important;
  color: #ffffff !important;
  box-shadow: 0 4px 16px rgba(67, 56, 202, 0.25), 0 1px 3px rgba(0, 0, 0, 0.08) !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-mine .dm-bubble-time {
  color: rgba(255, 255, 255, 0.85) !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-mine .dm-bubble-status {
  color: #a5f3fc !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-mine .msg-quoted-reply {
  background: rgba(0, 0, 0, 0.2) !important;
  border-left: 3px solid #ffffff !important;
  color: #f8fafc !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-mine .msg-rx-chip {
  background: rgba(0, 0, 0, 0.25) !important;
  border: 1px solid rgba(255, 255, 255, 0.25) !important;
  color: #ffffff !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-mine .msg-rx-chip.active {
  background: rgba(255, 255, 255, 0.3) !important;
  border-color: #ffffff !important;
}

/* Incoming DM Bubble - Crisp White Porcelain */
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-theirs {
  background: #ffffff !important;
  border: 1px solid rgba(68, 64, 60, 0.16) !important;
  color: #141312 !important;
  box-shadow: 0 2px 10px rgba(68, 64, 60, 0.06), 0 1px 2px rgba(68, 64, 60, 0.04) !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-theirs .dm-sender-name {
  color: #4338ca !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-theirs .dm-bubble-time {
  color: #78716c !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-theirs .msg-quoted-reply {
  background: #f5f1e8 !important;
  border-left: 3px solid #6366f1 !important;
  color: #44403c !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-theirs .msg-rx-chip {
  background: #f5f1e8 !important;
  border: 1px solid rgba(68, 64, 60, 0.16) !important;
  color: #141312 !important;
}
html[data-theme="parchment"] .personal-dm-bubble.dm-bubble-theirs .msg-rx-chip.active {
  background: rgba(99, 102, 241, 0.14) !important;
  border-color: #6366f1 !important;
}

/* Hover Action Pill - Parchment */
html[data-theme="parchment"] .dm-bubble-hover-actions {
  background: #ffffff !important;
  border: 1px solid rgba(68, 64, 60, 0.2) !important;
  box-shadow: 0 4px 16px rgba(68, 64, 60, 0.12) !important;
}
html[data-theme="parchment"] .dm-act-icon {
  color: #141312 !important;
}
html[data-theme="parchment"] .dm-act-icon:hover {
  background: #f5f1e8 !important;
}
'''

# Place CSS right before </head>
if '</head>' in content:
    content = content.replace('</head>', f'{css_to_add}\n</head>', 1)
    print("CSS successfully appended before </head>")
else:
    print("Error: </head> not found", file=sys.stderr)
    sys.exit(1)

# 2. Add container class toggles in _doRenderMessages:
old_render_init = '''    function _doRenderMessages() {
      const container = document.getElementById('messagesArea');
      if (!container) return;'''

new_render_init = '''    function _doRenderMessages() {
      const container = document.getElementById('messagesArea');
      if (!container) return;
      if (currentMode === 'dm') {
        container.classList.remove('channel-mode-active');
        container.classList.add('dm-mode-active');
      } else {
        container.classList.remove('dm-mode-active');
        container.classList.add('channel-mode-active');
      }'''

if old_render_init in content:
    content = content.replace(old_render_init, new_render_init, 1)
    print("Render init container class toggle added")
else:
    print("Error: old_render_init not found", file=sys.stderr)
    sys.exit(1)

# 3. Replace card rendering inside allMsgs.forEach:
old_card_block = '''          const card = document.createElement('div');
          card.className = `connecto-message-card ${isMine ? 'mine' : ''} ${msg.is_new_incoming ? 'incoming-spring' : ''}`;
          card.id = `msg-${msg.id}`;
          card.innerHTML = `
            <div class="msg-card-header">
              <div class="msg-card-user-info" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">
                <div class="msg-card-avatar">${renderAvatarHTML(msg.avatar, displayName, 36)}</div>
                <div class="msg-card-names">
                  <span class="msg-card-author">${escapeHTML(displayName)}</span>
                  ${showHandle ? `<span class="msg-card-handle">@${escapeHTML(msg.user)}</span>` : ''}
                  ${roleBadgeHTML}
                </div>
              </div>
              <div class="msg-card-time">${escapeHTML(formattedTime)}</div>
            </div>
            ${replyHTML}
            <div class="msg-card-body">
              ${pollHTML ? pollHTML : `<div id="msg-text-${msg.id}">${formattedContent}${editedHTML}${automodBadgeHTML}${scheduledPillHTML}${msg.expires_at ? `<span class="ephemeral-timer-pill" data-expires="${msg.expires_at}" style="display:inline-flex; align-items:center; gap:4px; font-size:10px; font-weight:800; color:#f59e0b; background:rgba(245,158,11,0.14); border:1px solid rgba(245,158,11,0.3); border-radius:6px; padding:1px 6px; margin-left:6px;"><i data-lucide="timer" style="width:11px; height:11px;"></i> <span class="countdown-val">...</span></span>` : ""}</div>`}
              <div id="msg-translation-${msg.id}" class="msg-inline-translation" style="display:none;"></div>
              ${linkPreviewHTML}
              ${attachmentHTML}
            </div>
            ${reactionsHTML}
            ${threadPillHTML}
            <div class="msg-hover-actions">
              <button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '🔥')" title="React 🔥">🔥</button>
              <button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '⚡')" title="React ⚡">⚡</button>
              <button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '👍')" title="React 👍">👍</button>
              <button class="msg-act-icon" onclick="openThreadDrawer('${msg.id}', '${encodeURIComponent(msg.channel_id || currentChannel)}')" title="Reply in Thread">🧵</button>
              <button class="msg-act-icon" onclick="initiateReply('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 40))}')" title="Quote Reply">💬</button>
              <button class="msg-act-icon" onclick="translateMessageInline('${msg.id}')" title="Translate Message">🌐</button>
              <button class="msg-act-icon ${isMessageBookmarked(msg.id) ? 'bookmarked' : ''}" onclick="toggleBookmarkMessage('${msg.id}')" title="${isMessageBookmarked(msg.id) ? 'Remove Bookmark' : 'Save Bookmark'}">🔖</button>
              <button class="msg-act-icon" onclick="copyMessageText('${msg.id}')" title="Copy Text">📋</button>
              <button class="msg-act-icon" onclick="openReportMessageModal('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 45))}')" title="Report Message">🚩</button>
              ${isOwner ? `<button class="msg-act-icon" onclick="promptEditMessage('${msg.id}')" title="Edit">✏️</button>` : ''}
              <button class="msg-act-icon" onclick="togglePinMessage('${msg.id}')" title="${msg.pinned ? 'Unpin' : 'Pin'}">📌</button>
              ${isOwner ? `<button class="msg-act-icon" onclick="deleteMessage('${msg.id}')" title="Delete">🗑️</button>` : ''}
            </div>
          `;'''

new_card_block = '''          const card = document.createElement('div');
          card.id = `msg-${msg.id}`;

          if (currentMode === 'dm') {
            // ==================== PERSONAL CHAT (DIRECT MESSAGE) BUBBLE UI ====================
            if (isMine) {
              // Outgoing message sent by currentUser -> Right-aligned Cyber/Indigo Bubble
              card.className = `personal-dm-row dm-row-mine ${msg.is_new_incoming ? 'incoming-spring' : ''}`;
              card.innerHTML = `
                <div class="personal-dm-bubble-wrap">
                  <div class="personal-dm-bubble dm-bubble-mine">
                    ${replyHTML}
                    <div class="dm-bubble-body">
                      ${pollHTML ? pollHTML : `<div id="msg-text-${msg.id}">${formattedContent}${editedHTML}${scheduledPillHTML}</div>`}
                      <div id="msg-translation-${msg.id}" class="msg-inline-translation" style="display:none;"></div>
                      ${linkPreviewHTML}
                      ${attachmentHTML}
                    </div>
                    <div class="dm-bubble-footer">
                      <span class="dm-bubble-time">${escapeHTML(formattedTime)}</span>
                      <span class="dm-bubble-status" title="Delivered & Read">✓✓</span>
                    </div>
                    ${reactionsHTML}
                  </div>
                  <div class="dm-bubble-hover-actions dm-actions-mine">
                    <button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '❤️')" title="Love ❤️">❤️</button>
                    <button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '🔥')" title="Fire 🔥">🔥</button>
                    <button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '👍')" title="Thumbs Up 👍">👍</button>
                    <button class="dm-act-icon" onclick="initiateReply('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 40))}')" title="Reply">💬</button>
                    <button class="dm-act-icon" onclick="copyMessageText('${msg.id}')" title="Copy">📋</button>
                    ${isOwner ? `<button class="dm-act-icon" onclick="promptEditMessage('${msg.id}')" title="Edit">✏️</button>` : ''}
                    ${isOwner ? `<button class="dm-act-icon" onclick="deleteMessage('${msg.id}')" title="Delete">🗑️</button>` : ''}
                  </div>
                </div>
              `;
            } else {
              // Incoming message from DM Partner -> Left-aligned Bubble with Avatar Beside It
              card.className = `personal-dm-row dm-row-theirs ${msg.is_new_incoming ? 'incoming-spring' : ''}`;
              card.innerHTML = `
                <div class="dm-bubble-avatar-col" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">
                  ${renderAvatarHTML(msg.avatar, displayName, 32)}
                </div>
                <div class="personal-dm-bubble-wrap">
                  <div class="personal-dm-bubble dm-bubble-theirs">
                    <div class="dm-sender-header">
                      <span class="dm-sender-name" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">${escapeHTML(displayName)}</span>
                    </div>
                    ${replyHTML}
                    <div class="dm-bubble-body">
                      ${pollHTML ? pollHTML : `<div id="msg-text-${msg.id}">${formattedContent}${editedHTML}</div>`}
                      <div id="msg-translation-${msg.id}" class="msg-inline-translation" style="display:none;"></div>
                      ${linkPreviewHTML}
                      ${attachmentHTML}
                    </div>
                    <div class="dm-bubble-footer">
                      <span class="dm-bubble-time">${escapeHTML(formattedTime)}</span>
                    </div>
                    ${reactionsHTML}
                  </div>
                  <div class="dm-bubble-hover-actions dm-actions-theirs">
                    <button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '❤️')" title="Love ❤️">❤️</button>
                    <button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '🔥')" title="Fire 🔥">🔥</button>
                    <button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '👍')" title="Thumbs Up 👍">👍</button>
                    <button class="dm-act-icon" onclick="initiateReply('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 40))}')" title="Reply">💬</button>
                    <button class="dm-act-icon" onclick="translateMessageInline('${msg.id}')" title="Translate">🌐</button>
                    <button class="dm-act-icon ${isMessageBookmarked(msg.id) ? 'bookmarked' : ''}" onclick="toggleBookmarkMessage('${msg.id}')" title="Bookmark">🔖</button>
                    <button class="dm-act-icon" onclick="copyMessageText('${msg.id}')" title="Copy">📋</button>
                    <button class="dm-act-icon" onclick="openReportMessageModal('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 45))}')" title="Report">🚩</button>
                  </div>
                </div>
              `;
            }
          } else {
            // ==================== GENERAL CHAT (CHANNELS) COMMUNITY CARD UI ====================
            card.className = `connecto-message-card channel-message-card ${isMine ? 'mine' : ''} ${msg.is_new_incoming ? 'incoming-spring' : ''}`;
            card.innerHTML = `
              <div class="msg-card-header">
                <div class="msg-card-user-info" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">
                  <div class="msg-card-avatar">${renderAvatarHTML(msg.avatar, displayName, 38)}</div>
                  <div class="msg-card-names">
                    <span class="msg-card-author">${escapeHTML(displayName)}</span>
                    ${showHandle ? `<span class="msg-card-handle">@${escapeHTML(msg.user)}</span>` : ''}
                    ${roleBadgeHTML}
                  </div>
                </div>
                <div class="msg-card-time" title="${escapeHTML(msg.timestamp || msg.created_at || '')}">${escapeHTML(formattedTime)}</div>
              </div>
              ${replyHTML}
              <div class="msg-card-body">
                ${pollHTML ? pollHTML : `<div id="msg-text-${msg.id}">${formattedContent}${editedHTML}${automodBadgeHTML}${scheduledPillHTML}${msg.expires_at ? `<span class="ephemeral-timer-pill" data-expires="${msg.expires_at}" style="display:inline-flex; align-items:center; gap:4px; font-size:10px; font-weight:800; color:#f59e0b; background:rgba(245,158,11,0.14); border:1px solid rgba(245,158,11,0.3); border-radius:6px; padding:1px 6px; margin-left:6px;"><i data-lucide="timer" style="width:11px; height:11px;"></i> <span class="countdown-val">...</span></span>` : ""}</div>`}
                <div id="msg-translation-${msg.id}" class="msg-inline-translation" style="display:none;"></div>
                ${linkPreviewHTML}
                ${attachmentHTML}
              </div>
              ${reactionsHTML}
              ${threadPillHTML}
              <div class="msg-hover-actions">
                <button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '🔥')" title="React 🔥">🔥</button>
                <button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '⚡')" title="React ⚡">⚡</button>
                <button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '👍')" title="React 👍">👍</button>
                <button class="msg-act-icon" onclick="openThreadDrawer('${msg.id}', '${encodeURIComponent(msg.channel_id || currentChannel)}')" title="Reply in Thread">🧵</button>
                <button class="msg-act-icon" onclick="initiateReply('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 40))}')" title="Quote Reply">💬</button>
                <button class="msg-act-icon" onclick="translateMessageInline('${msg.id}')" title="Translate Message">🌐</button>
                <button class="msg-act-icon ${isMessageBookmarked(msg.id) ? 'bookmarked' : ''}" onclick="toggleBookmarkMessage('${msg.id}')" title="${isMessageBookmarked(msg.id) ? 'Remove Bookmark' : 'Save Bookmark'}">🔖</button>
                <button class="msg-act-icon" onclick="copyMessageText('${msg.id}')" title="Copy Text">📋</button>
                <button class="msg-act-icon" onclick="openReportMessageModal('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 45))}')" title="Report Message">🚩</button>
                ${isOwner ? `<button class="msg-act-icon" onclick="promptEditMessage('${msg.id}')" title="Edit">✏️</button>` : ''}
                <button class="msg-act-icon" onclick="togglePinMessage('${msg.id}')" title="${msg.pinned ? 'Unpin' : 'Pin'}">📌</button>
                ${isOwner ? `<button class="msg-act-icon" onclick="deleteMessage('${msg.id}')" title="Delete">🗑️</button>` : ''}
              </div>
            `;
          }'''

if old_card_block in content:
    content = content.replace(old_card_block, new_card_block, 1)
    print("Card block successfully upgraded with distinct Channel vs DM layouts")
else:
    print("Error: old_card_block not found", file=sys.stderr)
    sys.exit(1)

# Write to all 3 paths
for path in [INDEX_PATH, APP_PATH, BACKEND_STATIC_PATH]:
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Updated {path}")

print("All index.html files updated successfully!")
