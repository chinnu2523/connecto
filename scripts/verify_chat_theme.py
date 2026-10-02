import asyncio
from playwright.async_api import async_playwright

async def run():
    async with async_playwright() as p:
        browser = await p.chromium.launch(
            headless=True,
            executable_path="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
        )
        context = await browser.new_context(viewport={"width": 1440, "height": 900})
        page = await context.new_page()

        print("1. Loading http://127.0.0.1:8000/?theme=parchment...")
        await page.goto("http://127.0.0.1:8000/?theme=parchment", wait_until="domcontentloaded")
        await page.wait_for_timeout(1000)

        # Check theme on landing
        theme = await page.evaluate("() => document.documentElement.getAttribute('data-theme')")
        print(f"Landing theme: {theme}")
        assert theme == "parchment", f"Expected parchment, got {theme}"

        # Enter in-app chat view
        print("2. Entering in-app chat view...")
        await page.evaluate("""() => {
            currentUser = 'sophia';
            authToken = 'test_token';
            currentAvatar = '/static/img/avatars/sophia.svg';
            localStorage.setItem('connecto_user', 'sophia');
            localStorage.setItem('connecto_token', 'test_token');
            switchMainView('chat');

            // Populate sample messages into #messagesArea for visual verification
            const msgArea = document.getElementById('messagesArea');
            if (msgArea) {
                msgArea.innerHTML = `
                <div class="message-group">
                    <div class="msg-group-header">
                        <span class="msg-group-avatar"><img src="/img/avatars/marcus.svg" alt="Marcus"></span>
                        <span class="msg-group-author">Marcus Aurelius</span>
                        <span class="msg-group-handle">@marcus</span>
                        <span class="msg-group-timestamp">12:45 PM</span>
                    </div>
                    <div class="msg-row">
                        <div class="msg-bubble msg-bubble-other bubble-single">
                            Database sync between local SQLite and Cloudflare D1 edge has been verified! Both Ivory Paper and Pure Black OLED themes are active.
                        </div>
                    </div>
                </div>

                <div class="message-group">
                    <div class="msg-group-header">
                        <span class="msg-group-avatar"><img src="/img/avatars/sophia.svg" alt="Sophia"></span>
                        <span class="msg-group-author msg-group-author-mine">Sophia Reynolds</span>
                        <span class="msg-group-handle">@sophia</span>
                        <span class="msg-group-timestamp">12:46 PM</span>
                    </div>
                    <div class="msg-row">
                        <div class="msg-bubble msg-bubble-mine bubble-single">
                            Here is the atomic batch synchronizer implementation in Python:
                            <div class="code-block-container">
                                <div class="code-block-header">
                                    <span class="code-lang-tag">python</span>
                                    <button class="btn-copy-code">Copy</button>
                                </div>
                                <pre class="hljs-pre"><code>d1_sync_manager.execute_batch_cloud_d1(statements)</code></pre>
                            </div>
                        </div>
                    </div>
                </div>
                `;
            }
        }""")
        await page.wait_for_timeout(1000)

        # Verify in-app view applied and theme is still parchment
        in_app_theme = await page.evaluate("() => document.documentElement.getAttribute('data-theme')")
        is_in_app = await page.evaluate("() => document.body.classList.contains('in-app-view')")
        print(f"In-app view active: {is_in_app}, Theme: {in_app_theme}")
        assert is_in_app, "Expected body to have in-app-view class"
        assert in_app_theme == "parchment", f"Expected parchment in chat, got {in_app_theme}"

        # Check styles of key in-app components in Parchment
        styles = await page.evaluate("""() => {
            const chSidebar = document.getElementById('channelsSidebar');
            const chatHeader = document.querySelector('.chat-header');
            const headerTitle = document.getElementById('headerTitle');
            const inputWrap = document.querySelector('.input-box-wrap');
            const toggleBtn = document.getElementById('chatHeaderThemeToggleBtn');
            const profToggleBtn = document.getElementById('userProfileThemeBtn');

            return {
                sidebarBg: window.getComputedStyle(chSidebar).backgroundColor,
                headerBg: window.getComputedStyle(chatHeader).backgroundColor,
                headerTitleColor: window.getComputedStyle(headerTitle).color,
                inputWrapBg: inputWrap ? window.getComputedStyle(inputWrap).backgroundColor : null,
                toggleBtnVisible: toggleBtn ? (toggleBtn.offsetWidth > 0 && toggleBtn.offsetHeight > 0) : false,
                profToggleBtnVisible: profToggleBtn ? (profToggleBtn.offsetWidth > 0 && profToggleBtn.offsetHeight > 0) : false
            };
        }""")
        print("Parchment in-app styles:", styles)
        assert styles["toggleBtnVisible"], "Chat header theme toggle button must be visible"
        assert styles["profToggleBtnVisible"], "User profile bar theme toggle button must be visible"

        # Screenshot In-App Chat in Ivory Paper theme
        screenshot1 = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/chat_ui_ivory_paper.png"
        await page.screenshot(path=screenshot1)
        print(f"Saved: {screenshot1}")

        # 3. Toggle to Noir (Dark) from chat header button
        print("3. Toggling to Pure Black OLED via in-chat header button...")
        await page.click("#chatHeaderThemeToggleBtn")
        await page.wait_for_timeout(800)

        theme_noir = await page.evaluate("() => document.documentElement.getAttribute('data-theme')")
        print(f"Theme after click: {theme_noir}")
        assert theme_noir == "noir", f"Expected noir, got {theme_noir}"

        # Screenshot In-App Chat in Pure Black OLED theme
        screenshot2 = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/chat_ui_pure_black_oled.png"
        await page.screenshot(path=screenshot2)
        print(f"Saved: {screenshot2}")

        # 4. Open Settings Modal and check Appearance tab
        print("4. Opening Settings Modal -> Appearance tab...")
        await page.evaluate("""() => {
            openSettingsModal();
            switchProfileStudioTab('theme');
        }""")
        await page.wait_for_timeout(800)

        # Check theme selector cards
        theme_tab_info = await page.evaluate("""() => {
            const pillNoir = document.getElementById('themePillNoir');
            const pillParchment = document.getElementById('themePillParchment');
            return {
                noirPillDisplay: window.getComputedStyle(pillNoir).display,
                parchmentPillDisplay: window.getComputedStyle(pillParchment).display
            };
        }""")
        print("Theme tab info in Noir:", theme_tab_info)
        assert theme_tab_info["noirPillDisplay"] != "none", "Noir pill should be visible when Noir is active"

        # Click Ivory Paper in Settings modal
        print("Clicking Ivory Paper inside Settings modal...")
        await page.click("#themeCardParchment")
        await page.wait_for_timeout(600)

        theme_back = await page.evaluate("() => document.documentElement.getAttribute('data-theme')")
        print(f"Theme after selecting in modal: {theme_back}")
        assert theme_back == "parchment", f"Expected parchment, got {theme_back}"

        # Screenshot Settings Modal in Ivory Paper
        screenshot3 = "/Users/madarauchiha/.gemini/antigravity/brain/737b130d-d4ea-42bb-a456-c5c839406134/settings_modal_appearance_parchment.png"
        await page.screenshot(path=screenshot3)
        print(f"Saved: {screenshot3}")

        await browser.close()
        print("ALL TESTS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    asyncio.run(run())
