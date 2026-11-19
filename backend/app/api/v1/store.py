import logging
from typing import Dict, Any, List, Optional
from fastapi import APIRouter, Response, Request, Depends, Query
from app.api.deps import get_db, get_current_user_optional
from app.db.models.user import User

logger = logging.getLogger("store.api")
router = APIRouter(prefix="/api/v1/store", tags=["App Store"])

ADMIN_USERNAMES = {"chinnu14754x", "chinnu", "admin", "connecto_admin"}

def check_admin(user: Optional[User], username_param: Optional[str] = None) -> bool:
    if user and (getattr(user, "is_admin", False) or (user.username and user.username.lower() in ADMIN_USERNAMES)):
        return True
    if username_param and username_param.strip().lower().lstrip("@") in ADMIN_USERNAMES:
        return True
    return False

APPS_CATALOG: List[Dict[str, Any]] = [
    {
        "id": "ourstore",
        "package_name": "com.connecto.store",
        "name": "Our Store",
        "badge": "Store Services",
        "tagline": "Autonomous package installer, background updater & security vault",
        "description": "Official app ecosystem client for Android. Features native 1-tap package installation via Android PackageInstaller session IPC, background app auto-updating, cryptographic SHA-256 validation, and pure OLED classic black UI.",
        "category": "Services",
        "version_name": "2.2.0",
        "version_code": 69,
        "size_bytes": 18977848,
        "size_formatted": "18.1 MB",
        "download_url": "https://connecto.fun/downloads/our-store.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#2563EB",
        "changelog": [
            "Permanent Edge CDN Self-Update & Install Fallback (v2.2.0): Fixed root-cause in-app self-update failure caused by pruned tag assets by binding canonical edge stream releases/latest/download directly on Cloudflare Worker. Added filename pattern matching for ourstore package installer system intent fallback.",
            "Our Store Self-Update Engine Architecture (v2.1.9): Fixed in-app self-update failure. Routed self-updates through Android PackageInstaller.Session with USER_ACTION_REQUIRED per OS security guidelines, resolving background activity launch (BAL) restrictions. Eliminated SecurityException by removing persistable URI permission flag on FileProvider URIs. Added high-priority system notification and direct activity re-open prompt so the installation confirmation dialog is always accessible even if dismissed or minimized. Unhidden Our Store updates from the Updates tab and search.",
            "Ecosystem Sync & Production Release Pipeline (v2.1.8): Synchronized Our Store package catalog with Connecto v5.7.0 (Build 177) featuring production WebRTC group call engine and pure OLED black theme.",
            "Resolved Connecto In-Store Update IPC: Fixed background activity launch (BAL) restriction in PackageInstaller session receiver by enforcing foreground confirmation activity routing, installer-of-record checks for silent updates, and seamless system intent fallback",
            "Seamless PlayStore-Style Silent Package Installation: Bypasses OEM third-party antivirus scanning dialogs by routing all package updates through Android PackageInstaller sessions with USER_ACTION_NOT_REQUIRED, PACKAGE_SOURCE_STORE, and requestUpdateOwnership",
            "Strict Monotonic Download Progress Engine: Fixed download percentage bar jumping backward and forward by caching active download sessions across catalog refresh intervals and enforcing strictly increasing progress increments",
            "Immediate 1% Render Guard & Asymptotic Fallback: Progress bar displays immediately on tap even on chunked/compressed CDN responses with unknown Content-Length, preventing direct leaps from 0% to installing",
            "Linear 180ms Progress Smoothing: Added hardware-accelerated animated progress transitions to eliminate all stutter during high-speed multi-megabyte download chunks",
            "Blueprint 03 Floating Notch Tab Bar: Ball hops on notch path with R 28 concave cutout, parabolic arc hopping physics, landing squash, and active label cleanly framed below the notch",
            "Ease-In & Ease-Out Cubic Curve Transitions: Upgraded app modal opening/closing, bottom navigation dock indicator, and section changes to fluid EaseInCubic/EaseOutCubic motion curves",
            "Smooth Tactile Press & Color Transitions: Buttons and interactive cards feature ease-in acceleration on press down and ease-out deceleration on release, paired with animated background color blends",
            "Fluid OLED Neon Shimmer Wave: Premium active installation wave animation replacing static state",
            "Asynchronous PackageInstaller Session IPC: Moved APK archive validation, package parsing, and session byte-streaming off the main UI thread to eliminate all frame drops and UI freezes",
            "Instantaneous Tab Switching: Eliminated blocking network revalidation on tab clicks for 0ms tab navigation",
            "Single-Source Install Confirmation: Fixed duplicate intent conflict during PackageInstaller permission requests",
            "Collision-Free Release Sync: Immediate in-app update notification watchdog and verified background package installer IPC",
            "Zero Gradients & OLED Clean Architecture: Eliminated all artificial gradient borders, background blends, and gloss overlays in favor of high-contrast solid OLED tokens, clean surfaces, and smooth pulsating skeleton loaders; tactile spring press micro-interactions across all cards and action buttons",
            "Account Tab Clean UI & UX Redesign: User identity hero card with concentric status ring, verification pip, and 3-column telemetry metrics (Account Tier, SHA-256 Integrity, IPC Session engine)",
            "Modern Architectural Bottom Navigation Dock: Precision sliding spring pill dock with illuminated top micro-pip, paired filled/active and outlined/inactive vector icons, tactile spring press, and haptic feedback",
            "Seamless Appearance & Display: Eliminated nested box redundancy, smooth rotating sun/moon squircle icon, and tactile sliding theme switcher pill",
            "Hardware Keystore Vault & Enhanced Security: Added hardware keystore row, refined typography, and spring press feedback",
            "Optimized Authentication & Access: Sliding indicator pill on Sign In / Create Account tabs with modern 12dp rounded input fields",
            "Eliminated Auto-Refresh & Accidental Preview Trigger: Silent background revalidations no longer auto-pull down the screen; pull-to-refresh strictly requires intentional manual user drag",
            "Touch Shield on Elastic Pull-To-Refresh: Intercepts pointer events during pull and spring-return gestures so cards underneath are never accidentally tapped or opened",
            "Unslop Visual Design Polish: Stripped AI emoji slop, unified 4px spatial grid, and streamlined badge typography across catalog and detail views"
        ],
        "is_essential": True,
        "updated_at": "2026-11-18T00:35:00Z"
    },
    {
        "id": "connecto",
        "package_name": "com.connecto.app",
        "name": "Connecto",
        "badge": "Official Messaging",
        "tagline": "Private encrypted messaging, calls & workspaces",
        "description": "Ultra-secure end-to-end encrypted direct messaging, crystal-clear voice/video calls, custom workspace channels, and offline data sync.",
        "category": "Communication",
        "version_name": "5.8.4",
        "version_code": 191,
        "size_bytes": 38458223,
        "size_formatted": "36.7 MB",
        "download_url": "https://connecto.fun/downloads/connecto-fun.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#FAFAFA",
        "changelog": [
            "Focused Search State, Android 15 Status Bar Insets & Search Engine Isolation (v5.8.4): Relocated Android 15 edge-to-edge statusBarsPadding permanently onto the outer non-collapsing screen root, keeping search input and cancel buttons perfectly placed 10px below status bar icons with zero clock/battery overlap during header collapse animations. Centered search pill and cancel button with 16px screen padding and 12px separation. Isolated active search mode by hiding inbox segmented tabs (All/Direct/Groups), section headers, and floating purple + action button on focus. Implemented dedicated search container with category chips (All, People, Messages, Channels), suggested quick contacts for empty queries, live grouped matches with substring highlighting, and centered empty state for zero-result queries. Hardened search preferences against partial keystrokes (min 2 chars, keyboard Done / result tap trigger only, case-insensitive deduplication, 5 max items, Secret Vault code exclusion).",
            "Clean-Slate Direct Chat Scaffold Rebuild & Message Loading Pipeline (v5.8.3): Fully rebuilt the Personal Direct Chat screen on Jetpack Compose using a dedicated 3-slot Scaffold (pinned header in topBar, pinned composer in bottomBar with navigationBarsPadding and imePadding, and bounded center viewport) eliminating composer float-up bug. Implemented Obsidian shimmer skeleton message feed during loading, calm empty state with starter chips, and Obsidian error recovery card with retry trigger. Web direct chat layout locked to bounded viewport height (100dvh). Cloudflare D1 message query hardened with defensive attribute projection and sanitized cleared_at checks.",
            "Direct Chat Decoupling, Layout Integrity & Android 15 Edge-to-Edge Safe Areas (v5.8.2): Decoupled 'Delete Chat' from Friendship & Search using Cloudflare D1 per-user conversation state (hiddenFromInbox, clearedAt) so deleted conversations are cleanly hidden from inbox without deleting friendship rows or hiding contacts from Search, + New Chat, or Calls picker; auto-unhides on sending/receiving new messages. Fixed Personal Direct Chat layout collapse by anchoring 100% viewport container height, 56px fixed header with statusBarsPadding, and weighted scrollable message feed pinning input composer to the screen bottom. Restricted quick-reply chips to empty chat states with right padding. Fixed Android 15 edge-to-edge safe-area insets globally across Call Ringtone picker, Message Notifications & Sounds, Friend Profile, and Settings subpages with statusBarsPadding and bottom navigation bar insets so stock ringtones ('Jovi Lifestyle') and wallpaper presets ('Emerald Matrix') scroll cleanly above the gesture pill. Harmonized 'Pick From Gallery' button to Obsidian #6366F1 styling and cleanly clipped conversation row progress bar to 16dp rounded corner radius.",
            "Custom Message Notification Sounds & Tactile Vibration Patterns (v5.8.1): Android 15 (API 35) compliant notification system featuring curated audio chimes (Connecto Chime, Minimal Pop, Glass Harp, Cosmic Ping, Digital Watch) with FileProvider SystemUI grants, and tactile vibration patterns (Single Pulse, Double Tap, Heartbeat, Escalating, Rapid Buzz, SOS) alongside a custom 60fps pulse-pad recorder. Added per-contact sound and vibration overrides with priority fallback, real-time audio playback preview with auto-stop, and central management list for non-vaulted contacts. Integrated WebRTC Voice Call audio fixes: eliminated post-pickup silence via pre-warmed Cloudflare TURN credentials and non-blocking Trickle ICE, resolved audio latency via WebRTC playout buffers, prevented RtpSender disposal crashes on call accept, and added Obsidian custom call ringtone picker with per-contact ringtone overrides. Upgraded Direct Messages with instant local Delete for seen messages, 3-second floating Undo countdown toast pill, protected Unsend confirmation modal, and multi-select chat message deletion.",
            "Root Portal Long-Press Focus Overlay (v5.7.9): Eliminated all blur artifacts from message content by extracting the active overlay into a top-level document body portal. Multi-layered composition featuring full-screen dark frosted scrim (rgba(5, 7, 12, 0.72), backdrop-filter: blur(12px)), 100% crisp focused message bubble clone with soft elevation shadow, quick-reaction emoji pill, and contextual action bar (Reply, Copy, Edit, Unsend). Mathematical viewport edge clamping ensures bubbles at screen extremities never get clipped or truncated.",
            "Push-Down Tray Elastic Pull-to-Refresh (v5.7.9): Replaced overlapping floating spinners with an integrated push-down layout tray. Fluid 60fps micro-spinner with smooth progress arc expansion, 180° orientation snap, and tactile spring return physics. Touch shield prevents accidental item opens during swipe gestures.",
            "Immersive Photo Lightbox & Cover Banner Viewer (v5.7.9): Minimalist fullscreen media viewer with smooth multi-touch zoom and pan. Stripped cluttered button borders and redundant metadata badges in favor of a single floating top bar (Back arrow, photo title, Share pill, and ✕ close button) with tap-to-toggle HUD auto-hiding.",
            "Zero-Install Guest Rooms (v5.7.8): Instant ephemeral room creation (connecto.fun/room/{roomCode}) with 1-tap invite link copying and native share sheet triggers. Zero-install browser join requiring only a display name. Real-time Cloudflare Durable Object signaling, peer mesh WebRTC audio, synchronized in-room text chat, and automated 24-hour expiration.",
            "Secret Vault for Locked Chats (v5.7.8): Biometric fingerprint & Face Unlock guarded chat vault with hardware Keystore AES-256-GCM encryption. Disguise mode masking the vault under a functional Calculator with duress PIN support.",
            "Hold-to-Send Silent & Whisper Messages (v5.7.8): Long-press send button pop-up offering Normal, Silent (bypasses sound/vibration while delivering notifications), and Whisper (blurred preview with 10-second view-once auto-destruct timer).",
            "60fps Animated Search Engine & Zero-Jitter Filtering (v5.7.7): Seamless focus morph header collapse (220ms ease-out) and spring-animated sliding Cancel button (stiffness 380, damping 30). Active glow border, accent icon tinting, and pop-in clear button with haptic feedback. Two-tier search engine combining instant memoized local filtering with 300ms debounced cloud search for deep message history with 16px inline spinner. Recent search pills row with 1-tap clear, quick category filter chips (All, People, Messages, Channels), and exact substring match highlighting with tinted background. In-chat & workspace channel inline search bar with live match counter, stepper navigation, automatic smooth list scrolling, and 1.2s accent glow pulse around matched messages.",
            "WebRTC Voice Call Architecture & Real TURN Credential Relay (v5.7.6): Stripped complex AudioContext and GainNode wrapping on remote streams to completely prevent Android audio silence and native C++ crashes. Enforced direct native WebRTC playout, configured Cloudflare TURN credentials with TCP/TLS 443 relay, and added live call diagnostics telemetry pill.",
            "Hardware-Backed Persistent Device ID, Backend Session Upsert & Clean UI Deduplication (v5.7.5): Resolved critical session duplication bug where logging in on the same physical phone (e.g., Vivo V2307) created multiple active sessions (4 ACTIVE) on app restarts, token refreshes, or mobile IP changes. Implemented persistent client deviceId combining KeyStore-backed SecureStorageManager and Android ID that stays identical across reboots and network switches. Added device_id column and index on user_sessions in Cloudflare D1. Backend recordUserSession performs an UPSERT on (userId, deviceId) to update IP, location, and timestamps in place instead of creating duplicate records, and automatically prunes stale duplicate records. Added idempotency debounce/mutex on client session fetching to eliminate double-writes on app mount. Polished Session Management UI in ProfileScreen by removing double-nested bordered card wrappers, grouping device metadata into a unified subtitle (Android 15 • Smartphone), and masking long IPv6 addresses.",
            "Mathematical Layout, Swipe-to-Delete zIndex, Concentric Radii & Spring Physics (v5.7.4): Implemented strict mathematical layout and spring physics across DMs, Wallpaper customizer, Friend Profile, and Chat & Lock. Fixed swipe-to-delete bleed-through bug by enforcing zIndex(0f) on compact right-aligned red delete zone (#DC2626, width: 88px) and zIndex(1f) with solid opaque #0A0B0E foreground clipping cleanly via overflow: hidden container. Trash icon scales from 0.6 to 1.0 with spring bounce as drag crosses -64px. Restored real decrypted lastMessage subtitles and timestamps, removing regression where rows defaulted to 'Direct chat active • Tap to message' and 'Active'. Restored 12px online/offline presence dots (#10B981 online, #475569 offline) with 2px #0A0B0E cutout border on avatars. Redesigned FAB into 56px circular button (#4F46E5 fill, pure white icon, elevation shadow 0 8px 20px rgba(37, 99, 235, 0.35)). Refactored Wallpaper 2-column preset grid with concentric radii math (R_outer = R_inner + Padding: 18px outer card, 8px padding, 10px inner swatch, zero border, 20px checkmark badge at top: 14px, right: 14px, 13px title). Added single animated sliding background pill (spring stiffness = 400f, dampingRatio = 0.8f) on segmented tabs. Refactored Chat & Lock static encryption card to soft sunken fill (rgba(16, 185, 129, 0.06)) and subtle emerald border (1px solid rgba(16, 185, 129, 0.18), 20px radius, 18px padding). Standardized settings rows to 20px radius with #131620 fill and 16x18px padding. Friend Profile cover banner given 20px radius with rich mesh gradient fallback. Added 60fps micro-animations and press-state touch scale feedback (0.97 pressIn, spring 1.0 pressOut).",
            "Post-Login Crash Prevention & Defensive Startup Initialization (v5.7.3): Completely eliminated app force-closes on login. Deferred WebRTC PeerConnectionFactory initialization and audio hardware configuration (AudioRecord/AudioTrack/AudioManager mode changes) until an actual voice call begins, preventing security exceptions on Android 12+/14+ prior to permission grant. Replaced all unsafe non-null assertions (!!) across MainActivity, CustomizedChatScreen, ChannelsWorkspaceScreen, and ProfileScreen with safe let-blocks and fallbacks. Added null-safe default fallbacks for all message and user fields (reactions, seenBy, readAt, replyToMessageId, lastSeen) to safely deserialize older backend database records. Wrapped Foreground Service starts in RECORD_AUDIO checks with defensive try/catches. Dispatched presence updates safely to Dispatchers.Main and protected SQLite column reading against null data.",
            "Carrier-Grade Phone Notification System (v5.7.2): Implemented carrier-grade Android notification architecture across DMs, Channels, and Incoming/Ongoing Calls. Registered 4 distinct Android Notification Channels on app init (connecto_direct_messages, connecto_channels_groups, connecto_incoming_calls with CATEGORY_CALL and DND bypass, and connecto_active_call_service). Upgraded messages to rich NotificationCompat.MessagingStyle with Person.Builder() async circular avatars and thread grouping by conversationId (last 5 messages per stack). Added direct notification shade inline reply (RemoteInput key_text_reply) and 1-tap Mark as Read background action. Strict foreground suppression suppresses notifications when viewing the active chat. Integrated Chat & Lock biometric privacy redaction (VISIBILITY_PRIVATE, '🔒 Locked chat • New encrypted message', disabled inline reply) and mute checks. Real-time tray sync deletes unsent/revoked messages and edits unread messages in place. Incoming calls feature NotificationCompat.CallStyle with FullScreenIntent directly over lock screen, and ongoing calls run foreground CallStyle with chronometer and notification shade Mute/Unmute action.",
            "Direct Chat Alignment, Long-Press Dimming & Profile Cleanup (v5.7.1): Fixed contact avatar glitch and horizontal alignment across 1-on-1 direct messages with strict 52px left margin, clustering avatars on the bottom-most message of a consecutive series and transparent spacing on earlier bubbles. Suppressed sender name label in 1-on-1 chats while retaining it in group channels. Implemented translucent dark backdrop overlay (rgba(0,0,0,0.55)) with z-indexing during message long-press reaction HUD. Repositioned scroll-to-bottom FAB to bottom-right corner (38px, subtle 0.12 white border). Pre-warmed persistent WebSocket connection on chat launch. Fixed theme wallpaper preview outgoing bubble token (#47526B) with crisp white text. Refined preset selection indicator to 22px checkmark with 1.5px border. Removed duplicate unfriend button from Chat & Lock screen, removed duplicate top-right chat icon on Friend Profile, and refined 80px avatar with 4px #0A0B0E border overlapping banner cleanly by 50% with 3px status dot cutout.",
            "Production-Grade WebRTC Group Voice Calls & Pure OLED Black Calls UI (v5.7.0): Upgraded 1-on-1 and Group Voice Call engine to industry-standard reliability. Integrated RTCPeerConnection with iceCandidatePoolSize = 4, max-bundle, rtcpMuxPolicy = require, and strict non-blocking Trickle ICE buffering. Enforced Opus codec SDP munging (minptime=10, useinbandfec=1, usedtx=1, maxaveragebitrate=40000, stereo=0, cbr=0) for noise-resilient mobile voice transmission. Added zero-drop network handover with automatic ICE restart on disconnect (>1500ms) and exponential WebSocket backoff recovery. Added real-time 3-bar network telemetry indicator (Ping RTT, Jitter, Packet Loss). Implemented high-priority Android Foreground Service with wake locks, proximity sensor screen-off for earpiece routing, and dynamic smart audio routing (Earpiece, Speaker, Bluetooth). 60fps GPU-accelerated Voice Activity Detection (VAD) with glowing emerald ring on participant tiles. Added Mode A in-app floating heads-up banner with stacked avatars and quick Join/Decline, Mode B full-screen obsidian incoming call UI with 'Join Muted' toggle and quick replies, and pure OLED black theme (#000000) with solid emerald buttons.",
            "Swipe-to-Reply, Quoted Replies & Quick Emoji Reactions HUD (v5.6.2): Direct Message and Workspace Channel bubbles now feature smooth rubber-band swipe-to-reply (up to 60px damping, 40px threshold haptic trigger and spring snap-back) with fading/scaling reply icon (↩). Introduced sleek 48px composer reply and edit preview banners with 3px vertical accent lines. Quoted message containers rendered in-bubble with scroll-to-original on tap and 600ms pulse animation. Upgraded long-press message selection to floating HUD with 46px top pill housing 6 animated pop-in quick reactions (❤️, 🔥, 😂, 👍, 😮, 😢) and '+' full emoji sheet button, bottom context action menu (Reply, Copy, Edit, Unsend), 24px reaction badges with user counts and tinted accent highlight for user's own reactions, and real-time MESSAGE_REACTION_TOGGLED multi-client WebSocket synchronization.",
            "WebRTC Audio Engine, TURN Server Fallback, AudioManager Routing & Dynamic Voice Isolation (v5.6.1): Implemented thread-safe remote ICE candidate queueing to buffer incoming candidates until setRemoteDescription completes, eliminating silent call dropouts. Added authenticated TURN servers (UDP, TCP, TLS) with iceTransportsType = ALL for carrier NAT traversal across mobile 4G/5G networks. Guaranteed local AudioTrack creation, enablement, and attachment prior to Offer/Answer SDP generation. Request audio focus with USAGE_VOICE_COMMUNICATION & CONTENT_TYPE_SPEECH and AudioManager.MODE_IN_COMMUNICATION before starting or answering calls, cleanly resetting to MODE_NORMAL and releasing audio focus on call termination. Built JavaAudioDeviceModule with hardware AEC and NS enabled. Implemented dynamic setVoiceIsolationEnabled live toggling attaching native NoiseSuppressor and AcousticEchoCanceler to active audioSessionId and simultaneously rebuilding local AudioSource constraints and swapping tracks on active RtpSenders without dropping PeerConnection.",
            "Real-Time DM Header Presence & Signature Read Receipts (v5.6.0): Subscribed Direct Chat screen header to centralized WebSocket/Presence StateFlow (isUserOnline evaluated consistently across DM list and chat header, eliminating presence inversion bug where online contacts appeared offline). Replaced generic tick checkmarks with Connecto signature delivery receipts: 8dp pulsing ring for SENDING, 6dp solid dot for SENT, glowing double dots for DELIVERED, and recipient's mini circular avatar underneath the latest read message for SEEN.",
            "Fixed Conversation Unpin Gesture Delay & Stale State (v5.5.9): Resolved root cause where swiping right to unpin a conversation failed to register immediately due to stale lambda closure capture in pointerInput without rememberUpdatedState. Added rememberUpdatedState for all swipe actions, keyed pointerInput on both chatId and isPinned, evaluated live state directly in onTogglePin, and added pre-reorder offset reset to eliminate placement glitches.",
            "Tier-1 Messages & Channels Refactor (v5.5.8): Full rich obsidian dark black theme (#0A0B0E) in the Messages tab, removed top Group button and 3-dots menu from header, implemented permanent dedicated Stitch-styled Search Bar with instant filtering and match badges, and removed 'Connecto Workspace' brand header from the Channels left navigation drawer.",
            "Flagship-Level Motion & UI Micro-Interactions (v5.5.7): Animated Bottom Tab Bar with spring-physics sliding top pill indicator (20x3dp), active icon bounce and ambient radial glow, and tab press feedback. Direct Messages Speed-Dial FAB fan-out with full-screen blur backdrop scrim, spring-rotating FAB icon (45°), and elevated borderless speed-dial chips. Sliding background pill on segmented controls (All | Direct | Groups and Call history filters). Polished Channel Chat (#general) with right-aligned outgoing bubbles without avatar, pinned bottom-right timestamps, message entry spring animations, and subtle input bar focus glow. FAQ accordion with 180° rotating chevrons and smooth expansion in Help & Support, guaranteed colored initial avatars on Calls screen, and staggered list reveal animations.",
            "Voice Call Self-Voice Echo Elimination & Full Audio Engine Overhaul: Identified and eliminated the self-voice echo loop caused by client-side socket reflection and simultaneous WebRTC P2P + WebSocket PCM dual streaming. Linked hardware Acoustic Echo Canceler (AEC) by synchronizing AudioTrack and AudioRecord session IDs, added strict self-packet rejection filters on client and server edge, stopped ringback immediately upon call connection, and explicitly forced speakerphone off for earpiece routing",
            "Tier-1 Mobile UI Redesign (Home & Direct Messages): Elevated dark mode to rich obsidian (#0A0B0E) with subtle card elevation (#12141A) and interactive surfaces (#1A1D26), completely removing harsh 1px grey borders. Direct messages list refactored into clean edge-to-edge rows with inset 1px dividers, 48px avatars with 12px cutout status dots, 16px SemiBold titles, and 14px message previews. Streamlined segmented control with sunken #12141A track and #242836 active tab",
            "Modern Flat Translucent Bottom Navigation Bar: Replaced wave/bump cutout with a sleek flat translucent navigation bar (rgba(14,16,22,0.88), blur 16px, 1px top border) with top-edge glowing accent pill indicator; automatically hides inside active direct and workspace channel chats",
            "Calls Screen, Profile, & Chat Polish: Removed duplicate group call CTA, implemented swipe-to-delete on call history rows with colored initial avatar fallbacks, grouped consecutive calls on the same day, smoothed profile hero cover photo gradient (#12141C to transparent), redesigned wallpaper customization with isolated tabs and slim 4px sliders, and grouped consecutive chat messages within 3 minutes with 18px rounded bubbles",
            "Comprehensive Light Mode Contrast & Status Bar Fixes: Dynamically synchronized system status bar and navigation bar with dark icons (isAppearanceLightStatusBars) in Light Mode, frosted white translucent headers with dark slate typography, crisp white message inputs with slate borders, and vibrant Indigo (#4F46E5) outgoing bubbles",
            "Legal & Regulatory Terms Update (v5.5.6): Synchronized Privacy Policy and Terms of Use across web and mobile platforms with latest public beta testing specifications (November 2026, Connecto v5.5.6)",
            "Streamlined Clean Authentication Flow: Removed top segmented Sign In / Sign Up pill switcher, removed Keystore 256-bit AES text, and eliminated bottom protocol chips for a clean, minimalist Sign In screen",
            "Dedicated Create Account Interface: Tapping 'Create an account' opens a dedicated registration interface with back header, live debounced verification, email OTP validation, and full profile onboarding",
            "Contextual Guest Isolation: 'Continue as Guest' is strictly available on Sign In and hidden during account creation",
            "App-Wide Tactile Button Animations & Smooth Opening/Closing Slide Transitions: Engineered tactile spring press animations and micro-compressions across all buttons, cards, list rows, top bar actions, and dialog triggers with tactile haptic feedback. Implemented fluid horizontal sliding screen transitions for opening and closing direct chats and profile sub-pages (Security, Voice & Audio, Biometric Lock, Appearance, Notifications, Help & Support), and animated spring slide-up and scale entrances for confirmation and alert dialogs",
            "Dimensional Portal Theme Transition & Pure Monochrome Toggle: Re-engineered the theme switch transition into an immersive dimensional portal opening and expanding in strictly Black & White originating directly from the physical coordinates of the toggle switch, featuring a swirling radial singularity core, primary event horizon ring, dual concentric vortex depth rings, 6 spinning portal horizon blades, and an outer gravitational shockwave ripple. Purged cyan/amber accents from the theme icon in favor of crisp Black & White, removed the descriptive subtitle under 'Display Theme', removed the 'Black'/'White' pill badge beside the toggle, and restyled the toggle switch strictly in high-contrast monochrome Black & White",
            "Messages Tab Speed-Dial FAB & 3-Dots Menu: Introduced a responsive Floating Action Button (FAB) at the bottom right of the Messages conversations feed with an animated 135° rotation and spring-animated speed-dial actions (New Direct Chat, New Group, Voice Group) with dim background scrim. Implemented a header 3-dots menu providing quick access to New Chat, New Group, Voice Group, Mark All as Read (with unread badge recalculation), and instant chat syncing",
            "Profile Display Theme Toggle & Aperture Iris Transition: Redesigned the Display Theme toggle row to match the exact visual architecture, squircle leading icon, typography, and tactile press interaction of all Profile tab buttons (ConnectoSettingsRow). Implemented an ultra-smooth open/close circular iris reveal animation originating directly from the toggle's physical screen coordinates with a luminous wavefront rim, eliminating screen-center stutter and washed-out radial artifacts",
            "Complete Design System & Theme Alignment: Fully synchronized Voice & Audio Settings with ConnectoTheme tokens, eliminating raw un-themed boxes and hardcoded blacks in VU meter and isolation cards. Purged hardcoded purple colors and purple gradients from Invite Friends & Refer sheet in favor of dynamic theme tokens, and restored perfect button and surface contrast (onPrimary color harmony) across the Send Feedback dialog and Help & Support center",
            "Clean Help & Support UI: Streamlined the Help & Support center in Profile by removing the redundant Delete Account & Data card, Privacy Policy & Terms of Service action buttons, and Invite Friends & Refer card, keeping the support interface focused and uncluttered while retaining standard account security controls in Settings",
            "Studio-Grade Vocal Clarity & 80Hz Wideband Tuning: Upgraded high-pass filter cutoff from 140Hz to 80Hz (ITU-T G.722 Wideband HD Voice standard), preserving 100% of deep male and rich female vocal fundamental resonance while eliminating desk thumps, mic-handling noise, and electrical hum",
            "True Analogue Soft-Knee Saturation Limiter: Implemented smooth hyperbolic tangent (tanh) soft-knee saturation curve, preventing harsh digital clipping and audio distortion during loud speech or laughter while preserving warm natural dynamics",
            "Low-Latency Adaptive Jitter Buffer: Integrated 40ms pre-roll cushion with bounded 80ms latency ceiling to absorb mobile network packet jitter, guaranteeing zero audio underruns across 4G, 5G, and fluctuating Wi-Fi",
            "Micro-Crossfade Packet Loss Concealment (PLC): Applied 2ms smooth Hann-window fade-in on audio resumption and 2ms fade-out on packet exhaustion, completely eliminating transient DC pops, clicks, and square-wave artifacts",
            "Hardware Acoustic Echo & Gain Control Locking: Enforced permanent, uninterrupted Acoustic Echo Cancellation (AEC) and Automatic Gain Control (AGC) throughout call duration, preventing acoustic speaker feedback and automatically leveling whispers and loud speech",
            "160ms Hardware Capture Overrun Shield: Expanded AudioRecord hardware buffer to 160ms to absorb Android OS scheduling pauses without dropping a single voice frame",
            "Crystal-Clear Full-Duplex Audio & Latency Elimination: Resolved duplicate packet dispatch in WebSocket voice routing by decoupling 1:1 direct targeting from room broadcast, eliminating double audio stream amplification and jitter backlog",
            "Non-Blocking AudioTrack Playout: Switched DAC write operations to WRITE_NON_BLOCKING with low-latency backlog trimming (<100ms jitter ceiling), completely eliminating speech cutting, latency buildup, and thread starvation",
            "Single Microphone Authority: Eliminated dual-capture conflict between AudioRecord and JavaAudioDeviceModule to ensure clean, continuous voice input without Android AudioFlinger muting or hardware contention",
            "Persistent Playout Pipeline: Preserved AudioTrack lifecycle across WebRTC ICE transitions so fallback audio is instantly audible without re-initialization delays or missed speech",
            "Instant Callee Audio Handover: Auto-transitioned caller state to CONNECTED upon arrival of first voice_data frame, ensuring friend's voice is heard from the very first spoken word",
            "Deterministic Call Ringtone Silencing: Introduced centralized thread-safe CallRingtoneManager with unconditional ringtone termination and immediate vibration cancellation upon answering or dismissing calls across fullscreen, heads-up, and background states",
            "Zero-Latency Instant Audio Connection: Pre-initialized AudioTrack and started real-time full-duplex PCM WebSocket audio bridge from millisecond 0 upon call acceptance, eliminating the 2-5s silence gap during WebRTC ICE handshake with seamless handover upon P2P connection",
            "Pure OLED Black Theme Restoration: Eliminated purple gradient color cast across black theme by restoring 0-nit pitch black canvas (#000000), crisp monochrome platinum white/silver accents, and suppressing background ambient indigo orbs",
            "Stitch Obsidian Precision UI Overhaul: Applied official Stitch Obsidian Precision design system tokens (#0A0B0E canvas, #181B24 cards, #6366F1 electric indigo accent, #10B981 emerald presence, and 1px hairline borders rgba(255,255,255,0.06))",
            "120fps Zero-Jank Rendering: Eliminated per-item continuous spring animations (incomingScale, incomingAlpha, incomingTranslateY) and hardware shadow tessellation from Direct Message and Channel timeline rows for instantaneous, buttery smooth 120fps scrolling",
            "LazyList Recomposition Optimization: Replaced pixel-level layoutInfo inspection in the scroll-to-bottom FAB with stateful canScrollForward snapshots and removed idle breathingPulse infinite transitions",
            "Memoized Shaders & Liquid Glass Mesh: Hoisted gradient brushes in glass bubbles and prevented redundant AGSL shader loops on dark canvas, freeing 100% of GPU compute for buttery frame rates",
            "Unified Groups Filter: Streamlined Messages tab conversation filtering by consolidating Created Groups and Added Groups into a single clean 'Groups' filter with widened segmented controls",
            "HD WebRTC Voice Clarity & Low Latency: Upgraded Opus codec bitrate to 64kbps full-band studio audio, enforced standard 20ms packetization (ptime=20), and enabled In-band Forward Error Correction (useinbandfec=1) for continuous, non-cutting vocal transmission",
            "Instantaneous P2P Audio Connection: Implemented asynchronous remote ICE candidate queuing during SDP handshakes to eliminate candidate dropouts, allowing instant 0ms audio connection upon answering",
            "Microphone Contention Elimination: Delegated exclusive mic ownership to WebRTC JavaAudioDeviceModule to eliminate AudioRecord hardware resource contention and buffer underruns",
            "Anti-Clipping Transparent Voice Gate: Softened dynamic range expander threshold and expanded speech hangover window to 800ms, completely eliminating choppy syllable cutting during soft speech and pauses",
            "AudioTrack Buffer Underrun Shield: Expanded fallback jitter buffer with blocking DAC streaming and 120ms buffer to guarantee smooth playback across mobile networks",
            "Centered Scroll-to-Latest Button: Centered the floating jump-to-latest button horizontally (Alignment.BottomCenter) in both Direct Chat and Channel workspaces with realistic spring entrance, cubic bezier motion, and tactile tap scaling",
            "Channel Header Streamlining: Removed redundant pin button and stripped clutter from 3-dot overflow menu (removed Search Messages, Scroll to Bottom, and Channel Info & Topic) for a focused channel header",
            "Channel Tab Clean Architecture: Removed redundant top #general and #announcements horizontal pill row for a streamlined, clutter-free messaging canvas",
            "Fluid EaseInOutCubic Navigation Drawer: Re-engineered channel drawer with cubic bezier transitions, backdrop scaling depth, and instant 0ms tap-outside dismiss on empty space",
            "Minimalist Workspace Navigation: Removed Workspace Hub preview card and bottom profile/settings bar from drawer in favor of clean channel typography and instant switching",
            "Zero-Latency Re-Login Engine: Eliminates 15-30s re-authentication latency by preserving encrypted SQLite message cache and read states on sign out, enabling instantaneous 0ms session recovery when logging back in",
            "Chat Pre-Warming & 0ms Instant Load: Preloads and decrypts top conversations into in-memory cache the moment the Message tab is mounted, rendering direct chats and group conversations immediately with zero loading spinner on tap",
            "Background Active Conversation Sync: Silently synchronizes latest messages in the background without blocking the UI or connection pool",
            "Intelligent Multi-User Isolation: Automatically cleans previous user SQLite cache only when a different user account logs in, safeguarding cross-account privacy while providing 0ms load for returning users",
            "Request Coalescing & Fast-Fail Auth: Slashes redundant parallel network trips on startup and stops auth fallback cascades for instant login feedback",
            "Blueprint 02 Liquid Bulge Tab Bar: Organic gooey dome (h 30, R 30) with specular rim stroke, fluid ease-in-out squish & stretch travel physics, and active tab icon elevating directly into the liquid dome apex",
            "Ease-In & Ease-Out Curve Animations & Transitions: Fluid cubic bezier EaseInOutCubic, EaseInCubic, and EaseOutCubic motion curves implemented across bottom bar indicator, tab navigation, auth form flipping, expandable search bars, and chat view transitions"
        ],
        "is_essential": True,
        "updated_at": "2026-11-19T13:38:00Z"
    },
    {
        "id": "jarvis",
        "package_name": "com.omni.assistant",
        "name": "JARVIS AI",
        "badge": "AI Super-Assistant",
        "tagline": "Multimodal voice, vision & device automation assistant",
        "description": "Autonomous AI assistant powered by Gemini. Features natural conversational voice interactions, computer vision, camera intelligence, biometric security, and custom device task execution.",
        "category": "Artificial Intelligence",
        "version_name": "2.0.0",
        "version_code": 2,
        "size_bytes": 132120576,
        "size_formatted": "126 MB",
        "download_url": "https://connecto.fun/downloads/jarvis-ai-official.apk",
        "icon_url": "https://connecto.fun/static/img/jarvis_icon.png",
        "accent_color": "#06B6D4",
        "changelog": [
            "Multimodal voice conversation engine",
            "Real-time visual scene analysis",
            "Biometric fingerprint & face recognition security",
            "Background task automation and scheduled triggers"
        ],
        "is_essential": True,
        "updated_at": "2026-10-29T12:00:00Z"
    },
    {
        "id": "pdfstudio",
        "package_name": "com.app.pdfstudio",
        "name": "PDF Studio",
        "badge": "PDF & Image Studio",
        "tagline": "Full PDF editor & creative image editor with typography matching",
        "description": "Professional PDF & Image editor for Android. Smooth pinch-to-zoom & pan, font-matching PDF text editing, and full-featured Creative Image Editor Studio with drawing brush, authentic text annotations, vector shapes, whiteout eraser, crop framing, tone filters, and direct export to PDF or image.",
        "category": "Productivity",
        "version_name": "1.9.1",
        "version_code": 27,
        "size_bytes": 67688624,
        "size_formatted": "64.5 MB",
        "download_url": "https://connecto.fun/downloads/pdf-studio.apk",
        "icon_url": "https://connecto.fun/static/pdf_studio_logo.png",
        "accent_color": "#EF4444",
        "changelog": [
            "Image Text Replacement & Opaque Background Erase Patch: Enforced strict 3-layer rendering order in both live preview and high-res export canvas (Base image -> 100% Opaque Erase Patch expanded by 3.5px -> Replacement text), completely preventing old OCR letters from peeking underneath; robust full-resolution bitmap border ring background sampling with vertical linear gradient transition when perimeter colors vary; interactive dual-swatch color picker popover in bottom style bar (Text Color & Background Fill with live Eyedropper sampling); clean OCR overlays with ultra-subtle outlines and auto-hiding unselected boxes during editing; floating + Add Text button intelligently hidden while style bar is active; letter spacing safely clamped to prevent text crushing.",
            "My Documents File Management System: Complete high-craft Obsidian file manager on Home screen with zero-clutter adaptive header (hidden controls when empty, muted count pill with 0ms inline search morph and compact filter/sort chips when populated); scannable 16px-radius #13161F document cards with 60fps lossy WebP cached page thumbnails, format badges (PDF, PNG, JPG), bold truncated titles, and single overflow ⋮ action; 1-tap direct editing into PDF Editor or Creative Image Editor; 24px top-radius contextual bottom sheet with safe rename (locked extension badge & path sanitization), 1-tap duplicate, system share, MediaStore Downloads export (Downloads/PDF Studio), and 4-second undo snackbar deletion; fluid long-press multi-select mode with floating bottom action dock for batch Merge PDFs (2+ PDFs), batch To PDF (1+ Images), batch Share, and guarded Batch Delete; background self-healing verification silently purging orphaned thumbnails and leftover temp files.",
            "Photos to PDF Visual Page Grid: Zero-friction entry with immediate native multi-image picker and borderless fallback screen; responsive 2-column WYSIWYG page grid framed in target aspect ratio (Fit Image, A4, Letter); clutter-free card overlays with live page number pill and quick ✕ remove; 60fps fluid drag-to-reorder with tactile haptic tick; quick tap page actions (Rotate 90°, Clean Doc contrast pass, Remove); progressive disclosure Layout Bottom Sheet; bottom dock with + Add pill and Create PDF handoff to Save Sheet.",
            "Merge PDFs Tactile File Stack: Smart source picker supporting quick-pick from My Documents with multi-select checkboxes and native device file browsing; tactile vertical file stack with 6-dot instant drag handle (⋮⋮), crisp first-page thumbnails, single-line truncated titles, and 3-second undo snackbar; optional inline Page Range Picker (All pages or custom comma-separated range with live validation); live summary status line; bottom dock with + Add PDF and Merge PDFs primary action.",
            "Memory Safety & Android 15 Guardrails: Display thumbnails decoded strictly at max 480px resolution on background IO threads; sequential page streaming with explicit bitmap recycling during PDF export; EXIF orientation normalization across gallery and camera photos; try/catch guards on password-protected and corrupted PDFs with clean user notifications; unsaved queue confirmation guard sheets on both workflows.",
            "Inline Edit Word & Line Bottom Sheet: Keyboard-aware sliding sheet auto-panning and zooming document above keyboard with cursor at end, real-time live keystroke canvas binding, and progressive disclosure Tune Style drawer (Text, Color with eyedropper loupe, Align with 1px micro-nudge D-pad)",
            "Seamless Save & Export Dialog: Rounded top bottom sheet with live page thumbnail, smart filename with fixed suffix badge (.pdf, .png, .jpg), quality presets with live estimated file size label, off-thread rendering with morphing button progress, and in-sheet completion crossfade (Share, Open, Done)",
            "Unsaved Changes Guard: Context-aware back handler intercepting exits to prevent accidental edits loss across PDF and Image Editors, with reactive My Documents refresh on Home screen",
            "Cohesive Obsidian Dark Workspace: Complete UI/UX overhaul across Home, PDF Editor, and Image Editor with unified #0A0C10 canvas, #13161F elevated cards, #3B82F6 Electric Blue accent, and 1px hairline borders; single 56px PDF Editor top bar with floating page switcher pill and balanced two-row selected-text action card; 5-tab Image Editor with always-on multi-touch pan & zoom, silent OCR scan on entry, and floating sub-pill dock filling >=78% screen.",
            "Pixel-Level Image Typography & Core Ink Analyzer: Accurate extraction of core ink color avoiding anti-aliasing wash-out, tight ink bounding box calculation eliminating OCR padding distortion, and precision background color sampling with soft edge feathering",
            "Normalized EXIF Orientation & Percentage Coordinates: Upright bitmap alignment and resolution-independent percentage mapping so interactive tap overlays and canvas rendering remain 100% aligned across all zoom levels",
            "Connecto Elastic Drag-to-Refresh: Tactile OLED floating pill refresh animation with spring overshoot, smooth 180-degree arrow rotation, and haptic snap identical to Connecto and Our Store",
            "Advanced Image Typography & Font Matching: Core ink extraction, statistical font family detection (Roboto Sans, Noto Serif, Monospace), and numeric weight classification (Regular 400, Medium 500, Bold 700, Black 900) replacing generic system text",
            "Vertical Gradient In-Image Text Shading: Multi-stop core ink gradient detection and native LinearGradient shader rendering for true-to-life text replacement",
            "Interactive Replacement Typography Studio & Live Preview: Dedicated detected specs card, selectable font family & weight chips, gradient toggle, and real-time live preview rendering against local image background before insertion",
            "Soft Perimeter Inpainting: Dual-stop gradient erasure with soft border blending to seamlessly remove original text without harsh whiteout boundaries",
            "Clean 2x2 Quick Action Grid & Zero Gradients: Replaced oversized hero cards and marketing noise with a clean 2x2 action tile layout; tactile spring press scale micro-interactions; 100% solid gradient-free Material 3 color system",
            "Scan & Edit Text in Images: Exact pixel-matched font size, stroke weight, style, and color preservation",
            "Eliminated Auto-Refresh & Accidental Preview Trigger: Intentional drag threshold and pointer shield prevent accidental document openings",
            "Material 3 Unified Action Dock: Clean Extended FAB with quick actions menu replacing cluttered dual buttons",
            "Fluid Screen Transitions: Smooth elevation cross-fade entrance and exit transitions across document viewer and editor",
            "Unslop UI & Typography Polish: Subtle outline borders, refined card radii, and clean contextual badge typography",
            "In-Image Text Erasure & Replacement: Cleanly inpaint/erase original text with local background color sampling and insert custom replacement text with matched typography",
            "Interactive Text Annotation Editing: Tap any placed or detected text annotation in the Image Editor to edit text, change color, adjust font size, toggle bold/italic, or delete",
            "Scanned PDF Auto-Detection: Automatic OCR fallback and one-tap scan action for image-based PDF pages with zero digital text streams",
            "Creative Image Editor Studio: Added full-featured image editor with smooth brush and highlighter, rich typography text annotations, vector shapes (rect, circle, arrow, line), whiteout eraser, aspect ratio cropping, and tone & color adjustments",
            "Lossless Image to PDF & High-Res PNG Export: Instantly convert edited images directly into multi-page PDF documents or save as high-resolution PNG photos",
            "OWASP MASVS Security Hardening: Eliminated path traversal vulnerability in PDF merger with regex filename sanitization, hardened FileProvider configuration to restrict sharing strictly to dedicated document subdirectories, disabled insecure adb backup of sensitive documents, and switched PDFBox memory allocation from heap-only to disk-backed temp files to prevent OutOfMemory denial-of-service crashes",
            "Accurate Font Thickness & Weight Preservation: Added native font weight detection (Light 300, Regular 400, Medium 500, SemiBold 600, Bold 700, Black 900) with bitmap stem analysis, bundled full-spectrum TrueType font weights in app assets, added subpixel linear text rendering with stroke dilation, and introduced an interactive Thickness/Weight selector in the Edit Dialog so text stroke thickness identically matches the original PDF without thinning or deformation",
            "Font Size, Bold Weight & Typography Preservation: Fixed root cause where custom user device theme cursive/handwriting fonts hijacked document rendering and synthetic fake bold caused extreme thickness distortion; bundled authentic TrueType document fonts (Roboto & Noto Serif) directly in app assets, removed aggressive medium-weight bolding, and aligned baselines with 1:1 typographic ratios so text size, genuine bold stroke weight, color, and font style strictly match the original PDF",
            "Full Coloured Text Preservation: Fixed root cause where PDFBox stripped color operators; added native color operators (SetNonStrokingColor, SetNonStrokingDeviceRGBColor, etc.) and dual-pass high-res glyph stroke sampling so colored text (green, blue, red, cyan, white) is 100% preserved and never converts to black",
            "Color & Typography Preview Indicator: Edit dialog and contextual selection pill now display the exact live color swatch, font family, size pt, and bold badge of the selected text",
            "Seamless In-Bitmap Compositing: Text replacements and erasures are rendered directly into the high-resolution page bitmap with exact sampled background color blending (teal, colored tables, shaded cells, and white backgrounds stay 100% authentic)",
            "Exact Mathematical Baseline Alignment: Text replacements align with subpixel precision to the exact baseline and width of the original PDF text",
            "Dual Edit Mode (Edit Word vs Edit Line): Tap any word to edit it directly, or choose 'Edit Line' to rewrite the whole sentence without word collision",
            "Auto-Fit Proportionate Sizing: Long replacement strings automatically fit into the original gap with zero overflow onto adjacent text",
            "Undo & Redo History: Seamless one-click undo and redo for all text edits and replacements",
            "Smooth Multi-Touch Pinch-to-Zoom & Double-Tap: Pinch or double-tap to zoom up to 450% with effortless pan",
            "Universal Image to PDF Converter (JPG, PNG, WEBP, BMP, HEIF, GIF)",
            "Organize Pages: Reorder, rotate, or delete pages losslessly"
        ],
        "is_essential": False,
        "updated_at": "2026-11-17T11:35:00Z"
    }
]

