# Connecto Platform (v3.9.6 Stable)

[![Android](https://img.shields.io/badge/Android-7.0%20to%2016%2B%20(Nougat%20to%20Baklava)-3DDC84?logo=android&logoColor=white)](https://connecto.fun/download/apk)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37%20(Android%2016%20Ready)-10b981)](https://connecto.fun)
[![HyperOS](https://img.shields.io/badge/Xiaomi%2FPOCO-HyperOS%20Verified-06b6d4)](https://connecto.fun)
[![FastAPI](https://img.shields.io/badge/Backend-FastAPI%20Async-009688?logo=fastapi&logoColor=white)](https://connecto.fun)
[![Rust Relay](https://img.shields.io/badge/Relay-Rust%20Tokio%2FAxum-DEA584?logo=rust&logoColor=white)](https://connecto.fun)
[![License: Patent Protected](https://img.shields.io/badge/License-Patent%20Pending%20%E2%80%A2%20All%20Rights%20Reserved-f59e0b)](LICENSE)

> **High-speed real-time messaging, ultra-low latency peer-to-peer WebRTC voice lounges, biometric hardware vault security, and zero-crash thread-safe architecture.**

Live Production Website: [https://connecto.fun](https://connecto.fun)  
Direct Android APK: [https://connecto.fun/download/apk](https://connecto.fun/download/apk)  
Engineering Changelog: [https://connecto.fun/changelog](https://connecto.fun/changelog)

---

## Architecture Overview

```
                      ┌──────────────────────────────────────────┐
                      │              Clients Tier                │
                      │  • Android Native (Jetpack Compose)      │
                      │  • Web Application (Vite / React / Next) │
                      └─────────────┬──────────────────┬─────────┘
                                    │                  │
                HTTPS / REST / WSS  │                  │  WebSockets / Audio
                                    ▼                  ▼
                      ┌───────────────────┐      ┌─────────────────────────┐
                      │  FastAPI Backend  │      │  Rust WebSocket Relay   │
                      │   (Port :8081)    │◄────►│      (Port :8082)       │
                      │                   │      │                         │
                      │  • Sliding 30d    │ Inter│  • Lock-Free DashMap    │
                      │    Session Auth   │Secret│  • Sub-1ms Broadcast    │
                      │  • Channel CRUD   │ Auth │  • Global Presences     │
                      │  • SQLite Storage │      │  • WebRTC Voice Mesh    │
                      └─────────┬─────────┘      └────────────┬────────────┘
                                │                             │
                                ▼                             ▼
                      ┌───────────────────┐      ┌─────────────────────────┐
                      │  SQLite (WAL Mode)│      │  coturn STUN/TURN Relay │
                      │  Local-First DB   │      │   connecto.fun:3478     │
                      │  Permanent DMs    │      │  16,384 Dynamic Ports   │
                      └───────────────────┘      └─────────────────────────┘
```

---

## Repository Structure

```
connecto/
├── LICENSE                  # Connecto Proprietary & Patent Protection License (Patent Pending)
├── PATENTS.md               # Formal Inventions & Patent Claims Disclosure Document
├── README.md                # Platform Architecture & Developer Documentation
├── .gitignore               # Multi-stack git ignore specifications
│
├── android/                 # Native Android Application (com.connecto.app)
│   ├── app/                 # Kotlin Jetpack Compose Application module
│   │   ├── src/main/java/   # Jetpack Compose UI, WebRTC Voice, Hardware Vault
│   │   ├── src/main/res/    # Drawables, themes, pure black OLED vectors, icons
│   │   ├── AndroidManifest.xml
│   │   └── build.gradle.kts # targetSdk 37, minSdk 24, arm64-v8a native packaging
│   ├── gradle/              # Gradle wrapper and version catalog
│   ├── build.gradle.kts     # Top-level project configuration
│   └── settings.gradle.kts
│
├── backend/                 # FastAPI Asynchronous Backend Service
│   ├── app/
│   │   ├── api/             # API v1 routes: auth, chat, users, careers, academy, ws
│   │   ├── core/            # Security, rate limiter, WebSockets manager, CV screening
│   │   ├── db/              # SQLAlchemy asynchronous models and SQLite persistence
│   │   ├── schemas/         # Pydantic validation and typing schemas
│   │   └── main.py          # Application entrypoint, CORS pinning, static routes
│   └── requirements.txt     # Python production dependencies
│
├── website/                 # Connecto Web Application & Static Assets (connecto.fun)
│   ├── static/              # Production web landing page, changelog, careers, icons
│   │   ├── index.html       # Landing page (v3.9.6 OLED Dark Theme)
│   │   ├── changelog.html   # Engineering releases & changelog timeline
│   │   ├── careers.html     # ATS resume screening portal
│   │   ├── architecture.html# Architectural deep-dive
│   │   └── status.html      # Real-time infrastructure status
│   └── web/                 # React / Vite / TypeScript web application source code
│
└── relay/                   # High-Throughput Rust WebSocket & Audio Relay
    ├── Cargo.toml           # Tokio, Axum, DashMap dependencies
    ├── Cargo.lock
    └── src/
        └── main.rs          # Asynchronous Rust relay with internal secret validation
```

---

## Key Technological Inventions (Patent Pending)

Detailed disclosures are provided in [`PATENTS.md`](PATENTS.md).

1. **Thread-Synchronized WebRTC Teardown Mutex**: Eliminates race conditions and SIGSEGV memory faults in `libjingle_peerconnection` during asynchronous disposal on Android 16 and POCO / HyperOS.
2. **Adaptive 16,384-Port Dynamic Pool coturn Relaying**: Expanded UDP/TCP port pool (`49152–65535`) combined with single-pass `GATHER_ONCE` ICE gathering, eliminating 200–800ms third-party relay latency.
3. **Hardware-Backed Biometric Vault**: Dual-tier Android Keystore cryptographic gating with zero-bypass screen lock integration and memory concealment (`FLAG_SECURE`).
4. **Dual-Tier High-Throughput Rust Audio & Broadcast Relay**: Lock-free in-memory packet dispatching achieving sub-1ms global delivery with inter-service cryptographic secret validation.
5. **Sliding-Window Stateless Session Renewal**: 30-day automated rolling session extension paired with bidirectional multi-alias SQLite message persistence.
6. **Pure Black OLED (#000000) Energy Conservation Engine**: Zero-emission OLED background architecture saving up to 40% AMOLED power during voice calls and messaging.

---

## Full-Stack Security Hardening

Connecto incorporates military-grade application and network security:

| Vector | Threat Addressed | Remediation Applied |
|---|---|---|
| **Arbitrary File Upload (RCE)** | Unvalidated fallback storage of executable files | Fallback removed; strict puremagic magic-byte validation (JPEG, PNG, WebP only) |
| **Unauthenticated Upload** | Anonymous disk flooding | Enforced mandatory `get_current_user` dependency on all upload endpoints |
| **Server-Side Request Forgery (SSRF)** | Internal metadata IP access (`169.254.169.254`) | Whitelisted profile media URLs strictly to relative paths (`/uploads/`, `/static/`) |
| **Relay Broadcast Hijack** | Forged channel injection | Enforced `X-Internal-Secret` cryptographic header check on Rust relay |
| **Admin Privilege Escalation** | Hardcoded username bypasses (`connecto_admin`) | Removed all username-based checks; authorization strictly bound to database `is_admin` |
| **Permissive CORS** | Wildcard / loopback exploitation | Stripped loopback origins; pinned regex to explicit HTTPS subdomains |
| **IP Spoofing Rate Limit Bypass** | `X-Forwarded-For` header rotation | Rate limiter extracts IP strictly from `CF-Connecting-IP` or kernel socket IP |
| **Username Enumeration** | Automated user discovery | Applied 20 req / 60s sliding-window rate limit on username validation |

---

## Building and Running

### 1. Android Application
```bash
cd android
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```
Target APK: `android/app/build/outputs/apk/release/app-release.apk`

### 2. Backend Service (FastAPI)
```bash
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Run development server
uvicorn app.main:app --host 127.0.0.1 --port 8081 --reload
```

### 3. Rust Relay Service
```bash
cd relay
cargo build --release
RELAY_INTERNAL_SECRET="your_secret_here" ./target/release/connecto-relay
```

### 4. Web Application (React / Vite)
```bash
cd website/web
npm install
npm run build
```

---

## Intellectual Property & License

Copyright &copy; 2026 Connecto Technologies / Vivek ([@chinnu2523](https://github.com/chinnu2523)). All Rights Reserved.

This project is licensed under the **Connecto Proprietary Software and Inventions Patent Protection License** (Patent Pending • All Rights Reserved). See the [`LICENSE`](LICENSE) file for complete terms and [`PATENTS.md`](PATENTS.md) for patent claims disclosure.
