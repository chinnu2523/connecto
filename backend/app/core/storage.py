import os
import uuid
from io import BytesIO
from PIL import Image
import puremagic
from fastapi import HTTPException, status
from app.core.config import settings

ALLOWED_MIME_TYPES = {"image/png", "image/jpeg", "image/webp"}

def validate_image_magic_bytes(file_bytes: bytes) -> str:
    """
    Validates actual file header / magic bytes using puremagic.
    Returns the detected MIME type or raises 400 Bad Request if invalid.
    """
    if not file_bytes:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="File content is empty."
        )

    try:
        matches = puremagic.from_string(file_bytes, mime=True)
        if isinstance(matches, str):
            mime_type = matches
        elif isinstance(matches, list) and len(matches) > 0:
            mime_type = matches[0]
        else:
            mime_type = ""
    except Exception:
        # Fallback inspection for PNG/JPEG/WEBP magic headers
        if file_bytes.startswith(b'\x89PNG\r\n\x1a\n'):
            mime_type = "image/png"
        elif file_bytes.startswith(b'\xff\xd8\xff'):
            mime_type = "image/jpeg"
        elif file_bytes.startswith(b'RIFF') and file_bytes[8:12] == b'WEBP':
            mime_type = "image/webp"
        else:
            mime_type = ""

    if mime_type not in ALLOWED_MIME_TYPES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid file format detected: '{mime_type or 'unknown'}'. Only JPEG, PNG, and WebP images are permitted."
        )

    return mime_type

def process_and_save_banner(file_bytes: bytes) -> str:
    """
    Validates magic bytes, center-crops and compresses image to 1200x400 WebP server-side,
    and saves to /uploads/banners/. Returns the public relative URL path.
    Max input size: 8 MB (checked by caller / FastAPI).
    """
    BANNER_W, BANNER_H = 1200, 400

    mime_type = validate_image_magic_bytes(file_bytes)

    try:
        image = Image.open(BytesIO(file_bytes))
        image.verify()
        image = Image.open(BytesIO(file_bytes))
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Corrupted or invalid image file: {str(e)}"
        )

    if image.mode not in ("RGB", "RGBA"):
        image = image.convert("RGBA" if "A" in image.mode else "RGB")

    # Center-crop to 1200x400 aspect ratio, then resize — preserves full width composition
    src_w, src_h = image.size
    target_ratio = BANNER_W / BANNER_H
    src_ratio = src_w / src_h

    if src_ratio > target_ratio:
        # Image is wider than needed — crop sides
        new_w = int(src_h * target_ratio)
        left = (src_w - new_w) // 2
        image = image.crop((left, 0, left + new_w, src_h))
    else:
        # Image is taller than needed — crop top/bottom
        new_h = int(src_w / target_ratio)
        top = (src_h - new_h) // 2
        image = image.crop((0, top, src_w, top + new_h))

    image = image.resize((BANNER_W, BANNER_H), Image.Resampling.LANCZOS)

    # Ensure RGB for WebP
    if image.mode == "RGBA":
        image = image.convert("RGB")

    banner_dir = os.path.join(settings.UPLOAD_DIR, "banners")
    os.makedirs(banner_dir, mode=0o755, exist_ok=True)
    try:
        os.chmod(banner_dir, 0o755)
    except Exception:
        pass

    filename = f"{uuid.uuid4().hex}.webp"
    filepath = os.path.join(banner_dir, filename)

    image.save(filepath, "WEBP", quality=82, optimize=True)
    try:
        os.chmod(filepath, 0o644)
    except Exception:
        pass

    return f"/uploads/banners/{filename}"


def process_and_save_avatar(file_bytes: bytes) -> str:
    """
    Validates magic bytes, resizes and compresses image to 512x512 WebP server-side,
    and saves to storage location. Returns the public relative URL path.
    """
    mime_type = validate_image_magic_bytes(file_bytes)

    try:
        image = Image.open(BytesIO(file_bytes))
        image.verify()  # Verify image integrity
        image = Image.open(BytesIO(file_bytes))  # Re-open after verify
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Corrupted or invalid image file: {str(e)}"
        )

    # Convert to RGB/RGBA if necessary
    if image.mode not in ("RGB", "RGBA"):
        image = image.convert("RGBA" if "A" in image.mode else "RGB")

    # Resize image preserving aspect ratio (max 512x512)
    image.thumbnail((512, 512), Image.Resampling.LANCZOS)

    # Create destination dir with explicit read & traverse permissions
    avatar_dir = os.path.join(settings.UPLOAD_DIR, "avatars")
    os.makedirs(avatar_dir, mode=0o755, exist_ok=True)
    try:
        os.chmod(avatar_dir, 0o755)
    except Exception:
        pass

    # Generate unique filename
    filename = f"{uuid.uuid4().hex}.webp"
    filepath = os.path.join(avatar_dir, filename)

    # Save compressed WebP image and enforce 0644 read permissions
    image.save(filepath, "WEBP", quality=85, optimize=True)
    try:
        os.chmod(filepath, 0o644)
    except Exception:
        pass

    return f"/uploads/avatars/{filename}"

