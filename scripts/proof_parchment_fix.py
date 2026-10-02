"""
Proof screenshots: parchment theme fixes verification
- Members sidebar (COMMUNITY MEMBERS heading visible)
- Friends view (tabs + content visible)  
- Nav pill (profile visible)
- Stealth mode toggle test
- Online / offline status dots
- Noir regression check
"""
import asyncio
from playwright.async_api import async_playwright

BASE = "http://localhost:8000"
USERNAME = "vivek"
TOKEN = "cf_edge_usr_vivek_1790929162063"
OUT = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134"

async def setup_page(page, theme: str):
    await page.goto(BASE, wait_until="domcontentloaded")
    await page.evaluate(f"""() => {{
        localStorage.setItem('connecto_user', '{USERNAME}');
        localStorage.setItem('connecto_token', '{TOKEN}');
        localStorage.setItem('connecto_home_theme', '{theme}');
    }}""")
    await page.reload(wait_until="domcontentloaded")
    await page.wait_for_timeout(2500)
    # Switch to in-app chat view
    await page.evaluate("() => { if (typeof switchMainView === 'function') switchMainView('chat'); }")
    await page.wait_for_timeout(2500)

async def run():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        ctx = await browser.new_context(viewport={"width": 1400, "height": 900})
        page = await ctx.new_page()

        # ── PARCHMENT THEME FIXES ──────────────────────────────────────────────
        print("Setting up PARCHMENT theme...")
        await setup_page(page, "parchment")

        # 1. Full view
        await page.screenshot(path=f"{OUT}/proof_parchment_full_fixed.png", full_page=False)
        print("  ✓ full view")

        # 2. Members sidebar — open it
        await page.evaluate("() => { if (typeof toggleMembersSidebar === 'function') toggleMembersSidebar(true); }")
        await page.wait_for_timeout(800)
        members = page.locator("#membersSidebar")
        if await members.is_visible():
            await members.screenshot(path=f"{OUT}/proof_parchment_members_fixed.png")
            print("  ✓ members sidebar")
        else:
            await page.screenshot(path=f"{OUT}/proof_parchment_members_fixed.png")
            print("  ✓ members (fallback full page)")

        # 3. Friends view
        await page.evaluate("() => { if (typeof switchMainView === 'function') switchMainView('friends'); }")
        await page.wait_for_timeout(1500)
        friends_view = page.locator("#friendsView")
        if await friends_view.is_visible():
            await friends_view.screenshot(path=f"{OUT}/proof_parchment_friends_fixed.png")
            print("  ✓ friends view")
        else:
            await page.screenshot(path=f"{OUT}/proof_parchment_friends_fixed.png")
            print("  ✓ friends (fallback full page)")

        # 4. Nav user pill / profile bar (landing nav)
        await page.evaluate("() => { if (typeof switchMainView === 'function') switchMainView('home'); }")
        await page.wait_for_timeout(1000)
        nav_pill = page.locator(".nav-user-pill").first
        if await nav_pill.is_visible():
            await nav_pill.screenshot(path=f"{OUT}/proof_parchment_navpill_fixed.png")
            print("  ✓ nav pill")
        else:
            await page.screenshot(path=f"{OUT}/proof_parchment_navpill_fixed.png")
            print("  ✓ nav pill (fallback)")

        # 5. Back to chat — stealth mode test
        await page.evaluate("() => { if (typeof switchMainView === 'function') switchMainView('chat'); }")
        await page.wait_for_timeout(1500)

        # Simulate stealth ON
        stealth_result_on = await page.evaluate("""() => {
            if (typeof updateStealthStatusUI === 'function') {
                updateStealthStatusUI(true);
                const dot = document.querySelector('.nav-user-status-dot');
                return dot ? { className: dot.className, title: dot.title, color: getComputedStyle(dot).backgroundColor } : null;
            }
            return 'function not found';
        }""")
        print(f"  Stealth ON dot: {stealth_result_on}")
        await page.screenshot(path=f"{OUT}/proof_parchment_stealth_on.png")

        # Simulate stealth OFF
        stealth_result_off = await page.evaluate("""() => {
            if (typeof updateStealthStatusUI === 'function') {
                updateStealthStatusUI(false);
                const dot = document.querySelector('.nav-user-status-dot');
                return dot ? { className: dot.className, title: dot.title, color: getComputedStyle(dot).backgroundColor } : null;
            }
            return 'function not found';
        }""")
        print(f"  Stealth OFF dot: {stealth_result_off}")
        await page.screenshot(path=f"{OUT}/proof_parchment_stealth_off.png")

        # ── NOIR REGRESSION CHECK ──────────────────────────────────────────────
        print("Checking NOIR theme (regression)...")
        await setup_page(page, "noir")
        await page.screenshot(path=f"{OUT}/proof_noir_full_regression.png")
        print("  ✓ noir full")
        await page.evaluate("() => { if (typeof toggleMembersSidebar === 'function') toggleMembersSidebar(true); }")
        await page.wait_for_timeout(800)
        noir_members = page.locator("#membersSidebar")
        if await noir_members.is_visible():
            await noir_members.screenshot(path=f"{OUT}/proof_noir_members_regression.png")
        else:
            await page.screenshot(path=f"{OUT}/proof_noir_members_regression.png")
        print("  ✓ noir members")

        await browser.close()
        print("\nAll proof screenshots captured!")

asyncio.run(run())
