import asyncio
from playwright.async_api import async_playwright

async def inspect_phone():
    async with async_playwright() as p:
        browser = await p.chromium.launch(
            headless=True,
            executable_path="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
        )
        ctx = await browser.new_context(
            viewport={"width": 390, "height": 844},
            user_agent="Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1"
        )
        page = await ctx.new_page()

        # 1. Homepage phone view
        await page.goto("http://localhost:8000", wait_until="domcontentloaded")
        await page.wait_for_timeout(1000)
        await page.screenshot(path="/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/diag_phone_homepage.png")

        # 2. In-app chat phone view
        await page.evaluate("""() => {
            localStorage.setItem('connecto_user', 'sophia');
            localStorage.setItem('connecto_token', 'test_token');
            switchMainView('chat');
        }""")
        await page.wait_for_timeout(1500)
        await page.screenshot(path="/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/diag_phone_chat.png")

        # 3. Channel sidebar drawer on phone
        await page.evaluate("""() => {
            if (typeof toggleMobileChannelDrawer === 'function') toggleMobileChannelDrawer();
            else if (typeof toggleSidebar === 'function') toggleSidebar();
        }""")
        await page.wait_for_timeout(500)
        await page.screenshot(path="/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/diag_phone_sidebar.png")

        await browser.close()
        print("Captured phone view screenshots successfully.")

if __name__ == "__main__":
    asyncio.run(inspect_phone())
