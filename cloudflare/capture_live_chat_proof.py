import asyncio
from playwright.async_api import async_playwright

async def capture_chat():
    async with async_playwright() as p:
        browser = await p.chromium.launch(
            executable_path="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            headless=True
        )
        context = await browser.new_context(viewport={"width": 1440, "height": 900})
        page = await context.new_page()

        # Set localStorage session for madara_legend to enter chat directly
        await page.goto("https://connecto.fun")
        await page.evaluate("""() => {
            localStorage.setItem("connecto_token", "cf_edge_usr_madara_legend_1790862000");
            localStorage.setItem("connecto_user", "madara_legend");
            localStorage.setItem("connecto_username", "madara_legend");
            localStorage.setItem("connecto_nickname", "Madara Uchiha");
            localStorage.setItem("connecto_avatar", "👾");
        }""")

        # Navigate to reload with session
        await page.goto("https://connecto.fun")
        await page.wait_for_timeout(3000)

        # Try clicking "Open Live Chat" if visible
        try:
            btn = page.locator("text=Open Live Chat").first
            if await btn.is_visible():
                await btn.click()
                await page.wait_for_timeout(2000)
        except Exception:
            pass

        # Capture screenshot of restored live chat
        path = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/proof_live_website_restored_chat.png"
        await page.screenshot(path=path)
        print(f"Screenshot saved to {path}")

        # Click "#voice-lounge" tab or microphone button
        try:
            voice_tab = page.locator("button:has-text('voice-lounge')").first
            if await voice_tab.is_visible():
                await voice_tab.click()
                await page.wait_for_timeout(2000)
            else:
                # Try clicking left icon with mic
                mic_btn = page.locator("div.nav-item, button").filter(has_text="voice-lounge").first
                await mic_btn.click()
                await page.wait_for_timeout(2000)
        except Exception as e:
            print("Voice click err:", e)

        vpath = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/proof_live_website_voice_rooms.png"
        await page.screenshot(path=vpath)
        print(f"Voice screenshot saved to {vpath}")

        await browser.close()

if __name__ == "__main__":
    asyncio.run(capture_chat())
