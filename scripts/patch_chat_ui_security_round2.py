#!/usr/bin/env python3
"""
Patch Chat UI Security Hardening (Round 2)
Cleans up remaining inline reaction handlers and thread pills.
"""
FILES = [
    '/Users/madarauchiha/connecto/website/static/index.html',
    '/Users/madarauchiha/connecto/backend/app/index.html'
]

def patch_file(filepath):
    print(f"Patching {filepath} (Round 2)...")
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Patch hover action reactions
    content = content.replace(
        """<button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '❤️')" title="Love ❤️">❤️</button>""",
        """<button class="dm-act-icon" data-emoji="❤️" onclick="handleReactionBtnClick(this)" title="Love ❤️">❤️</button>"""
    )
    content = content.replace(
        """<button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '🔥')" title="Fire 🔥">🔥</button>""",
        """<button class="dm-act-icon" data-emoji="🔥" onclick="handleReactionBtnClick(this)" title="Fire 🔥">🔥</button>"""
    )
    content = content.replace(
        """<button class="dm-act-icon" onclick="handleMessageReaction('${msg.id}', '👍')" title="Thumbs Up 👍">👍</button>""",
        """<button class="dm-act-icon" data-emoji="👍" onclick="handleReactionBtnClick(this)" title="Thumbs Up 👍">👍</button>"""
    )

    content = content.replace(
        """<button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '🔥')" title="React 🔥">🔥</button>""",
        """<button class="msg-act-icon" data-emoji="🔥" onclick="handleReactionBtnClick(this)" title="React 🔥">🔥</button>"""
    )
    content = content.replace(
        """<button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '⚡')" title="React ⚡">⚡</button>""",
        """<button class="msg-act-icon" data-emoji="⚡" onclick="handleReactionBtnClick(this)" title="React ⚡">⚡</button>"""
    )
    content = content.replace(
        """<button class="msg-act-icon" onclick="handleMessageReaction('${msg.id}', '👍')" title="React 👍">👍</button>""",
        """<button class="msg-act-icon" data-emoji="👍" onclick="handleReactionBtnClick(this)" title="React 👍">👍</button>"""
    )

    # 2. Patch thread pill
    content = content.replace(
        """<div class="msg-thread-pill" onclick="openThreadDrawer('${msg.id}', '${encodeURIComponent(msg.channel_id || currentChannel)}')">""",
        """<div class="msg-thread-pill" onclick="openThreadActionBtn(this)">"""
    )

    # 3. Patch search result item
    content = content.replace(
        """<div class="search-result-item" onclick="selectSearchResultUser('${escapeHTML(u)}')">""",
        """<div class="search-result-item" data-username="${escapeHTML(u)}" onclick="selectSearchResultUserFromElement(this)">"""
    )

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"  Successfully wrote {filepath}")

def main():
    for f in FILES:
        patch_file(f)

if __name__ == '__main__':
    main()
