#!/usr/bin/env python3
"""
Patch Chat UI Security Hardening
Replaces dangerous inline string interpolations in event handler attributes
with safe data-* attributes and delegation handlers.
Updates both website/static/index.html and backend/app/index.html.
"""
import sys

FILES = [
    '/Users/madarauchiha/connecto/website/static/index.html',
    '/Users/madarauchiha/connecto/backend/app/index.html'
]

NEW_HELPER_FUNCTIONS = """
    // ==================== SECURE EVENT DELEGATION HELPERS ====================
    function _getMsgFromBtn(el) {
      if (!el) return null;
      const card = el.closest('[data-msg-id]') || el.closest('.personal-dm-row, .connecto-message-card');
      const msgId = el.getAttribute('data-msg-id') || (card ? card.getAttribute('data-msg-id') : null);
      if (!msgId) return null;
      return messageStore.get(msgId) || { id: msgId };
    }

    function handleReplyActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg) initiateReply(msg.id, msg.user || '', (msg.content || '').slice(0, 40));
    }

    function handleReportActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg) openReportMessageModal(msg.id, msg.user || '', (msg.content || '').slice(0, 45));
    }

    function handleReactionBtnClick(el) {
      const msg = _getMsgFromBtn(el);
      const emoji = el.getAttribute('data-emoji') || el.innerText.trim() || '👍';
      if (msg && msg.id) handleMessageReaction(msg.id, emoji);
    }

    function copyMessageActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg && msg.id) copyMessageText(msg.id);
    }

    function translateMessageActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg && msg.id) translateMessageInline(msg.id);
    }

    function openThreadActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg && msg.id) {
        const card = el.closest('[data-msg-id]');
        const ch = (card ? card.getAttribute('data-channel-id') : '') || msg.channel_id || currentChannel;
        openThreadDrawer(msg.id, encodeURIComponent(ch));
      }
    }

    function promptEditActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg && msg.id) promptEditMessage(msg.id);
    }

    function togglePinActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg && msg.id) togglePinMessage(msg.id);
    }

    function deleteMessageActionBtn(el) {
      const msg = _getMsgFromBtn(el);
      if (msg && msg.id) deleteMessage(msg.id);
    }

    function handleMemberClickFromElement(el, event) {
      const uname = el.getAttribute('data-username') || el.closest('[data-username]')?.getAttribute('data-username');
      if (uname) handleMemberClick(uname, event);
    }

    function handleAttachmentImgClick(el) {
      const url = el.getAttribute('data-url');
      if (url && (url.startsWith('https://') || url.startsWith('http://') || url.startsWith('/uploads/') || url.startsWith('data:image/'))) {
        window.open(url, '_blank', 'noopener,noreferrer');
      }
    }

    function handleUnfriendCurrentDMTarget() {
      if (currentDMTarget) confirmUnfriendUser(currentDMTarget);
    }

    function handleAddFriendCurrentDMTarget() {
      if (currentDMTarget) sendFriendRequestFromDM(currentDMTarget);
    }

    function selectSearchResultUserFromElement(el) {
      const uname = el.getAttribute('data-username');
      if (uname) selectSearchResultUser(uname);
    }
"""

