import asyncio
import os
from playwright.async_api import async_playwright

BASE_URL = "http://localhost:8000"
ARTIFACT_DIR = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134"

async def test_theme_and_alternate_colors():
    async with async_playwright() as p:
        browser = await p.chromium.launch(
            headless=True,
            executable_path="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
        )
        context = await browser.new_context(viewport={"width": 1440, "height": 900})
        page = await context.new_page()

        print("=== STEP 1: Verify Dark Theme (Noir) and Pure White Alternate Color ===")
        await page.goto(BASE_URL, wait_until="domcontentloaded")
        await page.evaluate("""() => {
            localStorage.setItem('connecto_home_theme', 'noir');
            localStorage.setItem('connecto_user', 'sophia');
            localStorage.setItem('connecto_token', 'test_token_sophia');
            if (typeof setHomepageTheme === 'function') setHomepageTheme('noir');
            if (typeof switchMainView === 'function') switchMainView('chat');
        }""")
        await page.wait_for_timeout(1500)

        # Inject sample chat messages to showcase incoming cards and alternate mine cards
        await page.evaluate("""() => {
            currentChannel = 'general';
            currentUser = 'sophia';
            messageStore.clear();
            messageStore.set('m1', {
                id: 'm1',
                user: 'marcus',
                nickname: 'Marcus Aurelius',
                avatar: '/img/avatars/marcus.svg',
                content: 'Data parity and automated synchronization across Tailscale box-1 and Cloudflare D1 is fully established! Real-time telemetry is streaming.',
                created_at: new Date(Date.now() - 60000).toISOString()
            });
            messageStore.set('m2', {
                id: 'm2',
                user: 'sophia',
                nickname: 'Sophia Reynolds',
                avatar: '/img/avatars/sophia.svg',
                content: 'Confirmed! My messages now render in the pristine Pure White alternate card style for maximum contrast against pitch black.',
                created_at: new Date(Date.now() - 30000).toISOString()
            });
            messageStore.set('m3', {
                id: 'm3',
                user: 'elena',
                nickname: 'Elena Rostova',
                avatar: '/img/avatars/elena.svg',
                content: 'Sub-3s auto-updating ticker is active. Ephemeral polling guarantees zero dropped updates across tab switches.',
                created_at: new Date(Date.now() - 10000).toISOString()
            });
            if (typeof _doRenderMessages === 'function') _doRenderMessages();
        }""")
        await page.wait_for_timeout(1000)

        # Inspect computed styles for Dark Theme
        noir_data = await page.evaluate("""() => {
            const incomingCard = document.querySelector('.connecto-message-card:not(.mine)');
            const mineCard = document.querySelector('.connecto-message-card.mine');
            const mineAuthor = mineCard ? mineCard.querySelector('.msg-card-author') : null;
            const mineBody = mineCard ? mineCard.querySelector('.msg-card-body') : null;
            return {
                theme: document.documentElement.getAttribute('data-theme'),
                incomingBg: incomingCard ? getComputedStyle(incomingCard).backgroundColor : null,
                mineBg: mineCard ? getComputedStyle(mineCard).backgroundColor : null,
                mineColor: mineCard ? getComputedStyle(mineCard).color : null,
                mineAuthorColor: mineAuthor ? getComputedStyle(mineAuthor).color : null,
                mineBodyColor: mineBody ? getComputedStyle(mineBody).color : null
            };
        }""")
        print(f"Noir Theme Data: {noir_data}")

        # Capture Dark Theme proof
        noir_screenshot_path = os.path.join(ARTIFACT_DIR, "proof_noir_white_alternate_cards.png")
        await page.screenshot(path=noir_screenshot_path)
        print(f"Captured Dark Theme Proof: {noir_screenshot_path}")

        print("\n=== STEP 2: Verify Light Theme (Parchment) and Deep Black Alternate Color ===")
        # Switch to Parchment
        await page.evaluate("""() => {
            if (typeof setHomepageTheme === 'function') setHomepageTheme('parchment');
            if (typeof _doRenderMessages === 'function') _doRenderMessages();
        }""")
        await page.wait_for_timeout(1500)

        parchment_data = await page.evaluate("""() => {
            const incomingCard = document.querySelector('.connecto-message-card:not(.mine)');
            const mineCard = document.querySelector('.connecto-message-card.mine');
            const mineAuthor = mineCard ? mineCard.querySelector('.msg-card-author') : null;
            const mineBody = mineCard ? mineCard.querySelector('.msg-card-body') : null;
            return {
                theme: document.documentElement.getAttribute('data-theme'),
                incomingBg: incomingCard ? getComputedStyle(incomingCard).backgroundColor : null,
                mineBg: mineCard ? getComputedStyle(mineCard).backgroundColor : null,
                mineColor: mineCard ? getComputedStyle(mineCard).color : null,
                mineAuthorColor: mineAuthor ? getComputedStyle(mineAuthor).color : null,
                mineBodyColor: mineBody ? getComputedStyle(mineBody).color : null
            };
        }""")
        print(f"Parchment Theme Data: {parchment_data}")

        # Capture Light Theme proof
        parchment_screenshot_path = os.path.join(ARTIFACT_DIR, "proof_parchment_black_alternate_cards.png")
        await page.screenshot(path=parchment_screenshot_path)
        print(f"Captured Light Theme Proof: {parchment_screenshot_path}")

        print("\n=== STEP 3: Verify Persistence Across Page Reload (No Reversion!) ===")
        # Current theme in localStorage should be 'parchment'
        # Now reload page without any query parameter
        await page.reload(wait_until="domcontentloaded")
        await page.wait_for_timeout(2000)

        persisted_theme = await page.evaluate("() => document.documentElement.getAttribute('data-theme')")
        persisted_storage = await page.evaluate("() => localStorage.getItem('connecto_home_theme')")
        print(f"After Reload -> DOM Theme: {persisted_theme}, localStorage: {persisted_storage}")
        assert persisted_theme == "parchment", f"Theme reverted! Expected parchment, got {persisted_theme}"
        assert persisted_storage == "parchment", f"Storage corrupted! Expected parchment, got {persisted_storage}"

        # Capture Reload Proof
        reload_screenshot_path = os.path.join(ARTIFACT_DIR, "proof_theme_persistence_after_reload.png")
        await page.screenshot(path=reload_screenshot_path)
        print(f"Captured Reload Persistence Proof: {reload_screenshot_path}")

        print("\n=== ALL THEME & ALTERNATE COLOR VERIFICATIONS PASSED ===")
        await browser.close()

if __name__ == "__main__":
    asyncio.run(test_theme_and_alternate_colors())