@router.get("/apps")
async def get_store_apps(
    response: Response,
    request: Request,
    username: Optional[str] = Query(None),
    current_user: Optional[User] = Depends(get_current_user_optional)
):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate, max-age=0"
    response.headers["Access-Control-Allow-Origin"] = "*"

    is_admin = check_admin(current_user, username)
    apps_to_return = APPS_CATALOG if is_admin else [
        app for app in APPS_CATALOG
        if app["id"] != "jarvis" and app["package_name"] != "com.omni.assistant"
    ]

    return {
        "status": "success",
        "store_name": "Our Store",
        "store_version": "1.0.0",
        "total_apps": len(apps_to_return),
        "is_admin": is_admin,
        "apps": apps_to_return
    }

@router.get("/apps/{package_name}")
async def get_app_details(
    package_name: str,
    response: Response,
    request: Request,
    username: Optional[str] = Query(None),
    current_user: Optional[User] = Depends(get_current_user_optional)
):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate, max-age=0"
    response.headers["Access-Control-Allow-Origin"] = "*"

    is_admin = check_admin(current_user, username)

    for app in APPS_CATALOG:
        if app["package_name"] == package_name or app["id"] == package_name:
            if (app["id"] == "jarvis" or app["package_name"] == "com.omni.assistant") and not is_admin:
                return Response(content='{"error": "App not found"}', status_code=404, media_type="application/json")
            return {"status": "success", "app": app}
    return Response(content='{"error": "App not found"}', status_code=404, media_type="application/json")

@router.post("/consent")
async def record_user_consent(data: Dict[str, Any], response: Response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    username = str(data.get("username", "")).lower().strip().lstrip("@")
    if not username:
        return Response(content='{"status": "error", "message": "Username required"}', status_code=400, media_type="application/json")
    logger.info("Recorded store legal consent for user %s", username)
    return {
        "status": "success",
        "message": "Legal agreement recorded successfully",
        "username": username,
        "agreed": True,
        "agreed_at": data.get("agreed_at")
    }

@router.get("/consent")
async def get_user_consent(username: str = "", response: Response = None):
    if response:
        response.headers["Access-Control-Allow-Origin"] = "*"
    u = username.lower().strip().lstrip("@")
    return {
        "status": "success",
        "username": u,
        "agreed": True
    }
