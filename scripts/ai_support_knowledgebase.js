    function getAiCustomerSupportResponse(query) {
      const q = (query || "").toLowerCase();
      
      // 1. Security Labs & SOC Analysis (Blue Team, SIEM, Forensics, Incident Response)
      if (q.includes("soc") || q.includes("security lab") || q.includes("threat") || q.includes("siem") || q.includes("incident response") || q.includes("forensic") || q.includes("wireshark") || q.includes("pcap") || q.includes("volatility") || q.includes("process injection") || q.includes("snort") || q.includes("yara") || q.includes("phishing") || q.includes("event id") || q.includes("sysmon") || q.includes("learning hub") || q.includes("academy") || q.includes("quiz") || q.includes("course")) {
        return {
          text: `🛡️ **Hands-On Security Labs & SOC Analysis Suite:**\n\nConnecto features a comprehensive, interactive **Cyber Academy & Security Labs Hub** specifically built to train cyber defenders, blue teamers, and SOC analysts across 6 advanced protocol modules:\n\n• **Lab 1: SOC Alert Triage & SIEM Event Correlation:**\n  * Master Windows Security Event IDs: \`4624\` (Logon Types: 2=Interactive, 3=Network, 10=RDP), \`4625\` (Failed Logons with \`0xC000006A\` wrong password status), \`4688\` / Sysmon Event \`1\` (Process Creation with ParentCommandLine lineage), and \`7045\` (New Service persistence).\n  * Construct production Splunk SPL and KQL correlation rules to catch password spray patterns (\`fails > 10 AND successes == 1\`).\n\n• **Lab 2: Network Packet Forensics & Malicious PCAP Analysis:**\n  * Dissect packet captures (.pcap) in Wireshark with advanced BPF filters (\`dns.flags.response == 0 && dns.qry.name.len > 45\`).\n  * Detect Command-and-Control (C2) beaconing variance and Jitter calculation (Cobalt Strike, Sliver).\n  * Extract JA3/JA4 TLS Client Hello fingerprint hashes to unmask encrypted malware communications, and deploy custom Snort/Suricata intrusion detection signatures.\n\n• **Lab 3: Endpoint Memory Forensics & Process Injection:**\n  * Perform live volatile RAM investigations with Volatility 3 (\`windows.pstree\`, \`windows.malfind\`, \`windows.netscan\`).\n  * Hunt advanced memory injection techniques: Process Hollowing (RunPE via \`NtUnmapViewOfSection\`), DLL Side-Loading, and Reflective PE injection into legitimate processes like \`explorer.exe\` with suspicious \`PAGE_EXECUTE_READWRITE\` (RWX) memory pages.\n\n• **Lab 4: Email Threat Vectors & Phishing Header Analysis:**\n  * Verify the email authentication triad: SPF (\`v=spf1\`), DKIM cryptographic signatures, and DMARC alignment enforcement (\`p=none\` vs \`p=quarantine\` vs \`p=reject\`).\n  * Trace true origin MTAs in the bottom-most \`Received: from\` header, defang malicious URLs (\`hxxps[://]evil[.]com\`), and triage weaponized OLE VBA macro attachments with \`olevba\`.\n\n• **Lab 5: Web Application Attack Investigation & WAF Tuning:**\n  * Decipher real-world web exploitation in HTTP access logs: SQL Injection (SQLi), Server-Side Request Forgery (SSRF against link-local metadata \`http://169.254.169.254\`), Directory Path Traversal, and Command Injection.\n  * Craft and deploy instant edge WAF mitigation rules using Cloudflare Expression Language.\n\n• **Lab 6: Incident Response & Threat Containment Runbook:**\n  * Execute the 6-phase NIST SP 800-61r2 Incident Response framework: Preparation, Detection & Analysis, Containment (EDR host isolation vs memory preservation), Eradication, Recovery, and Post-Incident Lessons Learned.\n\nEach lab includes detailed technical documentation, interactive terminal examples, and verified knowledge tests with in-depth feedback notes!`,
          action: {
            title: 'Launch Security Labs & SOC Hub',
            subtitle: 'Start hands-on SOC analysis, packet forensics, and triage labs',
            onclick: "switchMainView('learning'); setTimeout(() => loadTrackWorkspace('soc'), 80);",
            label: 'Enter Security Labs',
            icon: 'shield-check'
          }
        };
      }

      // 2. Full Security Audit, Vulnerability Analysis & Hardening
      if (q.includes("audit") || q.includes("vulnerability") || q.includes("pentest") || q.includes("pen test") || q.includes("cve") || q.includes("hardening") || q.includes("security score") || q.includes("posture")) {
        return {
          text: `🛡️ **Full Platform Security Audit & Vulnerability Assessment Report:**\n\nConnecto underwent an extensive architectural and source-code security audit across its Cloudflare Edge Worker gateway, Cloudflare Pages CDN layer, and FastAPI backend, achieving an **A+ (Hardened)** overall security posture:\n\n• **Cryptographic Token Integrity (Hardened):**\n  * Upgraded edge authentication tokens to issue full 256-bit (64 hex characters) HMAC-SHA256 signatures with constant-time byte-by-byte comparison (\`diff |= ...\`) preventing timing side-channel attacks.\n  * Implemented seamless backward-compatible verification for active legacy sessions.\n\n• **Zero Hardcoded Master Keys (Fail-Closed):**\n  * Neutralized fallback secrets in the pairwise End-to-End Encryption key derivation path (\`/api/v1/e2e/secret\`). The endpoint now strictly validates \`env.SECRET_KEY\` and fails closed with HTTP 503 if the secret is absent.\n\n• **SQL Injection Defense (100% Parameterization):**\n  * Audited over 40 Cloudflare D1 database operations in the Worker; verified that 100% of user inputs use prepared statement parameter binding (\`.bind(...)\`). Zero dynamic SQL string concatenations exist.\n  * Backend SQLAlchemy ORM utilizes parameterized query expressions (\`select(Model).where(...)\`) with strict table allowlists.\n\n• **Defensive Policy Directives & Cross-Origin Isolation:**\n  * Enforced \`Strict-Transport-Security: max-age=31536000; includeSubDomains; preload\` across all responses.\n  * Enhanced Content Security Policy (CSP) with \`upgrade-insecure-requests;\` to completely eliminate mixed content.\n  * Implemented \`Cross-Origin-Opener-Policy: same-origin-allow-popups\` to isolate window browsing contexts while preserving Google OAuth and Razorpay flows.\n\n• **Error Shielding & Information Disclosure Prevention:**\n  * Sanitized unhandled global API exception catches to return generic error messages with HTTP 500, preventing internal SQLite/D1 syntax error leakage to untrusted clients.\n\n• **File Upload Security & Magic Byte Inspection:**\n  * All media uploads require authenticated sessions, undergo Pillow/puremagic magic byte inspection, are converted directly into WebP format under generated UUID filenames, and strictly reject executable extensions (\`.php\`, \`.sh\`, \`.html\`).`,
          action: {
            title: 'Inspect Cryptographic Architecture',
            subtitle: 'Read our zero-trust security & protocol specifications',
            onclick: "window.location.href='/architecture'",
            label: 'Security Specs',
            icon: 'shield-check'
          }
        };
      }

      // 3. Response Time, Latency & Edge Caching Optimization
      if (q.includes("speed") || q.includes("response time") || q.includes("latency") || q.includes("fast") || q.includes("slow") || q.includes("performance") || q.includes("ttfb") || q.includes("edge cache") || q.includes("cloudflare cache") || q.includes("cache")) {
        return {
          text: `⚡ **Global Edge Response Time & Latency Optimization:**\n\nConnecto's edge routing infrastructure was comprehensively re-engineered to deliver sub-300ms global Time-to-First-Byte (TTFB) and instant page loads:\n\n• **Cloudflare Edge In-Memory Caching:**\n  * Integrated Cloudflare Workers Edge Cache API (\`caches.default.match\` and \`caches.default.put\` via \`ctx.waitUntil\`). The 1.84 MB application bundle is cached across Cloudflare's 300+ global edge cities, serving subsequent requests in under **265ms TTFB** with \`CF-Cache-Status: HIT\`.\n\n• **Elimination of HTTP 308 Subrequest Redirects:**\n  * Resolved an origin subrequest penalty where requesting \`/index.html\` from Cloudflare Pages triggered a mandatory HTTP 308 redirect back to \`/\`. Normalizing routes to canonical \`/\` eliminated an entire network round-trip.\n\n• **Zero Worker CPU Buffer Bottlenecks:**\n  * Eliminated synchronous 1.84 MB string buffering and regex replacement scans in JavaScript Worker isolates, saving ~40ms of CPU time per request.\n\n• **Cold-Start Tunnel Bypass:**\n  * Defaulted isolate circuit breaker state to \`DOWN\`, completely bypassing 1,200ms–2,000ms origin proxy tunnel timeouts when the local server is offline.\n\n• **Aggressive Browser & CDN Cache Directives:**\n  * HTML pages: \`Cache-Control: public, max-age=60, s-maxage=600, stale-while-revalidate=86400\` (repeat browser visits load in **0ms** from disk cache).\n  * Static Assets & Downloads: \`Cache-Control: public, max-age=31536000, immutable\` for instant edge delivery.`,
          action: {
            title: 'Verify Live Response Time',
            subtitle: 'Reload page to test instantaneous Edge RAM Cache hit',
            onclick: "window.location.reload()",
            label: 'Benchmark Speed',
            icon: 'zap'
          }
        };
      }

      // 4. Blackscreen Privacy & Anti-Spyware
      if (q.includes("blackscreen") || q.includes("screenshot") || q.includes("screen record") || q.includes("spyware") || q.includes("flag_secure")) {
        return {
          text: `🛡️ **Hardware Blackscreen Protection & Anti-Spyware Engine:**\n\nConnecto implements native operating-system level hardware protections to guarantee complete visual privacy:\n\n• **FLAG_SECURE SurfaceFlinger Enforcement:** On Android devices, Connecto invokes Android OS \`WindowManager.LayoutParams.FLAG_SECURE\` on active window surfaces. This instructs the device display hardware compositor (SurfaceFlinger) to blank out the screen buffer whenever a capture event occurs.\n• **Anti-Spyware Shielding:** Even if malicious background spyware, commercial trojans, or unauthorized screen recorders run with user permissions on the device, all captured screenshots and video recordings result in completely pitch-black frames.\n• **Recent Apps Task Switcher Obfuscation:** Prevents sensitive chat previews, private media, or voice call screens from appearing in the Android task switcher carousel.\n• **Decrypted Stream Defense:** Ensures that ephemeral messages and secret conversations are rendered exclusively in protected volatile RAM, never saving screenshots to disk storage.`,
          action: {
            title: 'Experience Hardware Blackscreen',
            subtitle: 'Get the native Android APK v4.6.5 (36.0 MB)',
            href: '/download/apk',
            download: 'connecto-fun.apk',
            label: 'Download Android APK',
            icon: 'smartphone'
          }
        };
      }

      // 5. End-to-End Encryption (E2EE) & Zero-Knowledge Architecture
      if (q.includes("encrypt") || q.includes("security") || q.includes("privacy") || q.includes("e2ee") || q.includes("zero knowledge") || q.includes("private key")) {
        return {
          text: `🔒 **End-to-End Encryption & Cryptographic Architecture:**\n\nConnecto is architected from the ground up on zero-knowledge cryptographic foundations:\n\n• **Client-Side AES-256-GCM Encryption:** All direct messages and private communications are encrypted directly on your local device using the browser's hardware-accelerated WebCrypto Subtle API before ever entering the network socket.\n• **Zero Cleartext on Servers:** The FastAPI backend and Cloudflare D1 databases act strictly as blind message routers. Message payloads, media streams, and personal notes are stored and routed exclusively as ciphertext. Server operators and database dumps cannot inspect your conversations.\n• **PBKDF2 Password Derivation:** Passwords are never transmitted in cleartext; they are derived using PBKDF2 with 100,000 iterations and unique 16-byte cryptographically secure random salts.\n• **Cryptographic Session Signing:** Authentication tokens are signed with HMAC-SHA256 using full 256-bit signatures and verified via constant-time algorithms to prevent timing side-channel attacks.\n• **Ephemeral Voice & Video Media:** WebRTC voice calls and video lounges utilize DTLS-SRTP encryption with direct peer-to-peer media paths, ensuring media never touches persistent storage.`,
          action: {
            title: 'Inspect Cryptographic Architecture',
            subtitle: 'Read our zero-trust security & protocol specifications',
            onclick: "window.location.href='/architecture'",
            label: 'Security Specs',
            icon: 'shield-check'
          }
        };
      }

      // 6. Android APK Download & Installation
      if (q.includes("apk") || q.includes("android") || q.includes("mobile app") || q.includes("download") || q.includes("hyperos") || q.includes("samsung") || q.includes("xiaomi") || q.includes("poco")) {
        return {
          text: `📱 **Connecto for Android APK (Official Native Release v4.6.5):**\n\nExperience pure black OLED aesthetics, hardware biometrics, and studio-grade voice calls in your pocket:\n\n• **Direct Download Specs:** Official native APK v4.6.5 (Build 94) is 36.0 MB, supporting Android 7.0 through Android 16+ (tested and verified on Xiaomi HyperOS, Samsung OneUI 6+, and Google Pixel).\n• **Hardware Biometric Vault:** Secure your private conversations behind hardware fingerprint and face unlock authentication with zero cloud biometric leakage.\n• **Pure Black OLED Aesthetic:** Engineered with true #000000 backgrounds to maximize battery longevity on AMOLED displays while reducing eye fatigue.\n• **Installation Guide:**\n  1. Tap **Download APK** below.\n  2. When downloaded, tap the file in your notification shade or browser downloads.\n  3. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** and toggle **Allow from this source**.\n  4. Tap **Install** — Connecto is 100% verified, tracker-free, and contains zero third-party telemetry.`,
          action: {
            title: 'Download Official Android APK',
            subtitle: 'v4.6.5 Live Release • 36.0 MB • Pure Black OLED',
            href: '/download/apk',
            download: 'connecto-fun.apk',
            label: 'Download APK',
            icon: 'download'
          }
        };
      }

      // 7. Chat Wallpapers & Custom Backgrounds
      if (q.includes("wallpaper") || q.includes("background") || q.includes("chat photo") || q.includes("chat image")) {
        return {
          text: `🎨 **Custom Chat Wallpapers & Visual Presets:**\n\nPersonalize individual channels and direct chats with immersive visual themes:\n\n• **How to Apply:** Open any direct message or channel, click the title in the top bar, select **Theme & Wallpaper**, and choose from 5 master presets or upload your own custom photo.\n• **5 Curated Master Presets:**\n  1. *Emerald Aura*: Deep noir with neon emerald radiant gradients.\n  2. *Midnight Obsidian*: Pure black #000000 OLED minimalist glass.\n  3. *Cyberpunk Neon*: Electric magenta and cyan dual-tone.\n  4. *Sapphire Dusk*: Deep indigo twilight gradient.\n  5. *Sunset Horizon*: Warm editorial crimson and gold.\n• **Local Cache Persistence:** Wallpapers are saved persistently to your local browser storage with automated fallback healing across sessions.`,
          action: {
            title: 'Configure Visual Appearance',
            subtitle: 'Open appearance settings to customize display & presets',
            onclick: "openSettingsModal('appearance')",
            label: 'Open Appearance',
            icon: 'image'
          }
        };
      }

      // 8. Dual Themes (Dark Noir vs Light Parchment)
      if (q.includes("theme") || q.includes("dark mode") || q.includes("light mode") || q.includes("parchment") || q.includes("noir") || q.includes("oled") || q.includes("ivory")) {
        return {
          text: `🌓 **Dual Theme System (Dark Noir & Light Parchment):**\n\nConnecto provides two meticulously engineered themes tailored for different environments:\n\n• **Dark Noir (Pure Black OLED):**\n  * Engineered with true #000000 backgrounds for absolute black pixels on OLED/AMOLED screens.\n  * Drastically reduces battery drain, minimizes night-time blue light emissions, and pairs with luminous emerald badges.\n• **Light Parchment (Archival Ivory Paper):**\n  * Inspired by classical editorial broadsheets and warm parchment paper (#faf6ee).\n  * Features deep walnut text (#2d241e), warm ivory borders (#e2d7c5), and high-contrast typography designed for effortless daylight reading.\n• **Instant Switching:** Tap the Sun/Moon toggle in the top navigation bar or switch themes in Profile Settings ➔ Appearance. Both themes feature complete component-level parity across desktop and mobile views.`,
          action: {
            title: 'Toggle Theme Now',
            subtitle: 'Instantly toggle between Dark Noir and Light Parchment',
            onclick: "toggleEditorialTheme()",
            label: 'Switch Theme',
            icon: 'sun'
          }
        };
      }

      // 9. Voice Calling, Echo Cancellation & WebRTC
      if (q.includes("audio") || q.includes("mic") || q.includes("call") || q.includes("voice") || q.includes("sound") || q.includes("webrtc") || q.includes("isolation") || q.includes("echo") || q.includes("noise")) {
        return {
          text: `📞 **Spatial Voice Lounges & Hardware Audio Isolation:**\n\nConnecto delivers crystal-clear, low-latency audio through native WebRTC integration:\n\n• **Hardware Voice Isolation:** Connecto binds directly to browser and mobile hardware audio constraint processors:\n  * *AcousticEchoCanceler (AEC)*: Prevents speaker output from looping back into your microphone.\n  * *NoiseSuppressor (NS)*: Filters continuous background noise (fans, air conditioners, keystrokes).\n  * *AutomaticGainControl (AGC)*: Equalizes microphone volume to prevent sudden clipping.\n• **Opus 48kHz Codec:** Employs the industry-standard Opus voice codec with dynamic bitrate adaptation from 24kbps up to 96kbps for rich, spatial fidelity.\n• **Troubleshooting Audio:**\n  1. Verify browser microphone permissions for \`https://connecto.fun\`.\n  2. Ensure your default input device is selected under Profile Settings ➔ Audio & Sound FX.\n  3. If behind strict enterprise firewalls, Connecto automatically routes media through our global TURN relay mesh.`,
          action: {
            title: 'Check Audio & Sound FX Settings',
            subtitle: 'Test input devices, alert volumes, and master toggles',
            onclick: "openSettingsModal('sound')",
            label: 'Audio Settings',
            icon: 'mic'
          }
        };
      }

      // 10. Video Lounges & Screen Sharing
      if (q.includes("video") || q.includes("camera") || q.includes("screen share") || q.includes("webcam")) {
        return {
          text: `📹 **Encrypted Video Calls & Lounges:**\n\nConnect face-to-face with high-definition, encrypted video conferencing:\n\n• **Encrypted Peer Streams:** Video utilizes DTLS-SRTP encryption with dynamic VP8/VP9 hardware acceleration.\n• **Flexible Controls:** Toggle camera, switch between front/rear lenses on mobile, mute microphones, or share desktop screens during meetings.\n• **Bandwidth Optimization:** Automatically scales resolution from 360p up to 1080p based on detected network throughput.`,
          action: {
            title: 'Enter Live Chat & Calling',
            subtitle: 'Jump into the channels or direct calling lounge',
            onclick: "switchMainView('chat')",
            label: 'Go to Chat',
            icon: 'video'
          }
        };
      }

      // 11. Two-Factor Authentication (2FA) & Account Protection
      if (q.includes("2fa") || q.includes("two factor") || q.includes("otp") || q.includes("vault") || q.includes("biometric") || q.includes("auth code")) {
        return {
          text: `🛡️ **Two-Factor Authentication (2FA) & Account Security:**\n\nProtect your account against credential stuffing and unauthorized takeovers:\n\n• **Email OTP Verification:** Enable 2FA in Profile Settings ➔ Account & 2FA. When enabled, signing in from any new or untrusted device requires entering a 6-digit cryptographic verification code sent to your verified email.\n• **Sliding Window Rate Limiting:** Login and 2FA endpoints are protected by IP-based rate limiting (15 attempts/minute) to defeat automated brute-force attacks.\n• **Remote Session Invalidation:** Changing your password or toggling security settings instantly invalidates all prior sessions across devices using \`all_before_{user_id}\` revocation.\n• **Biometric Lock on Mobile:** Native Android users can restrict app access behind biometric fingerprint or facial recognition hardware.`,
          action: {
            title: 'Enable Two-Factor Authentication',
            subtitle: 'Protect your account with instant email OTP codes',
            onclick: "openSettingsModal('account')",
            label: 'Open 2FA Settings',
            icon: 'key'
          }
        };
      }

      // 12. Login, Sign Up, & Account Access
      if (q.includes("login") || q.includes("signin") || q.includes("sign in") || q.includes("log in") || q.includes("register") || q.includes("signup") || q.includes("sign up") || q.includes("create account")) {
        return {
          text: `✨ **Account Access & Registration:**\n\nJoin the Connecto community with instant, zero-friction registration:\n\n• **Zero Personal Data Required:** Create a free account in seconds with only a unique username and strong password. No phone numbers, credit cards, or invasive tracking.\n• **Interactive Security Modal:** Features real-time username availability indicators, an interactive 4-segment password strength meter, and a 1-click strong password generator.\n• **Remember This Device:** Keep your secure authenticated session active across browser restarts without needing to repeatedly log in.`,
          action: {
            title: 'Open Authentication Hub',
            subtitle: 'Sign in to your account or register a free username',
            onclick: "openAuthModal('register')",
            label: 'Sign Up / Log In',
            icon: 'user-check'
          }
        };
      }

      // 13. Adding Friends & Direct Messaging (DMs)
      if (q.includes("friend") || q.includes("add") || q.includes("dm") || q.includes("direct message") || q.includes("roster") || q.includes("chat with someone")) {
        return {
          text: `👥 **Adding Friends & Direct Messaging:**\n\nConnect directly with friends and team members:\n\n• **How to Add:** In the main chat view, tap the **Friends / DMs** icon on the left navigation rail, click **Add Friend**, and enter their exact \`@username\`.\n• **Real-Time Notification:** Your contact receives an immediate incoming friend request notification to Accept or Decline.\n• **Private Channel Provisioning:** Once accepted, a dedicated encrypted 1-on-1 direct channel is automatically established for real-time text, file attachments, and spatial voice calls.`,
          action: {
            title: 'Open Direct Messages',
            subtitle: 'View your friend roster or send a new request',
            onclick: "switchMainView('chat')",
            label: 'Go to Friends',
            icon: 'users'
          }
        };
      }

      // 14. Background Push Notifications & Alerts
      if (q.includes("notif") || q.includes("alert") || q.includes("background") || q.includes("push") || q.includes("delay") || q.includes("missed") || q.includes("battery") || q.includes("sleep")) {
        return {
          text: `🔔 **Background Push Notifications & Battery Optimization:**\n\nNever miss an important message or incoming voice call:\n\n• **Instant Push Signaling:** Connecto dispatches Web Push notifications with high-priority headers to wake sleeping device threads instantly.\n• **The 'Faah' Alert Chime:** Connecto features an engineered notification chime harmonically designed to cut through ambient noise without ear strain.\n• **Android Battery Exemption:** On aggressive OEM Android skins (Xiaomi HyperOS, Samsung OneUI, OnePlus OxygenOS), apps can be frozen in the background. To guarantee immediate alert delivery:\n  1. Go to device **Settings ➔ Apps ➔ Connecto**.\n  2. Tap **Battery** and select **Unrestricted**.\n  3. On Xiaomi / Poco devices, enable **Autostart** in App Info.`,
          action: {
            title: 'Configure Notification Settings',
            subtitle: 'Review push alert preferences and test sounds',
            onclick: "openSettingsModal('notifications')",
            label: 'Notification Settings',
            icon: 'bell'
          }
        };
      }

      // 15. Community Guidelines & Direct Engineering Support
      if (q.includes("guidelines") || q.includes("terms") || q.includes("privacy policy") || q.includes("rules") || q.includes("contact") || q.includes("email") || q.includes("support")) {
        return {
          text: `📜 **Community Standards & Engineering Support:**\n\nConnecto is dedicated to respectful, private, and secure communication:\n\n• **Privacy First:** Staff will never ask for your password, email OTP verification codes, or private encryption keys.\n• **Respectful Conduct:** Harassment, automated spam bots, and illicit content distribution are strictly prohibited and subject to immediate account termination.\n• **Direct Engineering Escalation:** If you encounter unexpected bugs, security inquiries, or deployment questions, our core engineering team can be reached directly at **support@connecto.fun**.`,
          action: {
            title: 'Email Engineering Team',
            subtitle: 'Direct support inbox: support@connecto.fun',
            onclick: "window.location.href='mailto:support@connecto.fun'",
            label: 'Email Support',
            icon: 'mail'
          }
        };
      }

      // 16. Greetings & Welcome
      if (q.includes("hello") || q.includes("hi") || q.includes("hey") || q.includes("who are you") || q.includes("help") || q === "yo" || q === "sup") {
        return {
          text: `👋 **Hello! Welcome to Connecto's 24/7 AI Support & Intelligence Assistant.**\n\nI can explain all features, protocols, and security mechanics in complete technical detail. Key topics you can explore:\n\n• **🛡️ Security Labs & SOC Analysis:** Hands-on SIEM event correlation, packet forensics, memory injection, phishing triage, and incident response.\n• **🔒 End-to-End Encryption:** Zero-knowledge architecture, client-side AES-256-GCM, and PBKDF2 password derivation.\n• **📱 Android Native APK v4.6.5:** FLAG_SECURE hardware blackscreen privacy, biometric vault, and OLED optimization.\n• **⚡ Response Time & Edge Speed:** Cloudflare Edge Worker caching, sub-300ms global TTFB, and 0ms browser cache loads.\n• **🌓 Dual Themes & Wallpapers:** OLED Dark Noir vs Archival Ivory Parchment, custom photo uploads, and presets.\n• **📞 WebRTC Voice Lounges:** Hardware Acoustic Echo Cancellation, Noise Suppression, and spatial audio tuning.\n\nAsk any question in plain English or select any quick chip below!`,
          action: {
            title: 'Launch Security Labs & SOC Hub',
            subtitle: 'Explore hands-on SOC analysis and protocol labs',
            onclick: "switchMainView('learning'); setTimeout(() => loadTrackWorkspace('soc'), 80);",
            label: 'Launch Security Labs',
            icon: 'shield-check'
          }
        };
      }

      // 17. Comprehensive Intelligent Deconstruction Fallback for Any Unclassified Query
      return {
        text: `🤖 **Connecto Intelligence Analysis:**\n\nYou asked: *"${query}"*\n\nHere is a detailed breakdown of how Connecto handles this topic:\n\n• **Platform Architecture & Implementation:**\n  Connecto is engineered as an ultra-low latency, zero-knowledge real-time communication platform. It combines a high-throughput Cloudflare Global Edge Worker gateway with native Cloudflare D1 distributed SQL, Cloudflare Pages CDN edge caching, and an asynchronous FastAPI backend.\n\n• **Security & Privacy Safeguards:**\n  All communications are protected by End-to-End Encryption (AES-256-GCM), 256-bit HMAC session verification, PBKDF2 with 100,000 iterations for password hashing, and Android \`FLAG_SECURE\` hardware blackscreen protection.\n\n• **Hands-On Learning & Exploration:**\n  If your inquiry relates to cybersecurity, networking protocols, or threat analysis, jump into our **Security Labs & SOC Analysis Hub** for interactive hands-on modules and quizzes.\n\n• **Direct Assistance:**\n  For custom inquiries or technical issues, use the quick chips below or reach our engineering team directly at **support@connecto.fun**.`,
        action: {
          title: 'Explore Security Labs & Hub',
          subtitle: 'Jump into hands-on cybersecurity labs and incident response',
          onclick: "switchMainView('learning'); setTimeout(() => loadTrackWorkspace('soc'), 80);",
          label: 'Open Security Labs',
          icon: 'shield-check'
        }
      };
    }