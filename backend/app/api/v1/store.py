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
        "description": "Professional PDF editor for Android. Select any text on the page with interactive zoom & pan, find & replace names in the exact same font and size, convert JPG/PNG/WEBP to PDF, and merge multiple PDF files losslessly.",
        "category": "Productivity",
        "version_name": "1.1.0",
        "version_code": 2,
        "size_bytes": 28369316,
        "size_formatted": "27.1 MB",
        "download_url": "https://connecto.fun/downloads/pdf-studio.apk",
        "icon_url": "https://connecto.fun/static/pdf_studio_logo.png",
        "accent_color": "#EF4444",
        "changelog": [
            "Pinpoint Text Selection: Tap any word, name, or sentence with instant contextual 'Edit' action",
            "Smooth Multi-Touch Pinch-to-Zoom & Pan: Inspect documents up to 400% magnification",
            "Same-Font Name & Text Replacement: Change names seamlessly in identical matching fonts and sizes",
            "Streamlined Clean UI: Removed drawing & annotate clutter for pure text editing experience",
            "Universal Image to PDF Converter (JPG, PNG, WEBP, BMP, HEIF, GIF)",
            "Lossless PDF merging and full page reorganization"
        ],
        "is_essential": False,
        "updated_at": "2026-10-29T21:15:00Z"
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
