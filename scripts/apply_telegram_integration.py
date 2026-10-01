import base64
import json
import re

def run():
    with open('website/static/img/telegram_channel_card.webp', 'rb') as f:
        b64_qr = base64.b64encode(f.read()).decode('utf-8')

    with open('website/static/maintenance.html', 'r', encoding='utf-8') as f:
        html = f.read()

    # Update embedded WebP image
    html = re.sub(r'src="data:image/webp;base64,[^"]+"', f'src="data:image/webp;base64,{b64_qr}"', html)

    # Ensure branding is connecto-fun
    html = html.replace('Telegram Updates (@VCONNECTOFUN)', 'Telegram Updates (connecto-fun)')
    html = html.replace('📢 @VCONNECTOFUN', '📢 connecto-fun')
    html = html.replace('alt="Telegram Channel @VCONNECTOFUN"', 'alt="Telegram Channel connecto-fun"')
    html = html.replace('Opening @VCONNECTOFUN Telegram channel...', 'Opening connecto-fun on Telegram...')

    for path in ['website/static/maintenance.html', 'website/static/maintenance_classic_3.html', 'backend/app/maintenance.html']:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(html)
        print(f"Updated {path}")

    with open('cloudflare/worker.js', 'r', encoding='utf-8') as f:
        lines = f.readlines()
    lines[4] = f"const CONNECTO_MAIN_HTML = {json.dumps(html)};\n"
    with open('cloudflare/worker.js', 'w', encoding='utf-8') as f:
        f.writelines(lines)
    print("Updated cloudflare/worker.js successfully!")

if __name__ == '__main__':
    run()
