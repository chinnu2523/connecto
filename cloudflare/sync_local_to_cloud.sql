INSERT OR REPLACE INTO servers (id, name, icon_url, owner_id, created_at) VALUES ('srv_connecto', 'Connecto Official Community', 'https://connecto.fun/uploads/avatars/connecto_logo.webp', 'f7b340a5-316f-44e6-8b16-3c1ce9d2de30', datetime('now'));
INSERT OR REPLACE INTO course_tracks (id, title, description, icon, created_at) VALUES ('genin', 'Genin Track - Foundations', 'Master core web fundamentals, HTTP/HTTPS protocols, browser inspection, and terminal basics.', '🌱', datetime('now'));
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('genin-1', 'genin', 'HTTP & HTTPS Deep Dive', '### HTTP & HTTPS Essentials

HTTP (Hypertext Transfer Protocol) is the foundation of data communication on the Web.

* **Methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD.
* **Status Codes**:
  * `2xx`: Success (200 OK, 201 Created, 204 No Content)
  * `3xx`: Redirection (301 Moved Permanently, 302 Found)
  * `4xx`: Client Error (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found)
  * `5xx`: Server Error (500 Internal Error, 502 Bad Gateway, 503 Service Unavailable)
* **HTTPS & TLS**: TLS encrypts the communication channel between client and server using asymmetric cryptography for handshake and symmetric cryptography for session data.', 50, 0);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_genin-1', 'genin-1', 'Which HTTP status code signifies that a resource has been successfully created on the server?', '["200 OK", "201 Created", "204 No Content", "301 Moved Permanently"]', 1, '201 Created indicates that the request succeeded and a new resource was created as a result.');
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('genin-2', 'genin', 'Browser DevTools Mastery', '### Browser DevTools Guide

Modern browsers feature powerful developer tooling:

* **Network Tab**: Inspect request headers, response bodies, timings (TTFB, DNS, SSL), and WebSocket frames.
* **Console**: Execute JavaScript, inspect exceptions, and monitor CSP violations.
* **Application / Storage Tab**: Inspect cookies (HttpOnly, Secure, SameSite), LocalStorage, SessionStorage, and IndexedDB.', 50, 1);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_genin-2', 'genin-2', 'Which cookie attribute prevents client-side JavaScript from accessing the cookie via document.cookie?', '["Secure", "HttpOnly", "SameSite=Strict", "Domain"]', 1, 'The HttpOnly flag prevents scripts from reading cookies, mitigating XSS token theft.');
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('genin-3', 'genin', 'Linux Terminal & Process Control', '### Linux Essentials for Developers

* **Service Management**: `systemctl status`, `systemctl restart`, `journalctl -u <service> -f`.
* **Process Inspection**: `ps aux`, `top`, `htop`, `kill -9 <PID>`.
* **Networking Tools**: `curl -I`, `dig`, `nc -zv`, `netstat`/`ss -tulpn`.', 50, 2);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_genin-3', 'genin-3', 'Which command streams real-time logs for a systemd service unit?', '["systemctl view service", "journalctl -u service -f", "cat /var/log/systemd", "tail -f /etc/systemd"]', 1, 'journalctl -u <service> -f follows the journal logs in real-time.');
INSERT OR REPLACE INTO course_tracks (id, title, description, icon, created_at) VALUES ('chunin', 'Chunin Track - Web & Security Fundamentals', 'Learn REST API design, WebSocket streaming, Burp Suite proxy testing, and OWASP Top 10 vulnerabilities.', '⚡', datetime('now'));
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('chunin-1', 'chunin', 'REST APIs & WebSocket Protocol', '### REST vs WebSockets

* **REST**: Stateless request-response model over HTTP/1.1 or HTTP/2. Ideal for CRUD operations.
* **WebSocket**: Full-duplex persistent TCP connection initiated via an HTTP Upgrade handshake (`Upgrade: websocket`). Enables sub-millisecond bidirectional event delivery.', 100, 0);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_chunin-1', 'chunin-1', 'What HTTP header is sent by a client to request an upgrade to WebSocket?', '["Connection: Upgrade & Upgrade: websocket", "Protocol: WSS", "Accept: application/websocket", "Transfer-Encoding: chunked"]', 0, 'WebSockets initiate via an HTTP 101 Switching Protocols with Connection: Upgrade and Upgrade: websocket headers.');
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('chunin-2', 'chunin', 'Burp Suite & Proxy Interception', '### Interception Proxy Mechanics

* **HTTP Proxy**: Intercepts requests between the client and server.
* **Repeater**: Manually modify headers/parameters and resend to analyze origin responses.
* **WebSocket History**: View live incoming and outgoing frames with raw hex/text inspection.', 100, 1);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_chunin-2', 'chunin-2', 'In Burp Suite, which tab is dedicated to modifying and re-sending individual HTTP requests?', '["Target", "Proxy", "Repeater", "Intruder"]', 2, 'Repeater is designed for crafting, tweaking, and re-executing individual requests.');
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('chunin-3', 'chunin', 'OWASP Top 10 & API Security', '### OWASP Top 10 Defenses

* **BOLA / IDOR**: Always validate that the authenticated user owns the requested object ID.
* **XSS (Cross-Site Scripting)**: Sanitize user input and enforce a strict Content Security Policy (CSP).
* **CSRF**: Use Anti-CSRF tokens and `SameSite=Lax` / `SameSite=Strict` cookie flags.', 100, 2);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_chunin-3', 'chunin-3', 'What is the primary countermeasure against Broken Object Level Authorization (BOLA)?', '["Encrypting the URL parameters", "Validating user ownership/permissions for every requested record", "Hiding the object ID in the body", "Using HTTPS"]', 1, 'Every request referencing a resource ID must verify server-side that the requester has access rights to that specific entity.');
INSERT OR REPLACE INTO course_tracks (id, title, description, icon, created_at) VALUES ('jonin', 'Jonin Track - Advanced Architecture & WebRTC', 'Master WebRTC peer-to-peer media, STUN/TURN traversal, Cloudflare Tunnels, and resilient infrastructure.', '🔥', datetime('now'));
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('jonin-1', 'jonin', 'WebRTC Architecture & Signaling', '### WebRTC Deep Dive

* **Signaling**: Out-of-band channel (typically WebSocket) to exchange Session Description Protocol (SDP) and ICE candidates.
* **RTCPeerConnection**: Handles audio, video, and data channels with DTLS-SRTP encryption.
* **ICE (Interactive Connectivity Establishment)**: Finds optimal direct network paths between peers.', 200, 0);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_jonin-1', 'jonin-1', 'What is the role of SDP (Session Description Protocol) in WebRTC?', '["Audio compression format", "Describing media codecs, resolutions, encryption keys, and network parameters", "Video rendering engine", "Database query protocol"]', 1, 'SDP describes media capabilities, supported codecs, and transport parameters exchanged during peer negotiation.');
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('jonin-2', 'jonin', 'NAT Traversal: STUN vs TURN', '### STUN vs TURN

* **STUN (Session Traversal Utilities for NAT)**: Discovers public IP:Port mapping. Lightweight, direct peer-to-peer.
* **TURN (Traversal Using Relays around NAT)**: Relays media packets through a cloud relay when symmetric NATs prevent direct P2P connections.', 200, 1);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_jonin-2', 'jonin-2', 'When is a TURN server required in WebRTC?', '["Always for every connection", "When both peers are behind restrictive/symmetric NATs or firewalls preventing direct UDP", "Only when recording calls", "When using IPv6"]', 1, 'TURN acts as a fallback relay when symmetric NATs or strict enterprise firewalls block direct peer-to-peer hole punching.');
INSERT OR REPLACE INTO lessons (id, track_id, title, content_markdown, xp_reward, order_index) VALUES ('jonin-3', 'jonin', 'Zero Trust Edge & Cloudflare Tunnels', '### Cloudflare Tunnel Architecture

* **cloudflared**: Establishes four outbound-only QUIC/HTTP2 tunnels to nearby Cloudflare edge POPs.
* **Zero Open Inbound Ports**: Inbound firewall rules can block all public WAN traffic (e.g. port 80/443), eliminating direct DDoS and port scanning exposure.
* **Edge TLS Termination**: Cloudflare automatically terminates SSL certificates and inspects traffic at the edge.', 200, 2);
INSERT OR REPLACE INTO quiz_questions (id, lesson_id, question_text, options, correct_option_index, explanation) VALUES ('quiz_jonin-3', 'jonin-3', 'What is the key security advantage of Cloudflare Tunnel over traditional port forwarding?', '["Requires opening port 80 and 443", "Requires NO open inbound ports on the origin firewall", "Bypasses all DNS lookups", "Disables TLS encryption"]', 1, 'Cloudflare Tunnel maintains persistent outbound-only connections to Cloudflare''s edge, requiring zero open inbound ports.');
INSERT OR REPLACE INTO job_openings (id, title, department, location, description, required_keywords, created_at) VALUES ('aa923b60-fefe-4cb5-ad33-0f6b2895be0e', 'Application Security (AppSec) Engineer', 'Cybersecurity', 'Remote', 'Lead application security audits, penetration testing, code reviews, and OWASP vulnerability mitigations.', '["python", "owasp", "sql", "docker", "pentesting", "linux"]', '2026-09-10 18:00:35.880143');
INSERT OR REPLACE INTO job_openings (id, title, department, location, description, required_keywords, created_at) VALUES ('5386d09c-0a33-49cf-87b0-0cac415b89ed', 'Threat Hunter & Incident Responder', 'Defensive Operations', 'Hybrid / On-site', 'Monitor SOC telemetry, perform packet inspection, analyze malware payloads, and conduct incident response.', '["nmap", "wireshark", "python", "linux", "splunk", "malware"]', '2026-09-10 18:00:35.880148');
INSERT OR REPLACE INTO job_applications (id, job_id, applicant_id, full_name, email, cv_file_url, parsed_text, ai_score, detected_skills, summary, created_at) VALUES ('de5e258e-b102-4ab9-adbe-fee3a89cb5bc', 'aa923b60-fefe-4cb5-ad33-0f6b2895be0e', 'ad26278e-55f5-4d55-aed0-1b88692a2e4d', 'Alex Thorne', 'applicant@connecto.dev', '/uploads/cv_resumes/43316a22f96f48288303e1ae152e42b5.txt', 'CURRICULUM VITAE - ALEX THORNE
Cybersecurity Engineer & Developer

EXPERIENCE:
- Application Security Specialist specializing in OWASP Top 10 mitigation.
- Hands-on experience with Python script automation, SQL injection testing, Nmap scanning, and Docker containerization.
- Threat analysis and Linux system administration.', 83, '["python", "owasp", "sql", "docker", "linux"]', 'Candidate matched 5 of 6 required technical skill keywords (83% compatibility index). Detected technical proficiencies: python, owasp, sql, docker, linux.', '2026-09-10 18:00:35.886589');
INSERT OR REPLACE INTO job_applications (id, job_id, applicant_id, full_name, email, cv_file_url, parsed_text, ai_score, detected_skills, summary, created_at) VALUES ('591fa9ef-08b3-42e3-9b60-ae4419bc39d4', 'aa923b60-fefe-4cb5-ad33-0f6b2895be0e', 'ad26278e-55f5-4d55-aed0-1b88692a2e4d', 'Alex Thorne', 'applicant@connecto.dev', '/uploads/cv_resumes/60a25b194e5a473fb33fca922bb0a538.txt', 'CURRICULUM VITAE - ALEX THORNE
Cybersecurity Engineer & Developer

EXPERIENCE:
- Application Security Specialist specializing in OWASP Top 10 mitigation.
- Hands-on experience with Python script automation, SQL injection testing, Nmap scanning, and Docker containerization.
- Threat analysis and Linux system administration.', 83, '["python", "owasp", "sql", "docker", "linux"]', 'Candidate matched 5 of 6 required technical skill keywords (83% compatibility index). Detected technical proficiencies: python, owasp, sql, docker, linux.', '2026-09-10 18:00:35.901116');
INSERT OR REPLACE INTO job_applications (id, job_id, applicant_id, full_name, email, cv_file_url, parsed_text, ai_score, detected_skills, summary, created_at) VALUES ('86dcd4d2-f6e2-4500-8fcb-60ad6d78aae0', 'aa923b60-fefe-4cb5-ad33-0f6b2895be0e', 'ad26278e-55f5-4d55-aed0-1b88692a2e4d', 'Alex Thorne', 'applicant@connecto.dev', '/uploads/cv_resumes/bba658860a6f4d0384776ebe683cf25c.txt', 'CURRICULUM VITAE - ALEX THORNE
Cybersecurity Engineer & Developer

EXPERIENCE:
- Application Security Specialist specializing in OWASP Top 10 mitigation.
- Hands-on experience with Python script automation, SQL injection testing, Nmap scanning, and Docker containerization.
- Threat analysis and Linux system administration.', 83, '["python", "owasp", "sql", "docker", "linux"]', 'Candidate matched 5 of 6 required technical skill keywords (83% compatibility index). Detected technical proficiencies: python, owasp, sql, docker, linux.', '2026-09-10 18:00:35.909191');
INSERT OR REPLACE INTO dm_participants (channel_id, user_id, created_at) VALUES ('d2ab6854-b7da-4013-ae18-2ca1f5e4ebe5', '7c5d9c2b-8f4f-44e0-a4f0-1b056844e470', '2026-09-11 06:55:36.600356');
INSERT OR REPLACE INTO dm_participants (channel_id, user_id, created_at) VALUES ('d2ab6854-b7da-4013-ae18-2ca1f5e4ebe5', '30dfca4c-861c-4232-b9ea-440717932ff8', '2026-09-11 06:55:36.600358');
INSERT OR REPLACE INTO users (id, username, display_name, email, password_hash, avatar_url, bio, username_changed, is_recruiter, is_admin, is_stealth, is_online, phone_number, two_factor_enabled, two_factor_method, created_at, updated_at) VALUES ('usr_madara', 'vance', 'Vance Sterling', 'vance@connecto.fun', '=19=65536,t=3,p=4+yGeQg/vYigpvy8aJQ1A6XDc1aE', '🔥', 'Developer and architect.', 0, 0, 1, 0, 1, '+919876543210', 0, 'email', datetime('now'), datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_madara', 'member', datetime('now'));
INSERT OR REPLACE INTO users (id, username, display_name, email, password_hash, avatar_url, bio, username_changed, is_recruiter, is_admin, is_stealth, is_online, phone_number, two_factor_enabled, two_factor_method, created_at, updated_at) VALUES ('usr_sakura', 'nova', 'Nova Ray', 'nova@connecto.fun', '=19=65536,t=3,p=4/gQ+MVqg5brH/MOTXndswCIS7xUTa0R+0uFg', '🌸', 'Community Specialist', 0, 0, 0, 0, 1, NULL, 0, 'email', datetime('now'), datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_sakura', 'member', datetime('now'));
INSERT OR REPLACE INTO users (id, username, display_name, email, password_hash, avatar_url, bio, username_changed, is_recruiter, is_admin, is_stealth, is_online, phone_number, two_factor_enabled, two_factor_method, created_at, updated_at) VALUES ('usr_naruto', 'phoenix', 'Phoenix Cole', 'phoenix@connecto.fun', '=19=65536,t=3,p=4/gQ+MVqg5brH/MOTXndswCIS7xUTa0R+0uFg', '🍃', 'Full-stack Engineer', 0, 0, 0, 0, 1, NULL, 0, 'email', datetime('now'), datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_naruto', 'member', datetime('now'));
INSERT OR REPLACE INTO users (id, username, display_name, email, password_hash, avatar_url, bio, username_changed, is_recruiter, is_admin, is_stealth, is_online, phone_number, two_factor_enabled, two_factor_method, created_at, updated_at) VALUES ('usr_sasuke', 'cipher', 'Cipher Kage', 'cipher@connecto.fun', '=19=65536,t=3,p=4/gQ+MVqg5brH/MOTXndswCIS7xUTa0R+0uFg', '⚡', 'Security & Crypto Engineer', 0, 0, 0, 0, 1, NULL, 0, 'email', datetime('now'), datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_sasuke', 'member', datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'f7b340a5-316f-44e6-8b16-3c1ce9d2de30', 'member', datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', '5d0917ac-6652-4050-80e1-6e16e367ef04', 'member', datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_chinnu', 'member', datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_vivek', 'member', datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'b569bade-fb1e-41e6-869d-117297f7279f', 'member', datetime('now'));
INSERT OR REPLACE INTO server_members (server_id, user_id, role, joined_at) VALUES ('srv_connecto', 'usr_test_user', 'member', datetime('now'));