def patch_file(filepath):
    print(f"Patching {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Add helper functions before initiateReply
    if 'function handleReplyActionBtn(' not in content:
        target = 'function initiateReply(msgId, author, snippet) {'
        if target in content:
            content = content.replace(target, NEW_HELPER_FUNCTIONS + '\n    ' + target)
            print("  -> Inserted secure helper functions")
        else:
            print("  [WARN] Could not find initiateReply target")

    # 2. Patch formatMessageContent with secure nonces
    old_fmt = """      // 1. Extract triple backtick code blocks with optional language specifier
      const codeBlocks = [];
      text = text.replace(/```([a-zA-Z0-9_-]+)?(?:\\r?\\n|[\\t ])?([\\s\\S]*?)```/g, (match, rawLang, codeContent) => {
        let lang = (rawLang || '').trim().toLowerCase();
        let code = codeContent;

        if (lang && !knownLangs.includes(lang) && (!window.hljs || !hljs.getLanguage(lang))) {
          const matchedKnown = knownLangs.find(kl => lang.startsWith(kl));
          if (matchedKnown) {
            const remainder = lang.slice(matchedKnown.length);
            lang = matchedKnown;
            code = remainder + ' ' + (code || '');
          } else {
            code = (rawLang || '') + ' ' + (code || '');
            lang = '';
          }
        }

        const placeholder = `__CODE_BLOCK_${codeBlocks.length}__`;
        codeBlocks.push({ lang: lang, code: (code || '').trim() });
        return placeholder;
      });

      // 2. Extract inline code with single backticks
      const inlineCodes = [];
      text = text.replace(/`([^`\\n]+)`/g, (match, inlineContent) => {
        const placeholder = `__INLINE_CODE_${inlineCodes.length}__`;
        inlineCodes.push(inlineContent);
        return placeholder;
      });"""

    new_fmt = """      const codeBlockNonce = Math.random().toString(36).substring(2, 10);
      // 1. Extract triple backtick code blocks with optional language specifier
      const codeBlocks = [];
      text = text.replace(/```([a-zA-Z0-9_-]+)?(?:\\r?\\n|[\\t ])?([\\s\\S]*?)```/g, (match, rawLang, codeContent) => {
        let lang = (rawLang || '').trim().toLowerCase();
        let code = codeContent;

        if (lang && !knownLangs.includes(lang) && (!window.hljs || !hljs.getLanguage(lang))) {
          const matchedKnown = knownLangs.find(kl => lang.startsWith(kl));
          if (matchedKnown) {
            const remainder = lang.slice(matchedKnown.length);
            lang = matchedKnown;
            code = remainder + ' ' + (code || '');
          } else {
            code = (rawLang || '') + ' ' + (code || '');
            lang = '';
          }
        }

        const placeholder = `__CODE_BLOCK_${codeBlockNonce}_${codeBlocks.length}__`;
        codeBlocks.push({ lang: lang, code: (code || '').trim(), placeholder });
        return placeholder;
      });

      // 2. Extract inline code with single backticks
      const inlineCodes = [];
      text = text.replace(/`([^`\\n]+)`/g, (match, inlineContent) => {
        const placeholder = `__INLINE_CODE_${codeBlockNonce}_${inlineCodes.length}__`;
        inlineCodes.push({ content: inlineContent, placeholder });
        return placeholder;
      });"""

    if old_fmt in content:
        content = content.replace(old_fmt, new_fmt)
        print("  -> Hardened code block placeholders with random nonces")

    old_restore = """      // 4. Restore inline code
      inlineCodes.forEach((ic, idx) => {
        const rendered = `<code class="inline-code-pill">${escapeHTML(ic)}</code>`;
        escaped = escaped.replace(`__INLINE_CODE_${idx}__`, rendered);
      });

      // 5. Restore and highlight triple-backtick code blocks
      codeBlocks.forEach((cb, idx) => {"""

    new_restore = """      // 4. Restore inline code
      inlineCodes.forEach((ic, idx) => {
        const rendered = `<code class="inline-code-pill">${escapeHTML(ic.content || ic)}</code>`;
        escaped = escaped.replaceAll(ic.placeholder || `__INLINE_CODE_${idx}__`, rendered);
      });

      // 5. Restore and highlight triple-backtick code blocks
      codeBlocks.forEach((cb, idx) => {"""

    if old_restore in content:
        content = content.replace(old_restore, new_restore)

    old_block_replace = """escaped = escaped.replace(`__CODE_BLOCK_${idx}__`, blockHTML);"""
    new_block_replace = """escaped = escaped.replaceAll(cb.placeholder || `__CODE_BLOCK_${idx}__`, blockHTML);"""
    if old_block_replace in content:
        content = content.replace(old_block_replace, new_block_replace)
        print("  -> Updated placeholder restoration to replaceAll with nonce identifiers")

    # 3. Patch renderLinkPreviewHTML to strictly require https:// or http://
    old_preview = """function renderLinkPreviewHTML(preview) {
      if (!preview || !preview.url) return '';
      const imageHTML = preview.image ? `<img src="${escapeHTML(preview.image)}" class="link-preview-image" alt="Preview Image" onerror="this.style.display='none'">` : '';
      const favHTML = preview.favicon ? `<img src="${escapeHTML(preview.favicon)}" class="link-preview-favicon" alt="Icon" onerror="this.style.display='none'">` : '<span style="font-size:12px;">🔗</span>';
      const siteName = preview.site_name || preview.domain || 'External Link';
      const title = preview.title || preview.url;
      const desc = preview.description ? `<div class="link-preview-desc">${escapeHTML(preview.description)}</div>` : '';

      return `
        <a href="${escapeHTML(preview.url)}" target="_blank" rel="noopener noreferrer" class="msg-link-preview-card" onclick="event.stopPropagation()">"""

    new_preview = """function renderLinkPreviewHTML(preview) {
      if (!preview || !preview.url) return '';
      const rawUrl = String(preview.url).trim();
      if (!rawUrl.startsWith('https://') && !rawUrl.startsWith('http://')) return '';
      const safeUrl = escapeHTML(rawUrl);
      const isSafeImg = (u) => u && (u.startsWith('https://') || u.startsWith('http://') || u.startsWith('/uploads/') || u.startsWith('data:image/'));
      const imageHTML = isSafeImg(preview.image) ? `<img src="${escapeHTML(preview.image)}" class="link-preview-image" alt="Preview Image" onerror="this.style.display='none'">` : '';
      const favHTML = isSafeImg(preview.favicon) ? `<img src="${escapeHTML(preview.favicon)}" class="link-preview-favicon" alt="Icon" onerror="this.style.display='none'">` : '<span style="font-size:12px;">🔗</span>';
      const siteName = preview.site_name || preview.domain || 'External Link';
      const title = preview.title || preview.url;
      const desc = preview.description ? `<div class="link-preview-desc">${escapeHTML(preview.description)}</div>` : '';

      return `
        <a href="${safeUrl}" target="_blank" rel="noopener noreferrer" class="msg-link-preview-card" onclick="event.stopPropagation()">"""

    if old_preview in content:
        content = content.replace(old_preview, new_preview)
        print("  -> Hardened link preview protocol validation (rejecting non-http/https)")

    # 4. Patch DM action buttons (outgoing and incoming)
    content = content.replace(
        """onclick="initiateReply('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 40))}')" title="Reply">💬</button>""",
        """onclick="handleReplyActionBtn(this)" title="Reply">💬</button>"""
    )
    content = content.replace(
        """onclick="initiateReply('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 40))}')" title="Quote Reply">💬</button>""",
        """onclick="handleReplyActionBtn(this)" title="Quote Reply">💬</button>"""
    )
    content = content.replace(
        """onclick="openReportMessageModal('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 45))}')" title="Report">🚩</button>""",
        """onclick="handleReportActionBtn(this)" title="Report">🚩</button>"""
    )
    content = content.replace(
        """onclick="openReportMessageModal('${msg.id}', '${escapeHTML(msg.user)}', '${escapeHTML(msg.content.slice(0, 45))}')" title="Report Message">🚩</button>""",
        """onclick="handleReportActionBtn(this)" title="Report Message">🚩</button>"""
    )
    content = content.replace(
        """onclick="copyMessageText('${msg.id}')" title="Copy">📋</button>""",
        """onclick="copyMessageActionBtn(this)" title="Copy">📋</button>"""
    )
    content = content.replace(
        """onclick="copyMessageText('${msg.id}')" title="Copy Text">📋</button>""",
        """onclick="copyMessageActionBtn(this)" title="Copy Text">📋</button>"""
    )
    content = content.replace(
        """onclick="translateMessageInline('${msg.id}')" title="Translate">🌐</button>""",
        """onclick="translateMessageActionBtn(this)" title="Translate">🌐</button>"""
    )
    content = content.replace(
        """onclick="translateMessageInline('${msg.id}')" title="Translate Message">🌐</button>""",
        """onclick="translateMessageActionBtn(this)" title="Translate Message">🌐</button>"""
    )
    content = content.replace(
        """onclick="promptEditMessage('${msg.id}')" title="Edit">✏️</button>""",
        """onclick="promptEditActionBtn(this)" title="Edit">✏️</button>"""
    )
    content = content.replace(
        """onclick="deleteMessage('${msg.id}')" title="Delete">🗑️</button>""",
        """onclick="deleteMessageActionBtn(this)" title="Delete">🗑️</button>"""
    )
    content = content.replace(
        """onclick="togglePinMessage('${msg.id}')" title="${msg.pinned ? 'Unpin' : 'Pin'}">📌</button>""",
        """onclick="togglePinActionBtn(this)" title="${msg.pinned ? 'Unpin' : 'Pin'}">📌</button>"""
    )
    content = content.replace(
        """onclick="openThreadDrawer('${msg.id}', '${encodeURIComponent(msg.channel_id || currentChannel)}')\" title=\"Reply in Thread\">🧵</button>""",
        """onclick="openThreadActionBtn(this)" title="Reply in Thread">🧵</button>"""
    )

    # 5. Patch member click on avatar and name
    content = content.replace(
        """<div class="dm-bubble-avatar-col" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">""",
        """<div class="dm-bubble-avatar-col" data-username="${escapeHTML(msg.user)}" onclick="handleMemberClickFromElement(this, event)">"""
    )
    content = content.replace(
        """<span class="dm-sender-name" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">${escapeHTML(displayName)}</span>""",
        """<span class="dm-sender-name" data-username="${escapeHTML(msg.user)}" onclick="handleMemberClickFromElement(this, event)">${escapeHTML(displayName)}</span>"""
    )
    content = content.replace(
        """<div class="msg-card-user-info" onclick="handleMemberClick('${escapeHTML(msg.user)}', event)">""",
        """<div class="msg-card-user-info" data-username="${escapeHTML(msg.user)}" onclick="handleMemberClickFromElement(this, event)">"""
    )
    content = content.replace(
        """onclick="handleMemberClick('${escapeHTML(m.username)}', event)">""",
        """onclick="handleMemberClickFromElement(this, event)">"""
    )

    # 6. Patch attachment image click
    content = content.replace(
        """onclick="window.open('${safeAttachmentUrl}', '_blank', 'noopener,noreferrer')">""",
        """onclick="handleAttachmentImgClick(this)">"""
    )
    content = content.replace(
        """alt="Attachment" onclick="handleAttachmentImgClick(this)">""",
        """data-url="${safeAttachment}" alt="Attachment" onclick="handleAttachmentImgClick(this)">"""
    )

    # 7. Patch friend actions in empty/intro state
    content = content.replace(
        """onclick="confirmUnfriendUser('${escapeHTML(currentDMTarget)}')\"""",
        """onclick="handleUnfriendCurrentDMTarget()\""""
    )
    content = content.replace(
        """onclick="sendFriendRequestFromDM('${escapeHTML(currentDMTarget)}')\"""",
        """onclick="handleAddFriendCurrentDMTarget()\""""
    )

    # 8. Ensure card element sets data-msg-id and data-channel-id
    card_id_target = "card.id = `msg-${msg.id}`;"
    card_id_replacement = """card.id = `msg-${msg.id}`;
          card.setAttribute('data-msg-id', msg.id);
          card.setAttribute('data-channel-id', msg.channel_id || currentChannel || '');"""
    if card_id_target in content and "card.setAttribute('data-msg-id'" not in content:
        content = content.replace(card_id_target, card_id_replacement)
        print("  -> Added data-msg-id and data-channel-id attributes on message card")

    # 9. Patch reactions chips to use data-emoji and handleReactionBtnClick
    old_chip = """<span class="msg-rx-chip ${hasReacted ? 'active' : ''}" onclick="handleMessageReaction('${msg.id}', '${emoji}')" title="${(users || []).join(', ')}">"""
    new_chip = """<span class="msg-rx-chip ${hasReacted ? 'active' : ''}" data-emoji="${escapeHTML(emoji)}" onclick="handleReactionBtnClick(this)" title="${escapeHTML((users || []).join(', '))}">"""
    if old_chip in content:
        content = content.replace(old_chip, new_chip)
        print("  -> Updated reaction chip click handling to data-emoji")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"  Successfully wrote {filepath}")

def main():
    for f in FILES:
        patch_file(f)

if __name__ == '__main__':
    main()
