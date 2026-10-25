export const LEARNING_TRACKS = [
  {
    "id": "genin",
    "title": "Genin Track - Foundations",
    "description": "Master core web fundamentals, HTTP/HTTPS protocols, browser inspection, and terminal basics.",
    "icon": "\ud83c\udf31",
    "xp_per_lesson": 50,
    "lessons": [
      {
        "id": "genin-1",
        "title": "HTTP & HTTPS Deep Dive",
        "summary": "Understand request methods, status codes, headers, and TLS/SSL encryption mechanics.",
        "content": "### HTTP & HTTPS Essentials\n\nHTTP (Hypertext Transfer Protocol) is the foundation of data communication on the Web.\n\n* **Methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD.\n* **Status Codes**:\n  * `2xx`: Success (200 OK, 201 Created, 204 No Content)\n  * `3xx`: Redirection (301 Moved Permanently, 302 Found)\n  * `4xx`: Client Error (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found)\n  * `5xx`: Server Error (500 Internal Error, 502 Bad Gateway, 503 Service Unavailable)\n* **HTTPS & TLS**: TLS encrypts the communication channel between client and server using asymmetric cryptography for handshake and symmetric cryptography for session data.",
        "quiz": {
          "question": "Which HTTP status code signifies that a resource has been successfully created on the server?",
          "options": [
            "200 OK",
            "201 Created",
            "204 No Content",
            "301 Moved Permanently"
          ],
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
          "options": [
            "Secure",
            "HttpOnly",
            "SameSite=Strict",
            "Domain"
          ],
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
          "options": [
            "systemctl view service",
            "journalctl -u service -f",
            "cat /var/log/systemd",
            "tail -f /etc/systemd"
          ],
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
    "icon": "\u26a1",
    "xp_per_lesson": 100,
    "lessons": [
      {
        "id": "chunin-1",
        "title": "REST APIs & WebSocket Protocol",
        "summary": "Compare stateless HTTP REST APIs with full-duplex persistent WebSocket streams.",
        "content": "### REST vs WebSockets\n\n* **REST**: Stateless request-response model over HTTP/1.1 or HTTP/2. Ideal for CRUD operations.\n* **WebSocket**: Full-duplex persistent TCP connection initiated via an HTTP Upgrade handshake (`Upgrade: websocket`). Enables sub-millisecond bidirectional event delivery.",
        "quiz": {
          "question": "What HTTP header is sent by a client to request an upgrade to WebSocket?",
          "options": [
            "Connection: Upgrade & Upgrade: websocket",
            "Protocol: WSS",
            "Accept: application/websocket",
            "Transfer-Encoding: chunked"
          ],
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
          "options": [
            "Target",
            "Proxy",
            "Repeater",
            "Intruder"
          ],
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
          "options": [
            "Encrypting the URL parameters",
            "Validating user ownership/permissions for every requested record",
            "Hiding the object ID in the body",
            "Using HTTPS"
          ],
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
    "icon": "\ud83d\udd25",
    "xp_per_lesson": 200,
    "lessons": [
      {
        "id": "jonin-1",
        "title": "WebRTC Architecture & Signaling",
        "summary": "Peer connections, SDP Offer/Answer exchange, and ICE Candidate discovery.",
        "content": "### WebRTC Deep Dive\n\n* **Signaling**: Out-of-band channel (typically WebSocket) to exchange Session Description Protocol (SDP) and ICE candidates.\n* **RTCPeerConnection**: Handles audio, video, and data channels with DTLS-SRTP encryption.\n* **ICE (Interactive Connectivity Establishment)**: Finds optimal direct network paths between peers.",
        "quiz": {
          "question": "What is the role of SDP (Session Description Protocol) in WebRTC?",
          "options": [
            "Audio compression format",
            "Describing media codecs, resolutions, encryption keys, and network parameters",
            "Video rendering engine",
            "Database query protocol"
          ],
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
          "options": [
            "Always for every connection",
            "When both peers are behind restrictive/symmetric NATs or firewalls preventing direct UDP",
            "Only when recording calls",
            "When using IPv6"
          ],
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
          "options": [
            "Requires opening port 80 and 443",
            "Requires NO open inbound ports on the origin firewall",
            "Bypasses all DNS lookups",
            "Disables TLS encryption"
          ],
          "correct": 1,
          "explanation": "Cloudflare Tunnel maintains persistent outbound-only connections to Cloudflare's edge, requiring zero open inbound ports."
        }
      }
    ]
  },
  {
    "id": "soc",
    "title": "Security Labs & SOC Analysis",
    "description": "Hands-on SOC triage, SIEM event correlation, packet forensics, memory injection hunting, phishing analysis, and incident response.",
    "icon": "\ud83d\udee1\ufe0f",
    "xp_per_lesson": 250,
    "lessons": [
      {
        "id": "soc-1",
        "title": "SOC Triage & SIEM Event Correlation",
        "summary": "Master Tier-1 alert triage, Sysmon and Windows Event ID analysis, and SIEM correlation rules.",
        "content": "### SOC Alert Triage Pipeline & Windows Event Forensics\n\nA Security Operations Center (SOC) analyst operates as the first line of defense against cyber threats. The triage pipeline follows five rigorous steps:\n\n1. **Alert Ingestion**: Telemetry aggregated from EDR, firewalls, cloud logs, and identity providers into SIEM (Splunk, Microsoft Sentinel, Elastic).\n2. **False Positive Filtering**: Verifying baseline behavior, authorized administration scripts, and maintenance windows.\n3. **Event Correlation**: Linking disparate telemetry across timestamp, user account, IP address, and parent process.\n4. **Severity Prioritization**: P1 (Critical - Active Ransomware/Data Exfiltration), P2 (High - Lateral Movement/Malware Execution), P3 (Medium - Suspicious Script/Policy Violation), P4 (Low - Recon/Scan).\n5. **Escalation & Containment**: Engaging Tier-2 Incident Response when true malicious intent is verified.\n\n### Critical Windows Security Event IDs\n\n* `4624`: **Successful Logon**\n  * **Logon Type 2**: Interactive (physical keyboard/console logon).\n  * **Logon Type 3**: Network (accessing SMB shares or IIS web servers).\n  * **Logon Type 10**: RemoteInteractive (Remote Desktop Protocol / RDP).\n  * **Logon Type 7**: Unlock (user unlocked existing desktop session).\n* `4625`: **Failed Logon** (Watch `SubStatus 0xC000006A` = Wrong Password, `0xC0000072` = Account Disabled).\n* `4688` & Sysmon Event `1`: **Process Creation** (Captures ParentImage, Image path, CommandLine switches, and Hashes).\n* `7045` & Sysmon Event `6`: **New Service Installed** (Common persistence and privilege escalation vector).\n* Sysmon Event `3`: **Network Connection** (Maps active network sockets back to the originating Process ID).\n\n### Practical SIEM Correlation Query (Splunk SPL)\n\n```spl\nindex=security (EventCode=4625 OR EventCode=4624)\n| eval action=if(EventCode==4624, \"Success\", \"Failure\")\n| stats count(eval(action==\"Failure\")) as fails, count(eval(action==\"Success\")) as successes by TargetUserName, Source_Network_Address\n| where fails > 10 AND successes == 1\n```\n\n*Analysis Note*: More than 10 consecutive failures immediately followed by a single successful logon from an external IP indicates a successful Password Spray or Brute Force attack (MITRE ATT&CK T1110).",
        "quiz": {
          "question": "During an alert triage, you notice Windows Event ID 4624 with Logon Type 10 from external IP 198.51.100.44, followed immediately by Event ID 4688 spawning powershell.exe with an encoded command (-enc). What does this pattern indicate to a SOC analyst?",
          "options": [
            "A standard scheduled task backup running locally on the workstation.",
            "An interactive RDP session initiating suspicious encoded PowerShell execution for potential staging or lateral movement.",
            "A normal domain controller replication event over RPC.",
            "A benign Windows Update service restart."
          ],
          "correct": 1,
          "explanation": "Logon Type 10 specifically designates Remote Desktop Protocol (RDP) logons. Spawning PowerShell with base64 encoded parameters (-enc / -EncodedCommand) right after an RDP connection is a hallmark indicator of an adversary obfuscating commands or downloading second-stage payloads (MITRE ATT&CK T1059.001)."
        }
      },
      {
        "id": "soc-2",
        "title": "Network Packet Forensics & Malicious PCAPs",
        "summary": "Analyze packet captures in Wireshark, detect C2 beaconing, DNS tunneling, and dissect TLS handshakes.",
        "content": "### Deep Packet Inspection & Network Forensics\n\nNetwork forensics captures immutable evidence directly from the wire. When analyzing packet captures (.pcap / .pcapng), analysts look for behavioral indicators of command-and-control (C2) communication, data exfiltration, and lateral scanning.\n\n### Wireshark Protocol Filters for Threat Detection\n\n* `tcp.flags.syn == 1 && tcp.flags.ack == 0`: Identifies SYN port scanning sweeps.\n* `dns.flags.response == 0 && dns.qry.name.len > 45`: Uncovers high-entropy DNS query tunneling.\n* `http.request.method == \"POST\" && (http.request.uri contains \"/gate\" || http.request.uri contains \"/upload\")`: Potential exfiltration / C2 check-in.\n* `tls.handshake.type == 1`: Client Hello inspection for TLS fingerprinting.\n\n### Command & Control (C2) Beaconing Mechanics\n\nMalware frameworks (Cobalt Strike, Sliver, Mythic) communicate with C2 servers using periodic beaconing. SOC analysts detect beaconing by analyzing **Jitter**:\n\n* **Interval**: Fixed timer (e.g. sleep 60 seconds).\n* **Jitter Percentage**: Introduces variance (e.g. 20% jitter = requests arrive every 48s to 72s).\n* High connection count to an unfamiliar external IP with uniform packet sizes is a primary C2 telemetry signature.\n\n### TLS Fingerprinting with JA3 / JA4\n\nEven when traffic is encrypted via HTTPS, the initial unencrypted TLS Client Hello reveals the client's cipher suites, extensions, elliptic curves, and point formats. Hashing these parameters produces a **JA3 Fingerprint**:\n\n```text\nCobalt Strike Default JA3: 72a589da586844d7f0818ce684948eea\nStandard Chrome Browser JA3: b32309a26951912be7dba376398abc3b\n```\n\n### Snort / Suricata Rule for DNS Exfiltration\n\n```snort\nalert dns $HOME_NET any -> $EXTERNAL_NET 53 (\n    msg:\"CONNECTO-SOC: High-Entropy DNS Exfiltration Tunneling Detected\";\n    content:\"|00 01 00 01|\";\n    isdataat:50,relative;\n    classtype:bad-unknown;\n    sid:2000001;\n    rev:1;\n)\n```",
        "quiz": {
          "question": "You are analyzing a PCAP file where an internal host queries thousands of unique, random 60-character subdomains under a single external domain (e.g. a7b91c...exfil.xyz) over UDP port 53. What attack technique is being executed?",
          "options": [
            "DNS Spoofing / Cache Poisoning",
            "DNS Tunneling for data exfiltration or C2 communication",
            "NTP Amplification DDoS attack",
            "ARP Poisoning Man-in-the-Middle"
          ],
          "correct": 1,
          "explanation": "High-entropy, long subdomain strings sent over DNS queries are a classic indicator of DNS Tunneling (MITRE ATT&CK T1071.004). Adversaries use DNS to exfiltrate base64/hex-encoded data or establish C2 communications through recursive DNS resolvers when egress HTTP/HTTPS is firewalled."
        }
      },
      {
        "id": "soc-3",
        "title": "Endpoint Memory Forensics & Process Injection",
        "summary": "Inspect live memory dumps with Volatility 3, detect Process Hollowing, and identify reflective DLL injection.",
        "content": "### Volatility 3 Memory Analysis & Volatile Triage\n\nWhen malware executes entirely in memory without dropping an executable file onto disk (fileless malware), disk forensics reveals nothing. Memory forensics provides complete visibility into running code, unencrypted network buffers, and process injection.\n\n### Core Volatility 3 Investigation Commands\n\n* `vol -f mem.dmp windows.pstree`: Displays the parent-child process execution tree. Alerts on abnormal lineage (e.g., `cmd.exe` spawning from `winword.exe`).\n* `vol -f mem.dmp windows.malfind`: Scans Virtual Address Descriptors (VAD) for memory regions allocated with `PAGE_EXECUTE_READWRITE` (RWX) permissions containing executable code.\n* `vol -f mem.dmp windows.netscan`: Extracts all TCP/UDP endpoints, including sockets closed shortly before memory acquisition.\n* `vol -f mem.dmp windows.cmdline`: Recovers command-line arguments passed to all running and terminated processes.\n\n### Process Injection Techniques\n\n* **Process Hollowing (RunPE)**: Adversary starts a legitimate target process (e.g. `svchost.exe` or `explorer.exe`) in a suspended state (`CREATE_SUSPENDED`), unmaps original code using `NtUnmapViewOfSection`, allocates RWX memory with `VirtualAllocEx`, writes malicious payload with `WriteProcessMemory`, and resumes execution with `ResumeThread`.\n* **DLL Side-Loading**: Exploiting the Windows dynamic link library search order to force a signed, trusted application to load a malicious proxy DLL from the current working directory.\n* **Reflective DLL Injection**: Writing a customized DLL directly into foreign process memory and executing it via an exported reflective loader without registering it in `PEB->Ldr`.\n\n### Forensic Indicators Checklist\n\n1. Legitimate Windows system binaries running from non-standard directories (e.g., `C:\\Users\\Public\\svchost.exe`).\n2. Memory pages marked RWX containing the 'MZ' (0x4D 0x5A) PE header.\n3. Instances of `lsass.exe` possessing unusual open handles or unauthorized debug privileges (`SeDebugPrivilege`).",
        "quiz": {
          "question": "In Volatility 3 windows.malfind output, you find a memory segment inside explorer.exe with protection PAGE_EXECUTE_READWRITE (RWX) and the first two bytes are 4D 5A (MZ header). What does this signify?",
          "options": [
            "A standard Windows explorer shell cache update.",
            "A benign Microsoft .NET CLR just-in-time compilation buffer.",
            "A reflective executable or DLL injected into explorer.exe memory space.",
            "Normal swap file paging activity."
          ],
          "correct": 2,
          "explanation": "Memory pages marked RWX (Read-Write-Execute) with the 'MZ' (0x4D 0x5A) PE magic header inside legitimate host processes like explorer.exe are a smoking gun for Process Injection (MITRE ATT&CK T1055). Legitimate code pages are typically PAGE_EXECUTE_READ to conform to W^X security principles."
        }
      },
      {
        "id": "soc-4",
        "title": "Email Threat Vectors & Phishing Header Analysis",
        "summary": "Inspect email headers, verify SPF, DKIM, DMARC alignment, defang malicious URLs, and triage attachments.",
        "content": "### Email Authentication Triad (SPF, DKIM, DMARC)\n\nEmail remains the primary initial access vector (MITRE ATT&CK T1566). Defending against spoofed sender domains requires analyzing email headers:\n\n* **SPF (Sender Policy Framework)**: Validates that the sending mail transfer agent (MTA) IP is authorized in the sender domain's DNS TXT record (`v=spf1 include:_spf.google.com ~all`).\n* **DKIM (DomainKeys Identified Mail)**: Cryptographically verifies email body and header integrity using an RSA/Ed25519 digital signature (`DKIM-Signature:` header) verified against the sender's public key published in DNS.\n* **DMARC (Domain-based Message Authentication, Reporting, and Conformance)**: Enforces domain alignment between the `From:` header and the SPF/DKIM domains:\n  * `p=none`: Monitor only (deliver message despite failures).\n  * `p=quarantine`: Send failed emails to the spam/junk folder.\n  * `p=reject`: Completely block and drop unauthorized emails.\n\n### Dissecting Phishing Email Headers\n\n1. **True Origin IP**: Trace the earliest (bottom-most) `Received: from` header in the message chain.\n2. **Return-Path vs From**: Adversaries often spoof the visual `From:` display name while the underlying envelope sender (`Return-Path`) points to an attacker-controlled relay.\n3. **Homograph / Punycode Attacks**: Using Cyrillic or Unicode characters that visually mimic ASCII characters (e.g., `xn--c1yn-qqa.fun`).\n\n### SOC Analyst Phishing Triage Checklist\n\n* **URL Defanging**: Always defang malicious links before logging in tickets: `hxxps[://]evil[.]com/login`.\n* **Attachment Triage**: Analyze macros using `olevba` to detect `AutoOpen()`, `Shell()`, or `WScript.Shell` execution.\n* **Header Extraction**: Confirm `Authentication-Results:` for `spf=pass`, `dkim=pass`, and `dmarc=pass`.",
        "quiz": {
          "question": "You receive an alert for a suspicious email. The header shows: From: IT Support <helpdesk@paypa1.com>, Return-Path: <attacker@mailrelay-node8.ru>, and Authentication-Results: dkim=fail dmarc=fail (p=none). Why did the email still reach the user's inbox?",
          "options": [
            "Because the email was encrypted with PGP.",
            "Because the domain's DMARC policy is configured with p=none (monitor-only), preventing mail servers from rejecting or quarantining failures.",
            "Because Return-Path overrides all authentication failures.",
            "Because the display name matched internal company support."
          ],
          "correct": 1,
          "explanation": "A DMARC policy of p=none indicates monitoring mode only. Even when DKIM and SPF checks fail, receiving mail gateways will not reject or quarantine the message, allowing spoofed phishing emails to land in recipient inboxes."
        }
      },
      {
        "id": "soc-5",
        "title": "Web Attack Investigation & WAF Defense",
        "summary": "Dissect SQLi, SSRF, XSS, and Path Traversal in web server logs and configure Cloudflare WAF rules.",
        "content": "### Investigating Web Exploitation in Access Logs\n\nWeb application attacks leave distinct audit trails in server access logs and WAF telemetry. A SOC analyst must quickly recognize attack payloads:\n\n* **SQL Injection (SQLi)**:\n  `GET /api/user?id=1%20UNION%20SELECT%20username,password_hash%20FROM%20users--`\n  *Look for*: Status 200 with an unusually large response byte size compared to baseline.\n* **Server-Side Request Forgery (SSRF)**:\n  `POST /api/preview?url=http://169.254.169.254/latest/meta-data/identity-credentials/`\n  *Look for*: Outbound HTTP connections to `169.254.169.254` (cloud metadata service) or RFC 1918 private IP subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`).\n* **Directory Path Traversal / LFI**:\n  `GET /download?file=../../../../../../etc/passwd%00`\n* **Command Injection**:\n  `POST /ping?target=127.0.0.1;cat${IFS}/etc/shadow`\n\n### Cloudflare WAF Custom Rule Deployment\n\nTo neutralize zero-day web exploits in real-time, analysts craft Cloudflare WAF rules in Cloudflare Expression Language:\n\n```text\n(http.request.uri.path contains \"/api/\" and (\n    http.request.uri.query contains \"UNION SELECT\" or\n    http.request.uri.query contains \"169.254.169.254\" or\n    http.request.uri.query contains \"/etc/passwd\"\n))\nAction: Block (HTTP 403)\n```\n\n### Incident Response Triage Steps\n\n1. Block the attacker IP / ASN at the edge WAF.\n2. Query database logs for executed statements within the incident timestamp window.\n3. Verify whether sensitive tables or authentication sessions were compromised.",
        "quiz": {
          "question": "A web application log shows an external request: POST /api/preview?url=http://169.254.169.254/latest/meta-data/identity-credentials/ returning HTTP 200 OK with 1,420 bytes. What vulnerability was exploited and what is the immediate consequence?",
          "options": [
            "Cross-Site Scripting (XSS); the attacker stole user session cookies.",
            "Server-Side Request Forgery (SSRF); the server fetched cloud instance metadata, exposing temporary IAM role credentials.",
            "Reflected Denial of Service; the server crashed due to buffer overflow.",
            "SQL Injection; the attacker dropped the production database."
          ],
          "correct": 1,
          "explanation": "169.254.169.254 is the link-local Cloud Instance Metadata Service (IMDS) address in AWS/GCP/Azure. Exploiting an SSRF vulnerability allows an adversary to force the web application to query its own internal metadata service, harvesting cloud credentials (MITRE ATT&CK T1552.005)."
        }
      },
      {
        "id": "soc-6",
        "title": "Incident Response & Threat Containment",
        "summary": "Execute the 6-phase NIST incident response cycle during active ransomware and credential compromise.",
        "content": "### NIST SP 800-61r2 6-Phase Incident Response Lifecycle\n\nWhen a critical breach occurs, ad-hoc reactions lead to evidence destruction and expanded blast radius. The NIST Incident Response Framework standardizes the workflow:\n\n```text\n+---------------+     +---------------------+     +---------------+\n| 1. PREPARATION| --> | 2. DETECTION & ANL. | --> | 3. CONTAINMENT|\n+---------------+     +---------------------+     +---------------+\n                                                          |\n+---------------+     +---------------------+             v\n| 6. POST-INC.  | <-- | 5. RECOVERY         | <-- +---------------+\n| (Lessons Lrn) |     | (Restore & Monitor) |     | 4. ERADICATION|\n+---------------+     +---------------------+     +---------------+\n```\n\n### Detailed Phase Breakdown\n\n1. **Preparation**: Centralized immutable logging, offline immutable backups, defined playbooks, out-of-band communication channels (Connecto E2EE lounges).\n2. **Detection & Analysis**: Validating alerts, identifying attack vectors, calculating scope, and determining severity level.\n3. **Containment**:\n   * **Short-Term**: Network isolation of compromised host via EDR agent or managed switch VLAN isolation. Terminate active remote sessions (`all_before_{user_id}`).\n   * **Long-Term**: Snapshot virtual disks and dump volatile RAM *before* system shutdown to preserve volatile forensic evidence.\n4. **Eradication**: Identify all persistence mechanisms (scheduled tasks, run keys, WMI subscriptions, backdoors), clean malware binaries, and rotate all compromised enterprise credentials.\n5. **Recovery**: Restore operations from clean, validated backups, rebuild hosts from hardened gold images, and enforce enhanced telemetry monitoring for 30 days.\n6. **Post-Incident Activity**: Complete Root Cause Analysis (RCA), document timeline, calculate financial/data impact, and remediate structural security gaps.",
        "quiz": {
          "question": "During an active incident where a workstation is observed encrypting network shares, which action should a SOC incident responder take FIRST?",
          "options": [
            "Immediately power off the physical machine by pulling the power plug.",
            "Isolate the host from the network (via EDR or switch port disable) and preserve memory/disk state for forensic analysis.",
            "Email all company employees to warn them.",
            "Reformat the hard drive and reinstall the operating system immediately."
          ],
          "correct": 1,
          "explanation": "Immediate network isolation halts lateral movement and ransomware spread to connected network shares while preserving ephemeral RAM artifacts (encryption keys, active processes, C2 sockets). Powering off destroys volatile RAM evidence, while reformatting prevents root cause discovery."
        }
      }
    ]
  }
];
