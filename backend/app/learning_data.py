LEARNING_TRACKS = [
    {
        "id": "genin",
        "title": "Genin Track - Foundations",
        "description": "Master core web fundamentals, HTTP/HTTPS protocols, browser inspection, and terminal basics.",
        "icon": "🌱",
        "xp_per_lesson": 50,
        "lessons": [
            {
                "id": "genin-1",
                "title": "HTTP & HTTPS Deep Dive",
                "summary": "Understand request methods, status codes, headers, and TLS/SSL encryption mechanics.",
                "content": "### HTTP & HTTPS Essentials\n\nHTTP (Hypertext Transfer Protocol) is the foundation of data communication on the Web.\n\n* **Methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD.\n* **Status Codes**:\n  * `2xx`: Success (200 OK, 201 Created, 204 No Content)\n  * `3xx`: Redirection (301 Moved Permanently, 302 Found)\n  * `4xx`: Client Error (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found)\n  * `5xx`: Server Error (500 Internal Error, 502 Bad Gateway, 503 Service Unavailable)\n* **HTTPS & TLS**: TLS encrypts the communication channel between client and server using asymmetric cryptography for handshake and symmetric cryptography for session data.",
                "quiz": {
                    "question": "Which HTTP status code signifies that a resource has been successfully created on the server?",
                    "options": ["200 OK", "201 Created", "204 No Content", "301 Moved Permanently"],
                    "correct": 1,
                    "explanation": "201 Created indicates that the request succeeded and a new resource was created as a result."
                }
            },
            {
                "id": "genin-2",
                "title": "Browser DevTools Mastery",
                "summary": "Inspect DOM elements, debug console logs, monitor network payloads, and analyze storage.",
                "content": "### Browser DevTools Guide\n\nModern browsers feature powerful developer tooling:\n\n* **Network Tab**: Inspect request headers, response bodies, timings (TTFB, DNS, SSL), and WebSocket frames.\n* **Console**: Execute JavaScript, inspect exceptions, and monitor CSP violations.\n* **Application / Storage Tab**: Inspect cookies (HttpOnly, Secure, SameSite), LocalStorage, SessionStorage, and IndexedDB.",
                "quiz": {
                    "question": "Which cookie attribute prevents client-side JavaScript from accessing the cookie via document.cookie?",
                    "options": ["Secure", "HttpOnly", "SameSite=Strict", "Domain"],
                    "correct": 1,
                    "explanation": "The HttpOnly flag prevents scripts from reading cookies, mitigating XSS token theft."
                }
            },
            {
                "id": "genin-3",
                "title": "Linux Terminal & Process Control",
                "summary": "Master bash navigation, systemd services, process management, and permissions.",
                "content": "### Linux Essentials for Developers\n\n* **Service Management**: `systemctl status`, `systemctl restart`, `journalctl -u <service> -f`.\n* **Process Inspection**: `ps aux`, `top`, `htop`, `kill -9 <PID>`.\n* **Networking Tools**: `curl -I`, `dig`, `nc -zv`, `netstat`/`ss -tulpn`.",
                "quiz": {
                    "question": "Which command streams real-time logs for a systemd service unit?",
                    "options": ["systemctl view service", "journalctl -u service -f", "cat /var/log/systemd", "tail -f /etc/systemd"],
                    "correct": 1,
                    "explanation": "journalctl -u <service> -f follows the journal logs in real-time."
                }
            }
        ]
    },
    {
        "id": "chunin",
        "title": "Chunin Track - Web & Security Fundamentals",
        "description": "Learn REST API design, WebSocket streaming, Burp Suite proxy testing, and OWASP Top 10 vulnerabilities.",
        "icon": "⚡",
        "xp_per_lesson": 100,
        "lessons": [
            {
                "id": "chunin-1",
                "title": "REST APIs & WebSocket Protocol",
                "summary": "Compare stateless HTTP REST APIs with full-duplex persistent WebSocket streams.",
                "content": "### REST vs WebSockets\n\n* **REST**: Stateless request-response model over HTTP/1.1 or HTTP/2. Ideal for CRUD operations.\n* **WebSocket**: Full-duplex persistent TCP connection initiated via an HTTP Upgrade handshake (`Upgrade: websocket`). Enables sub-millisecond bidirectional event delivery.",
                "quiz": {
                    "question": "What HTTP header is sent by a client to request an upgrade to WebSocket?",
                    "options": ["Connection: Upgrade & Upgrade: websocket", "Protocol: WSS", "Accept: application/websocket", "Transfer-Encoding: chunked"],
                    "correct": 0,
                    "explanation": "WebSockets initiate via an HTTP 101 Switching Protocols with Connection: Upgrade and Upgrade: websocket headers."
                }
            },
            {
                "id": "chunin-2",
                "title": "Burp Suite & Proxy Interception",
                "summary": "Intercept HTTP/HTTPS traffic, modify requests in Repeater, and inspect WebSocket frames.",
                "content": "### Interception Proxy Mechanics\n\n* **HTTP Proxy**: Intercepts requests between the client and server.\n* **Repeater**: Manually modify headers/parameters and resend to analyze origin responses.\n* **WebSocket History**: View live incoming and outgoing frames with raw hex/text inspection.",
                "quiz": {
                    "question": "In Burp Suite, which tab is dedicated to modifying and re-sending individual HTTP requests?",
                    "options": ["Target", "Proxy", "Repeater", "Intruder"],
                    "correct": 2,
                    "explanation": "Repeater is designed for crafting, tweaking, and re-executing individual requests."
                }
            },
            {
                "id": "chunin-3",
                "title": "OWASP Top 10 & API Security",
                "summary": "Understand Broken Object Level Authorization (BOLA), SQL Injection, XSS, and CSRF prevention.",
                "content": "### OWASP Top 10 Defenses\n\n* **BOLA / IDOR**: Always validate that the authenticated user owns the requested object ID.\n* **XSS (Cross-Site Scripting)**: Sanitize user input and enforce a strict Content Security Policy (CSP).\n* **CSRF**: Use Anti-CSRF tokens and `SameSite=Lax` / `SameSite=Strict` cookie flags.",
                "quiz": {
                    "question": "What is the primary countermeasure against Broken Object Level Authorization (BOLA)?",
                    "options": ["Encrypting the URL parameters", "Validating user ownership/permissions for every requested record", "Hiding the object ID in the body", "Using HTTPS"],
                    "correct": 1,
                    "explanation": "Every request referencing a resource ID must verify server-side that the requester has access rights to that specific entity."
                }
            }
        ]
    },
    {
        "id": "jonin",
        "title": "Jonin Track - Advanced Architecture & WebRTC",
        "description": "Master WebRTC peer-to-peer media, STUN/TURN traversal, Cloudflare Tunnels, and resilient infrastructure.",
        "icon": "🔥",
        "xp_per_lesson": 200,
        "lessons": [
            {
                "id": "jonin-1",
                "title": "WebRTC Architecture & Signaling",
                "summary": "Peer connections, SDP Offer/Answer exchange, and ICE Candidate discovery.",
                "content": "### WebRTC Deep Dive\n\n* **Signaling**: Out-of-band channel (typically WebSocket) to exchange Session Description Protocol (SDP) and ICE candidates.\n* **RTCPeerConnection**: Handles audio, video, and data channels with DTLS-SRTP encryption.\n* **ICE (Interactive Connectivity Establishment)**: Finds optimal direct network paths between peers.",
                "quiz": {
                    "question": "What is the role of SDP (Session Description Protocol) in WebRTC?",
                    "options": ["Audio compression format", "Describing media codecs, resolutions, encryption keys, and network parameters", "Video rendering engine", "Database query protocol"],
                    "correct": 1,
                    "explanation": "SDP describes media capabilities, supported codecs, and transport parameters exchanged during peer negotiation."
                }
            },
            {
                "id": "jonin-2",
                "title": "NAT Traversal: STUN vs TURN",
                "summary": "Understand why symmetric NATs require TURN relay servers for guaranteed call connectivity.",
                "content": "### STUN vs TURN\n\n* **STUN (Session Traversal Utilities for NAT)**: Discovers public IP:Port mapping. Lightweight, direct peer-to-peer.\n* **TURN (Traversal Using Relays around NAT)**: Relays media packets through a cloud relay when symmetric NATs prevent direct P2P connections.",
                "quiz": {
                    "question": "When is a TURN server required in WebRTC?",
                    "options": ["Always for every connection", "When both peers are behind restrictive/symmetric NATs or firewalls preventing direct UDP", "Only when recording calls", "When using IPv6"],
                    "correct": 1,
                    "explanation": "TURN acts as a fallback relay when symmetric NATs or strict enterprise firewalls block direct peer-to-peer hole punching."
                }
            },
            {
                "id": "jonin-3",
                "title": "Zero Trust Edge & Cloudflare Tunnels",
                "summary": "Expose internal origins securely without opening public inbound firewall ports.",
                "content": "### Cloudflare Tunnel Architecture\n\n* **cloudflared**: Establishes four outbound-only QUIC/HTTP2 tunnels to nearby Cloudflare edge POPs.\n* **Zero Open Inbound Ports**: Inbound firewall rules can block all public WAN traffic (e.g. port 80/443), eliminating direct DDoS and port scanning exposure.\n* **Edge TLS Termination**: Cloudflare automatically terminates SSL certificates and inspects traffic at the edge.",
                "quiz": {
                    "question": "What is the key security advantage of Cloudflare Tunnel over traditional port forwarding?",
                    "options": ["Requires opening port 80 and 443", "Requires NO open inbound ports on the origin firewall", "Bypasses all DNS lookups", "Disables TLS encryption"],
                    "correct": 1,
                    "explanation": "Cloudflare Tunnel maintains persistent outbound-only connections to Cloudflare's edge, requiring zero open inbound ports."
                }
            }
        ]
    }
]
