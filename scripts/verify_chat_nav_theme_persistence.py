import asyncio
from playwright.async_api import async_playwright

async def check():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True, executable_path='/Applications/Google Chrome.app/Contents/MacOS/Google Chrome')
        page = await browser.new_page(viewport={'width': 1440, 'height': 900})
        await page.goto('http://localhost:8000', wait_until='domcontentloaded')
        await page.evaluate("""() => {
            localStorage.setItem('connecto_home_theme', 'parchment');
            localStorage.setItem('connecto_user', 'sophia');
            localStorage.setItem('connecto_token', 'test_token_sophia');
        }""")
        await page.reload(wait_until='domcontentloaded')
        await page.wait_for_timeout(1000)
        # Click enter chat
        await page.click('button:has-text("Enter Chat")')
        await page.wait_for_timeout(2000)
        theme = await page.evaluate("() => document.documentElement.getAttribute('data-theme')")
        print('In-App theme after clicking Enter Chat:', theme)
        assert theme == 'parchment', f"Expected parchment, got {theme}"
        await page.screenshot(path='/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/proof_chat_nav_theme_persistence.png')
        await browser.close()
        print("SUCCESS! Navigating into chat preserved parchment theme perfectly.")

if __name__ == '__main__':
    asyncio.run(check())
