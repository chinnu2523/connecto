import logging
from typing import Dict, Any, List
from fastapi import APIRouter, Response

logger = logging.getLogger("store.api")
router = APIRouter(prefix="/api/v1/store", tags=["App Store"])

APPS_CATALOG: List[Dict[str, Any]] = [
    {
        "id": "ourstore",
        "package_name": "com.connecto.store",
        "name": "Our Store",
        "badge": "App Hub Engine",
        "tagline": "Autonomous package installer, background updater & security vault",
        "description": "Official app ecosystem client for Android. Features native 1-tap package installation via Android PackageInstaller session IPC, background app auto-updating, cryptographic SHA-256 validation, and pure OLED classic black UI.",
        "category": "System",
        "version_name": "1.6.3",
        "version_code": 20,
        "size_bytes": 13707111,
        "size_formatted": "13.1 MB",
        "download_url": "https://connecto.fun/downloads/our-store.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#FFFFFF",
        "changelog": [
            "JARVIS AI Preview Fix: Resolved issue where PDF Studio captures were shown in JARVIS previews; replaced with authentic voice reasoning, memory vault, and settings screenshots",
            "Connecto Guest Previews: Replaced administrator data with authentic non-admin guest profile screenshots (@guest)",
            "Legal & Privacy Governance: Added full Terms of Use and Zero-Telemetry Privacy Policy modal and footers",
            "De-Cluttered Catalog Cards: Removed all external diagnostic text, build numbers and version specs for a clean, human-designed aesthetic",
            "Real Application Previews: Replaced synthetic text mockups with authentic, full-resolution app screenshots and tap-to-zoom modal",
            "Interactive Swipe Navigation: Fluid finger-tracking horizontal pager sliding between Store, Updates, and Account screens",
            "Enterprise Security Vault: AES-256-GCM hardware key protection and SHA-256 integrity verification"
        ],
        "is_essential": True,
        "updated_at": "2026-10-31T17:15:00Z"
    },
    {
        "id": "connecto",
        "package_name": "com.connecto.app",
        "name": "Connecto",
        "badge": "Official Messaging",
        "tagline": "Private encrypted messaging, calls & workspaces",
        "description": "Ultra-secure end-to-end encrypted direct messaging, crystal-clear voice/video calls, custom workspace channels, and offline data sync.",
        "category": "Communication",
        "version_name": "4.6.15",
        "version_code": 104,
        "size_bytes": 37850000,
        "size_formatted": "36.1 MB",
        "download_url": "https://connecto.fun/downloads/connecto-fun.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#2563EB",
        "changelog": [
            "Interactive Horizontal Pager: Fluid finger-tracking swipe transitions across Home, Messages, Channels, Calls, and Profile tabs",
            "Touch Gestures Isolation: Auto-locks horizontal paging during direct 1-on-1 chats to prevent accidental navigation",
            "SSO ContentProvider Hardening: Restricted with signature-level permission and caller certificate verification",
            "Native Android System Face ID support via BiometricPrompt with passive glance-to-unlock"
        ],
        "is_essential": True,
        "updated_at": "2026-10-31T12:40:00Z"
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
        "badge": "PDF Editor & Converter",
        "tagline": "Change names in matching fonts, edit text & convert images to PDF",
        "description": "Professional PDF editor for Android. Smooth pinch-to-zoom & pan, edit anything on the page, tap any text or area to insert and replace text in matching fonts, and convert images to PDF.",
        "category": "Productivity",
        "version_name": "1.4.10",
        "version_code": 15,
        "size_bytes": 30873266,
        "size_formatted": "29.4 MB",
        "download_url": "https://connecto.fun/downloads/pdf-studio.apk",
        "icon_url": "https://connecto.fun/static/pdf_studio_logo.png",
        "accent_color": "#EF4444",
        "changelog": [
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
        "updated_at": "2026-10-30T11:45:00Z"
    }
]

@router.get("/apps")
async def get_store_apps(response: Response):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate, max-age=0"
    response.headers["Access-Control-Allow-Origin"] = "*"
    return {
        "status": "success",
        "store_name": "Our Store",
        "store_version": "1.0.0",
        "total_apps": len(APPS_CATALOG),
        "apps": APPS_CATALOG
    }

@router.get("/apps/{package_name}")
async def get_app_details(package_name: str, response: Response):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate, max-age=0"
    response.headers["Access-Control-Allow-Origin"] = "*"
    for app in APPS_CATALOG:
        if app["package_name"] == package_name or app["id"] == package_name:
            return {"status": "success", "app": app}
    return Response(content='{"error": "App not found"}', status_code=404, media_type="application/json")
