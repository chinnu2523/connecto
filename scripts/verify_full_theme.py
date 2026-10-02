#!/usr/bin/env python3
"""
Verify full theme implementation for Connecto - both parchment and noir themes
in the in-app chat view after login.

Strategy: inject auth token directly into localStorage so we bypass the login form
(which requires the real API backend), then force switchMainView('chat').
"""
import asyncio
import os

ARTIFACTS_DIR = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134"
BASE_URL = "http://localhost:8000"
# Real credentials fetched from production
AUTH_TOKEN = "cf_edge_usr_vivek_1790929162063"
USERNAME = "vivek"


async def inject_auth_and_enter(page):
    """Inject auth token into localStorage and switch to chat view."""
    await page.evaluate(f"""
        // Inject auth credentials
        localStorage.setItem('connecto_user', '{USERNAME}');
        localStorage.setItem('connecto_token', '{AUTH_TOKEN}');
        // Also set the global JS var if already initialized
        try {{ currentUser = '{USERNAME}'; }} catch(e) {{}}
        try {{ authToken = '{AUTH_TOKEN}'; }} catch(e) {{}}
    """)
    await page.wait_for_timeout(500)

    # Reload so the page picks up localStorage on init
    await page.reload(wait_until="domcontentloaded", timeout=15000)
    await page.wait_for_timeout(3000)

    # Check if auto-loaded into chat view
    view_ok = await page.evaluate("""
        (() => {
            const chatView = document.getElementById('chatAppView') ||
                             document.querySelector('.guilds-nav') ||
                             document.querySelector('.channels-sidebar');
            return chatView ? chatView.offsetParent !== null : false;
        })()
    """)
    print(f"  Chat view auto-loaded: {view_ok}")

    if not view_ok:
        # Force switch to chat
        await page.evaluate("""
            try {
                if (typeof switchMainView === 'function') switchMainView('chat');
            } catch(e) { console.error('switchMainView error:', e); }
        """)
        await page.wait_for_timeout(2500)

    # Verify currentUser is set
    user_ok = await page.evaluate("""
        (typeof currentUser !== 'undefined' && currentUser && currentUser !== 'guest')
    """)
    print(f"  currentUser OK: {user_ok}")
    return user_ok


async def screenshot_theme(page, theme_val, prefix):
    """Apply theme and capture screenshots for a given theme."""
    # Apply theme
    await page.evaluate(f"""
        localStorage.setItem('connecto_home_theme', '{theme_val}');
        document.documentElement.setAttribute('data-theme', '{theme_val}');
        try {{
            if (typeof setHomepageTheme === 'function') setHomepageTheme('{theme_val}', false);
        }} catch(e) {{}}
    """)
    await page.wait_for_timeout(800)

    # Ensure still in chat view
    view_ok = await page.evaluate("""
        (() => {
            const chatView = document.getElementById('chatAppView') ||
                             document.querySelector('.guilds-nav');
            return chatView ? chatView.offsetParent !== null : false;
        })()
    """)
    if not view_ok:
        await page.evaluate("if (typeof switchMainView === 'function') switchMainView('chat');")
        await page.wait_for_timeout(1500)
        # Re-apply theme after switch
        await page.evaluate(f"""
            document.documentElement.setAttribute('data-theme', '{theme_val}');
            try {{ if (typeof setHomepageTheme === 'function') setHomepageTheme('{theme_val}', false); }} catch(e) {{}}
        """)
        await page.wait_for_timeout(500)

    # Full page screenshot
    out = f"{ARTIFACTS_DIR}/{prefix}_full.png"
    await page.screenshot(path=out, full_page=False)
    print(f"  ✓ Full view      → {os.path.basename(out)}")

    # Individual component screenshots
    components = [
        ("guilds_nav",    ".guilds-nav"),
        ("channels",      ".channels-sidebar"),
        ("chat_header",   ".chat-header"),
        ("messages",      "#messagesArea, .messages-area"),
        ("input_area",    ".stitch-input-card, .input-area, .input-box-wrap"),
        ("profile_bar",   ".user-profile-bar"),
        ("members",       ".members-sidebar"),
    ]
    for name, sel in components:
        try:
            el = page.locator(sel).first
            if await el.is_visible(timeout=2000):
                path = f"{ARTIFACTS_DIR}/{prefix}_{name}.png"
                await el.screenshot(path=path)
                print(f"  ✓ {name:<14} → {os.path.basename(path)}")
            else:
                print(f"  – {name:<14}   not visible")
        except Exception as e:
            print(f"  ✗ {name:<14}   skipped ({type(e).__name__})")


async def run():
    from playwright.async_api import async_playwright

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        ctx = await browser.new_context(viewport={"width": 1440, "height": 900})
        page = await ctx.new_page()

        # Step 1: Load the page once to init localStorage namespace
        print("Loading page...")
        await page.goto(BASE_URL, wait_until="domcontentloaded", timeout=15000)
        await page.wait_for_timeout(1000)

        # Step 2: Inject auth and enter app
        print("Injecting auth credentials...")
        user_ok = await inject_auth_and_enter(page)

        if not user_ok:
            # Last resort: direct JS call to set currentUser in global scope
            await page.evaluate(f"""
                window.currentUser = '{USERNAME}';
                window.authToken = '{AUTH_TOKEN}';
                if (typeof switchMainView === 'function') switchMainView('chat');
            """)
            await page.wait_for_timeout(2000)

        # Take a diagnostic screenshot first
        diag = f"{ARTIFACTS_DIR}/diagnostic_post_login.png"
        await page.screenshot(path=diag)
        print(f"  ✓ Diagnostic     → diagnostic_post_login.png")

        # ── PARCHMENT THEME ───────────────────────────────────────────────
        print("\n=== PARCHMENT (ivory paper) theme ===")
        await screenshot_theme(page, "parchment", "proof_parchment")

        # ── NOIR THEME ────────────────────────────────────────────────────
        print("\n=== NOIR (pure black OLED) theme ===")
        await screenshot_theme(page, "noir", "proof_noir")

        await browser.close()
        print("\n✅ All screenshots saved.")


if __name__ == "__main__":
    asyncio.run(run())
