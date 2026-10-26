import asyncio
from playwright.async_api import async_playwright

async def run():
    async with async_playwright() as p:
        b = await p.chromium.launch()
        page = await b.new_page()
        await page.set_content('''
            <script>
                window.pwned = false;
                function escapeHTML(str) {
                    if (!str) return '';
                    return String(str).replace(/[&<>'"]/g, tag => ({
                        '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
                    }[tag] || tag));
                }
                function renderLinkPreviewHTML(preview) {
                    if (!preview || !preview.url) return '';
                    const safeUrl = (preview.url.startsWith('https://') || preview.url.startsWith('http://')) ? escapeHTML(preview.url) : '#';
                    return `<a id="testLink" href="${escapeHTML(preview.url)}">Link</a>`;
                }
                document.body.innerHTML = renderLinkPreviewHTML({ url: 'javascript:window.pwned=true;' });
            </script>
        ''')
        await page.click('#testLink')
        res = await page.evaluate('() => window.pwned')
        print('Was javascript: executed?:', res)
        await b.close()

asyncio.run(run())
