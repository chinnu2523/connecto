import base64
import json
import os
import subprocess

def run():
    # 1. Reset to clean HEAD state first
    subprocess.run([
        'git', 'checkout', '--',
        'backend/app/maintenance.html',
        'cloudflare/worker.js',
        'website/static/maintenance.html',
        'website/static/maintenance_classic_3.html'
    ], check=True)

    with open('website/static/img/telegram_channel_card.webp', 'rb') as f:
        b64_qr = base64.b64encode(f.read()).decode('utf-8')

    with open('website/static/maintenance.html', 'r', encoding='utf-8') as f:
        content = f.read()

    # Box-sizing & reset
    content = content.replace(
        '  <style>',
        '''  <style>
    *, *::before, *::after {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }''',
        1
    )

    # Center box width
    content = content.replace(
        '''    main.center-box {
      max-width: 520px;''',
        '''    main.center-box {
      width: 100%;
      max-width: 520px;''',
        1
    )

    # CSS for Telegram buttons, footer, and modal
    css_anchor = '''    .btn-status-link:hover svg {
      transform: translateX(2px);
    }'''

    telegram_css = '''    .btn-status-link:hover svg {
      transform: translateX(2px);
    }

    .btn-telegram-link {
      background: rgba(42, 171, 238, 0.12);
      color: #70c7f7;
      border: 1px solid rgba(42, 171, 238, 0.35);
      padding: 10px 20px;
      border-radius: 24px;
      font-size: 14px;
      font-weight: 500;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 7px;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .btn-telegram-link:hover {
      background: rgba(42, 171, 238, 0.22);
      color: #ffffff;
      border-color: rgba(42, 171, 238, 0.65);
      transform: translateY(-2px);
      box-shadow: 0 6px 18px rgba(42, 171, 238, 0.25);
    }
    .btn-telegram-link:active {
      transform: translateY(0) scale(0.97);
    }
    .btn-telegram-link svg {
      width: 15px;
      height: 15px;
      fill: currentColor;
      transition: transform 0.2s ease;
    }
    .btn-telegram-link:hover svg {
      transform: scale(1.12) rotate(-8deg);
    }

    .btn-qr-link {
      background: transparent;
      color: var(--text-sub);
      border: 1px solid var(--border);
      padding: 10px 18px;
      border-radius: 24px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: color 0.15s ease, border-color 0.15s ease, transform 0.15s ease;
    }
    .btn-qr-link:hover {
      color: var(--text-title);
      border-color: rgba(255, 255, 255, 0.25);
      transform: translateY(-1px);
    }
    .btn-qr-link svg {
      width: 14px;
      height: 14px;
    }

    .footer-telegram {
      color: var(--text-sub);
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      transition: color 0.15s ease;
    }
    .footer-telegram:hover {
      color: #70c7f7;
    }
    .footer-telegram svg {
      width: 13px;
      height: 13px;
      fill: currentColor;
    }

    /* Telegram QR Modal */
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.82);
      backdrop-filter: blur(8px);
      -webkit-backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      z-index: 1000;
      opacity: 0;
      visibility: hidden;
      pointer-events: none;
      transition: opacity 0.25s cubic-bezier(0.16, 1, 0.3, 1), visibility 0.25s;
    }
    .modal-overlay.active {
      opacity: 1;
      visibility: visible;
      pointer-events: auto;
    }
    .modal-card {
      background: #121212;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 22px;
      padding: 24px 22px 20px;
      max-width: 350px;
      width: 100%;
      text-align: center;
      box-shadow: 0 30px 60px rgba(0, 0, 0, 0.75), 0 0 0 1px rgba(255, 255, 255, 0.05);
      position: relative;
      transform: scale(0.92) translateY(12px);
      transition: transform 0.3s cubic-bezier(0.34, 1.4, 0.64, 1);
    }
    .modal-overlay.active .modal-card {
      transform: scale(1) translateY(0);
    }
    .modal-close-btn {
      position: absolute;
      top: 14px;
      right: 14px;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      border: 1px solid var(--border);
      background: rgba(255, 255, 255, 0.05);
      color: var(--text-sub);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 16px;
      line-height: 1;
      transition: all 0.15s ease;
    }
    .modal-close-btn:hover {
      color: #fff;
      border-color: rgba(255, 255, 255, 0.3);
      background: rgba(255, 255, 255, 0.1);
    }
    .modal-title {
      font-size: 17px;
      font-weight: 600;
      color: var(--text-title);
      margin-top: 4px;
      margin-bottom: 6px;
    }
    .modal-desc {
      font-size: 13px;
      color: var(--text-sub);
      line-height: 1.45;
      margin-bottom: 16px;
    }
    .modal-qr-box {
      background: #0a0a0a;
      border-radius: 16px;
      padding: 10px;
      display: inline-block;
      border: 1px solid rgba(255, 255, 255, 0.08);
      margin-bottom: 14px;
    }
    .modal-qr-box img {
      display: block;
      width: 180px;
      height: auto;
      border-radius: 10px;
      margin: 0 auto;
    }
    .modal-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-family: monospace;
      font-size: 12px;
      color: #70c7f7;
      background: rgba(42, 171, 238, 0.1);
      border: 1px solid rgba(42, 171, 238, 0.25);
      padding: 4px 12px;
      border-radius: 14px;
      margin-bottom: 14px;
    }
    .modal-buttons {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .btn-modal-open {
      background: var(--button-bg);
      color: var(--button-text);
      font-weight: 600;
      font-size: 13.5px;
      padding: 10px 16px;
      border-radius: 20px;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      transition: all 0.2s ease;
    }
    .btn-modal-open:hover {
      background: var(--button-hover);
      transform: translateY(-1px);
    }
    .btn-modal-app {
      background: transparent;
      color: var(--text-sub);
      border: 1px solid var(--border);
      font-size: 12.5px;
      padding: 8px 14px;
      border-radius: 20px;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      transition: all 0.15s ease;
    }
    .btn-modal-app:hover {
      color: var(--text-title);
      border-color: rgba(255, 255, 255, 0.25);
    }'''

    assert css_anchor in content
    content = content.replace(css_anchor, telegram_css, 1)

    # Responsive Media Query
    mq_old = '''    @media (max-width: 520px) {
      body { padding: 32px 18px 24px; }
      h1.heading { font-size: 26px; }
      p.body-text { font-size: 14.5px; }
      .button-group { flex-direction: column; width: 100%; }
      .btn-refresh, .btn-status-link { width: 100%; justify-content: center; }
    }'''

    mq_new = '''    @media (max-width: 520px) {
      body { padding: 32px 20px 24px; }
      main.center-box { width: 100%; margin: 24px auto; }
      h1.heading { font-size: 24px; line-height: 1.3; }
      p.body-text { font-size: 14px; line-height: 1.6; }
      .button-group { flex-direction: column; width: 100%; gap: 10px; }
      .btn-refresh, .btn-telegram-link, .btn-qr-link, .btn-status-link { width: 100%; justify-content: center; box-sizing: border-box; }
      footer.bottom-bar { flex-direction: column; gap: 8px; font-size: 12px; }
      .bottom-sep { display: none; }
    }'''

    assert mq_old in content
    content = content.replace(mq_old, mq_new, 1)

    # Primary Action Buttons
    btn_old = '''    <!-- Primary Action Buttons -->
    <div class="button-group animate-entry delay-5">
      <button class="btn-refresh" id="btn-refresh" onclick="handleRefresh()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M23 4v6h-6M1 20v-6h6"/><path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/></svg>
        <span id="refresh-label">Refresh Page</span>
      </button>
      <a href="/static/status.html" class="btn-status-link">
        <span>View System Status</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
      </a>
    </div>'''

    btn_new = '''    <!-- Primary Action Buttons -->
    <div class="button-group animate-entry delay-5">
      <button class="btn-refresh" id="btn-refresh" onclick="handleRefresh()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M23 4v6h-6M1 20v-6h6"/><path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/></svg>
        <span id="refresh-label">Refresh Page</span>
      </button>
      <a href="https://t.me/VCONNECTOFUN" target="_blank" rel="noopener noreferrer" class="btn-telegram-link" id="btn-telegram-primary" onclick="handleTelegramClick(event)">
        <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.75-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/></svg>
        <span>Telegram Updates</span>
      </a>
      <button type="button" class="btn-qr-link" onclick="openQrModal()" title="View Channel QR Code">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1.5"></rect><rect x="14" y="3" width="7" height="7" rx="1.5"></rect><rect x="14" y="14" width="7" height="7" rx="1.5"></rect><rect x="3" y="14" width="7" height="7" rx="1.5"></rect></svg>
        <span>QR Code</span>
      </button>
      <a href="/static/status.html" class="btn-status-link">
        <span>System Status</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
      </a>
    </div>'''

    assert btn_old in content
    content = content.replace(btn_old, btn_new, 1)

    # Footer
    footer_old = '''  <!-- Clean Bottom Footer -->
  <footer class="bottom-bar animate-entry delay-6">
    <span>Need help? <a href="mailto:support@connecto.fun">support@connecto.fun</a></span>
    <span>&bull;</span>
    <a href="https://t.me/connecto" target="_blank" rel="noopener">Telegram Updates</a>
    <span>&bull;</span>
    <span>&copy; 2026 Connecto</span>
  </footer>'''

    footer_new = f'''  <!-- Clean Bottom Footer -->
  <footer class="bottom-bar animate-entry delay-6">
    <span>Need help? <a href="mailto:support@connecto.fun">support@connecto.fun</a></span>
    <span class="bottom-sep">&bull;</span>
    <a href="https://t.me/VCONNECTOFUN" target="_blank" rel="noopener noreferrer" class="footer-telegram" onclick="handleTelegramClick(event)">
      <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.75-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/></svg>
      <span>Telegram Updates (@VCONNECTOFUN)</span>
    </a>
    <span class="bottom-sep">&bull;</span>
    <span>&copy; 2026 Connecto</span>
  </footer>

  <!-- Telegram QR Modal -->
  <div id="telegram-modal" class="modal-overlay" onclick="closeQrModal(event)">
    <div class="modal-card" onclick="event.stopPropagation()">
      <button type="button" class="modal-close-btn" onclick="closeQrModal()" aria-label="Close modal">&times;</button>
      <h3 class="modal-title">Official Telegram Updates</h3>
      <p class="modal-desc">Follow real-time maintenance logs, server ETA alerts, and instant announcements.</p>
      
      <div class="modal-qr-box">
        <img src="data:image/webp;base64,{b64_qr}" alt="Telegram Channel @VCONNECTOFUN" />
      </div>

      <div>
        <div class="modal-badge">
          <span>📢 @VCONNECTOFUN</span>
        </div>
      </div>

      <div class="modal-buttons">
        <a href="https://t.me/VCONNECTOFUN" target="_blank" rel="noopener noreferrer" class="btn-modal-open" onclick="handleTelegramClick(event)">
          <span>Open Telegram Channel</span>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </a>
        <a href="tg://resolve?domain=VCONNECTOFUN" class="btn-modal-app" onclick="showToast('⚡ Connecting in Telegram App...')">
          <span>Connect in Telegram App</span>
        </a>
      </div>
    </div>
  </div>'''

    assert footer_old in content
    content = content.replace(footer_old, footer_new, 1)

    # JavaScript Handlers
    js_code = '''
    function handleTelegramClick(e) {
      showToast('✈️ Opening @VCONNECTOFUN Telegram channel...');
    }

    function openQrModal() {
      const modal = document.getElementById('telegram-modal');
      if (modal) modal.classList.add('active');
    }

    function closeQrModal(e) {
      const modal = document.getElementById('telegram-modal');
      if (modal) modal.classList.remove('active');
    }

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') closeQrModal();
    });

    if (window.location.hash === '#qr' || window.location.hash === '#telegram') {
      setTimeout(openQrModal, 400);
    }
  </script>'''

    assert '  </script>' in content
    content = content.replace('  </script>', js_code, 1)

    # Save to website/static/maintenance.html
    with open('website/static/maintenance.html', 'w', encoding='utf-8') as f:
        f.write(content)

    # Save to website/static/maintenance_classic_3.html
    with open('website/static/maintenance_classic_3.html', 'w', encoding='utf-8') as f:
        f.write(content)

    # Save to backend/app/maintenance.html
    with open('backend/app/maintenance.html', 'w', encoding='utf-8') as f:
        f.write(content)

    # Update cloudflare/worker.js line 5
    with open('cloudflare/worker.js', 'r', encoding='utf-8') as f:
        lines = f.readlines()

    lines[4] = f"const CONNECTO_MAIN_HTML = {json.dumps(content)};\n"
    with open('cloudflare/worker.js', 'w', encoding='utf-8') as f:
        f.writelines(lines)

    print("Successfully built and synchronized all maintenance files and cloudflare/worker.js!")

if __name__ == '__main__':
    run()
