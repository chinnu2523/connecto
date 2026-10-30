import logging
from typing import Dict, Any, List
from fastapi import APIRouter, Response

logger = logging.getLogger("store.api")
router = APIRouter(prefix="/api/v1/store", tags=["App Store"])

APPS_CATALOG: List[Dict[str, Any]] = [
    {
        "id": "connecto",
        "package_name": "com.connecto.app",
        "name": "Connecto",
        "badge": "Official Messaging",
        "tagline": "Private encrypted messaging, calls & workspaces",
        "description": "Ultra-secure end-to-end encrypted direct messaging, crystal-clear voice/video calls, custom workspace channels, and offline data sync.",
        "category": "Communication",
        "version_name": "4.6.8",
        "version_code": 97,
        "size_bytes": 37888000,
        "size_formatted": "36.1 MB",
        "download_url": "https://connecto.fun/downloads/connecto-fun.apk",
        "icon_url": "https://connecto.fun/static/favicon.svg",
        "accent_color": "#2563EB",
        "changelog": [
            "Our Store dynamic integration & updates",
            "Fixed direct message preview synchronization",
            "Enhanced biometrics & screen security",
            "Workspace channel presence optimizations"
        ],
        "is_essential": True,
        "updated_at": "2026-10-29T13:30:00Z"
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
        "version_name": "1.4.4",
        "version_code": 9,
        "size_bytes": 28316079,
        "size_formatted": "27.0 MB",
        "download_url": "https://connecto.fun/downloads/pdf-studio.apk",
        "icon_url": "https://connecto.fun/static/pdf_studio_logo.png",
        "accent_color": "#EF4444",
        "changelog": [
            "Pixel-Perfect Text Replacement Alignment: Fixed vertical coordinate shift bug so replacements and whiteouts cleanly envelope the exact target text without overlapping upper lines",
            "Automatic Color Extraction: Live extraction of exact original font colors (including custom theme colors) preserved across all edits",
            "Complete Whiteout Masking: Generous padding envelopes ascenders and descenders with zero underlying text bleed",
            "Original Formatting Preservation: Font family, size, color, and styles (bold/italic) are automatically locked and preserved from the PDF",
            "Focused Text Editing: Streamlined interface to edit only the text data without altering typography or colors",
            "Clean Document View: Removed all wireframe rectangle and square boxes for pristine viewing",
            "Exact-Fit Page Bounding: Fixed landscape and portrait aspect ratio alignment eliminating misplaced selections in empty space",
            "In-Page Text Tap Selection: Directly tap any text on the page to highlight and edit or erase it",
            "WYSIWYG Live Text Editing: Modifications and whiteouts render in real-time directly over the canvas",
            "Undo & Redo History: Seamless one-click undo and redo for all text edits and replacements",
            "1-Tap Erase / Whiteout: Cleanly remove unwanted text, numbers, or names from any page",
            "Smooth Multi-Touch Pinch-to-Zoom & Double-Tap: Pinch or double-tap to zoom up to 450% with effortless pan",
            "Universal Image to PDF Converter (JPG, PNG, WEBP, BMP, HEIF, GIF)",
            "Organize Pages: Reorder, rotate, or delete pages losslessly"
        ],
        "is_essential": False,
        "updated_at": "2026-10-30T08:45:00Z"
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
