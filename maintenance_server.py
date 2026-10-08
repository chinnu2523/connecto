#!/usr/bin/env python3
import http.server
import socketserver
import os
import mimetypes
import json

PORT = 8080
STATIC_DIR = "/Users/madarauchiha/connecto/website/static"
MAINTENANCE_HTML = os.path.join(STATIC_DIR, "maintenance.html")

class MaintenanceHandler(http.server.BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.handle_request(is_head=True)

    def do_GET(self):
        self.handle_request(is_head=False)

    def handle_request(self, is_head=False):
        path = self.path.split("?")[0]

        # 1. API Health & Status endpoints
        if path in ["/api/health", "/api/v1/health", "/api/status", "/api/v1/status", "/api/members"]:
            self.send_response(503)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.send_header("Retry-After", "300")
            self.end_headers()
            if not is_head:
                payload = {
                    "status": "maintenance",
                    "server": "connecto_edge",
                    "mode": "maintenance",
                    "message": "Connecto servers are currently undergoing maintenance.",
                    "maintenance": True
                }
                self.wfile.write(json.dumps(payload).encode("utf-8"))
            return

        if path in ["/api/server-mode", "/api/v1/system/mode"]:
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            if not is_head:
                payload = {
                    "active_server": "maintenance",
                    "origin_status": "MAINTENANCE",
                    "cloud_edge": "active",
                    "automation": "active"
                }
                self.wfile.write(json.dumps(payload).encode("utf-8"))
            return

        # 2. APK Download Endpoints
        if path in ["/download", "/download/apk", "/connecto-fun.apk", "/downloads/connecto-fun.apk"]:
            self.send_response(302)
            self.send_header("Location", "https://github.com/chinnu2523/connecto/releases/download/v4.2.4/connecto-fun.apk")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            return

        # 3. Static Files (CSS, JS, images, icons)
        local_file = os.path.join(STATIC_DIR, path.lstrip("/"))
        if os.path.isfile(local_file) and not path.endswith(".html"):
            mime_type, _ = mimetypes.guess_type(local_file)
            mime_type = mime_type or "application/octet-stream"
            try:
                with open(local_file, "rb") as f:
                    content = f.read()
                self.send_response(200)
                self.send_header("Content-Type", mime_type)
                self.send_header("Content-Length", str(len(content)))
                self.send_header("Cache-Control", "public, max-age=3600")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                if not is_head:
                    self.wfile.write(content)
                return
            except Exception:
                pass

        # 4. Fallback / Default: Official Connecto Maintenance Page
        try:
            with open(MAINTENANCE_HTML, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
            self.send_header("Retry-After", "300")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            if not is_head:
                self.wfile.write(content)
        except Exception as e:
            self.send_response(500)
            self.end_headers()
            self.wfile.write(f"Maintenance page error: {e}".encode("utf-8"))

    def log_message(self, format, *args):
        # Concise logging
        pass

if __name__ == "__main__":
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), MaintenanceHandler) as httpd:
        print(f"Connecto Maintenance Edge Server running on port {PORT}...")
        httpd.serve_forever()
