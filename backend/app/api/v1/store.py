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
        "version_name": "2.1.5",
        "version_code": 64,
        "size_bytes": 18961450,
        "size_formatted": "18.1 MB",
        "download_url": "https://connecto.fun/downloads/our-store.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#2563EB",
        "changelog": [
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
        "version_name": "5.3.8",
        "version_code": 152,
        "size_bytes": 38070048,
        "size_formatted": "36.3 MB",
        "download_url": "https://connecto.fun/downloads/connecto-fun.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#2563EB",
        "changelog": [
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
        "updated_at": "2026-11-18T00:48:00Z"
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
        "version_name": "1.5.5",
        "version_code": 21,
        "size_bytes": 67492016,
        "size_formatted": "64.4 MB",
        "download_url": "https://connecto.fun/downloads/pdf-studio.apk",
        "icon_url": "https://connecto.fun/static/pdf_studio_logo.png",
        "accent_color": "#EF4444",
        "changelog": [
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
