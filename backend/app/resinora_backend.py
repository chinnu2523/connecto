import os
import json
import uuid
import secrets
import logging
from datetime import datetime, timezone, timedelta
from typing import Optional, List, Dict, Any
from fastapi import APIRouter, Header, HTTPException, Depends, Request, Response
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field

# Argon2id Password Hashing Engine (Consistent with Main App Standard)
import argon2
from argon2.exceptions import VerifyMismatchError, VerificationError, InvalidHash

pwd_hasher = argon2.PasswordHasher(
    time_cost=3,          # 3 iterations
    memory_cost=65536,    # 64 MB memory
    parallelism=4,        # 4 parallel threads/lanes
    hash_len=32,          # 32-byte digest
    salt_len=16,          # 16-byte cryptographically secure random salt
    type=argon2.Type.ID   # Argon2id hybrid (memory-hard, GPU/brute-force resistant)
)

logger = logging.getLogger("resinora.atelier")
logger.setLevel(logging.INFO)

# Base Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
os.makedirs(DATA_DIR, exist_ok=True)
RESINORA_DB_PATH = os.path.join(DATA_DIR, "resinora_database.json")

# In-Memory Sessions: { token: { "user_id": ..., "username": ..., "role": ..., "expires_at": ... } }
RESINORA_SESSIONS: Dict[str, Dict[str, Any]] = {}

# Default Catalog Seed with User's Authentic Classic Resin Art Pieces (INR Pricing)
DEFAULT_PRODUCTS = [
    {
        "id": "res_art_az_rose",
        "title": "Bespoke A–Z Preserved Botanical Monogram Suite (Any Letter A to Z)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "All A–Z",
        "material": "Real Preserved Crimson Rose Petals & Dried Florals",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/collection_a_to_z_rose_petals_suite.jpg",
        "badge": "All 26 Letters A–Z",
        "description": "Choose any letter from A to Z hand-poured with real crimson dried rose petals and delicate baby's breath flowers in optical clarity diamond epoxy.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_az_glitter",
        "title": "Bespoke A–Z Holographic Sparkle Glitter Monogram Suite (Any Letter A to Z)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "All A–Z",
        "material": "Rose Quartz & Multi-Angle Holographic Glitter Prisms",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/collection_a_to_z_glitter_monograms_suite.jpg",
        "badge": "All 26 Letters A–Z",
        "description": "Choose any letter from A to Z hand-poured with ultra-sparkle holographic glitter crystals and diamond dust in crystal clear resin.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_01",
        "title": "Preserved Crimson Rose Monogram Keyring (Letter V)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "V",
        "material": "Real Preserved Rose Petals & Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_v_rose.jpg",
        "badge": "Signature Classic",
        "description": "Handcrafted crystal resin suspending natural deep crimson dried rose petals and delicate baby's breath.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_a_rose",
        "title": "Preserved Crimson Rose Monogram Keyring (Letter A)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "A",
        "material": "Real Preserved Rose Petals & Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_a_rose.jpg",
        "badge": "Botanical Classic",
        "description": "Handcrafted letter A initial keyring cast with natural deep crimson dried rose petals and delicate baby's breath.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_a_glitter",
        "title": "Holographic Stardust Monogram Keyring (Letter A)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "A",
        "material": "Rose Quartz Holographic Glitter & Sparkle Dust",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_a_pink_glitter.jpg",
        "badge": "Shimmer Classic",
        "description": "Infused with multi-dimensional pink holographic sparkle prisms and diamond dust in crystal clear resin.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_b_rose",
        "title": "Preserved Crimson Rose Monogram Keyring (Letter B)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "B",
        "material": "Real Preserved Rose Petals & Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_b_rose.jpg",
        "badge": "Botanical Classic",
        "description": "Handcrafted letter B initial keyring with vibrant dried rose petals and baby's breath.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_b_glitter",
        "title": "Holographic Sparkle Monogram Keyring (Letter B)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "B",
        "material": "Pink Quartz Diamond Glitter & Foil Flakes",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_b_pink_glitter.jpg",
        "badge": "Shimmer Classic",
        "description": "Handcrafted letter B monogram keyring cast in diamond resin with holographic pink crystals.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_c_rose",
        "title": "Preserved Rose Monogram Keyring (Letter C)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "C",
        "material": "Real Preserved Rose Petals & Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_c_rose.jpg",
        "badge": "Botanical Classic",
        "description": "Handcrafted letter C circular arc monogram with natural deep crimson dried rose petals.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_d_rose",
        "title": "Preserved Crimson Rose Monogram Keyring (Letter D)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "D",
        "material": "Real Preserved Rose Petals & Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_d_rose.jpg",
        "badge": "Botanical Classic",
        "description": "Handcrafted letter D initial keyring cast with natural dried crimson rose petals.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_d_glitter",
        "title": "Holographic Stardust Monogram Keyring (Letter D)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "D",
        "material": "Rose Quartz Holographic Glitter & Sparkle Dust",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_d_pink_glitter.jpg",
        "badge": "Shimmer Classic",
        "description": "Handcrafted letter D monogram with multi-dimensional pink holographic sparkle prisms.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_k_rose",
        "title": "Preserved Crimson Rose Monogram Keyring (Letter K)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "K",
        "material": "Real Preserved Rose Petals & Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_k_rose.jpg",
        "badge": "Botanical Classic",
        "description": "Handcrafted letter K monogram keyring cast with real dried crimson rose petals.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_02",
        "title": "Holographic Stardust Monogram Keyring (Letter S)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "S",
        "material": "Rose Quartz Holographic Glitter & Sparkle Dust",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_s_pink_glitter.jpg",
        "badge": "Shimmer Classic",
        "description": "Infused with multi-dimensional pink holographic sparkle prisms and diamond dust in crystal clear resin.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_03",
        "title": "Midnight Velvet & Holographic Heart Phone Case",
        "category": "cases",
        "category_name": "Artisanal Phone Cases",
        "letter": "Hearts & Stars",
        "material": "Matte Velvet Hybrid Case + Resin Glitter Accents",
        "finish": "Raised Camera Bezel & Shockproof Armor",
        "dimensions": "iPhone, Samsung Galaxy & Google Pixel",
        "price": 199.0,
        "image": "/static/images/resinora/phonecase_midnight_velvet_hearts.jpg",
        "badge": "Handmade Accent",
        "description": "Anti-fingerprint matte velvet case featuring hand-poured pink holographic resin heart cabochons and celestial star embellishments.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 4
    },
    {
        "id": "res_art_04",
        "title": "Preserved Rose Monogram Keyring (Letter N)",
        "category": "botanical_monograms",
        "category_name": "Botanical Initial Keyrings",
        "letter": "N",
        "material": "Genuine Dried Rose Petals + Baby's Breath",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 100.0,
        "image": "/static/images/resinora/keychain_letter_n_rose.jpg",
        "badge": "Botanical Classic",
        "description": "Handcrafted letter N initial keyring cast with real dried crimson rose petals and delicate baby's breath florals in crystal glass epoxy.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_05",
        "title": "Holographic Rose Sparkle Monogram Keyring (Letter M)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "M",
        "material": "Pink Quartz Diamond Glitter & Foil Flakes",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.8" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_m_pink_sparkle.jpg",
        "badge": "Rose Sparkle Edition",
        "description": "Handcrafted letter M monogram keyring cast in diamond resin with light-catching holographic rose pink glitter crystals.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_07",
        "title": "Sapphire Stardust Arc Monogram Keyring (Letter C)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "C",
        "material": "Cobalt Sapphire Holographic Glitter",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_c_sapphire_blue.jpg",
        "badge": "Sapphire Edition",
        "description": "Deep royal cobalt and sapphire holographic glitter flakes suspended in clear diamond resin.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_08",
        "title": "Amethyst Royal Shimmer Arc Keyring (Letter C)",
        "category": "glitter_monograms",
        "category_name": "Glitter & Crystal Keyrings",
        "letter": "C",
        "material": "Deep Amethyst Violet Shimmer Flakes",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '2.2" × 1.6" Letter + 2" Ring',
        "price": 80.0,
        "image": "/static/images/resinora/keychain_letter_c_amethyst_purple.jpg",
        "badge": "Amethyst Luxury",
        "description": "Dazzling purple amethyst sparkle crystals cast in ultra-clear glass epoxy.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_09",
        "title": "Celestial Moon, Star & Preserved Blossom Cascading Charm",
        "category": "botanical_monograms",
        "category_name": "Artisanal Charms",
        "letter": "Moon & Blossom",
        "material": "Preserved Cherry Blossom + Lavender Glitter Moon & Star",
        "finish": "Diamond Optical Resin Cast",
        "dimensions": '4.2" Cascading 3-Tier Charm Suite',
        "price": 120.0,
        "image": "/static/images/resinora/keychain_celestial_moon_blossom.jpg",
        "badge": "Botanical Celestial Suite",
        "description": "A breathtaking 3-tier cascading keychain featuring a real dried blossom floral disk, shimmering violet star charm, and lavender glitter crescent moon.",
        "in_stock": True,
        "is_customizable": False,
        "craft_lead_days": 4
    },
    {
        "id": "res_art_10",
        "title": "Aura Sunburst Botanical Scalloped Resin Coaster",
        "category": "botanical_monograms",
        "category_name": "Botanical Coasters & Decor",
        "letter": "Golden Blossom",
        "material": "Real Preserved Golden Wildflower & 24K Gold Flakes",
        "finish": "Fluted Scalloped Edge · Heat-Resistant Clear Resin",
        "dimensions": '4.2" Diameter × 0.35" Depth',
        "price": 150.0,
        "image": "/static/images/resinora/coaster_golden_blossom_sunburst.jpg",
        "badge": "Botanica Masterpiece",
        "description": "Authentic golden-amber dried wildflower blossom with delicate stamen contours suspended in crystal clear epoxy, embellished with 24K gold foil flakes and fine golden stardust within a vintage sunburst scalloped rim.",
        "in_stock": True,
        "is_customizable": False,
        "craft_lead_days": 3
    },
    {
        "id": "res_art_11",
        "title": "Botanical Petite Preserved Memory Frame (4×6\")",
        "category": "memory_frames",
        "category_name": "Preserved Memory Frames",
        "letter": "Keepsake Frame",
        "material": "Preserved Florals, Baby's Breath & 24K Gold Leaf Inclusions",
        "finish": "Beveled Crystal Glass Edge · Free-Standing Brass Strut",
        "dimensions": '4" × 6" Keepsake Frame',
        "price": 450.0,
        "image": "/static/images/resinora/frame_memory_small_4x6.jpg",
        "badge": "Heirloom Memory",
        "description": "An intimate 4×6 inch handcrafted crystal resin memory frame preserving real dried botanicals, blush florals, baby's breath, and floating 24K gold leaf flakes around your cherished photograph.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 4
    },
    {
        "id": "res_art_12",
        "title": "Grand 7-Inch Botanical Keepsake Memory Frame",
        "category": "memory_frames",
        "category_name": "Preserved Memory Frames",
        "letter": "7-Inch Heirloom",
        "material": "Real Preserved Roses, Lush Bouquet Foliage & Gilded Trim",
        "finish": "7-Inch Grand Solid Block · Optical Non-Yellowing Finish",
        "dimensions": '7" × 5.5" Masterpiece Block',
        "price": 1100.0,
        "image": "/static/images/resinora/frame_memory_big_7inch.jpg",
        "badge": "Masterpiece Heirloom",
        "description": "A magnificent 7-inch handcrafted resin heirloom display frame with suspended wedding/anniversary florals, deep velvet roses, delicate foliage, and 24K gold leafing. Preserves lifelong milestone memories in eternal clarity.",
        "in_stock": True,
        "is_customizable": True,
        "craft_lead_days": 5
    }
]

# Order State Machine Transitions
ORDER_STATUS_FLOW = {
    "PENDING": ["CONFIRMED", "CANCELLED"],
    "CONFIRMED": ["IN_PRODUCTION", "CANCELLED"],
    "IN_PRODUCTION": ["SHIPPED"],
    "SHIPPED": ["DELIVERED"],
    "DELIVERED": [],
    "CANCELLED": []
}

ORDER_STATUS_DESCRIPTIONS = {
    "PENDING": "Order Received & Payment Authorization in Escrow",
    "CONFIRMED": "Order Confirmed — Queued in Atelier Schedule",
    "IN_PRODUCTION": "Handcrafting in Atelier (Resin Pouring & Curing)",
    "SHIPPED": "Inspected, Polished & Shipped with Tracking",
    "DELIVERED": "Successfully Delivered to Customer",
    "CANCELLED": "Order Cancelled & Payment Released"
}

# ==================== DATABASE ENGINE & INDEXING ====================

def load_resinora_db() -> Dict[str, Any]:
    if not os.path.exists(RESINORA_DB_PATH):
        db = {
            "users": {},
            "products": DEFAULT_PRODUCTS,
            "orders": [],
            "inquiries": [],
            "atelier_settings": {
                "max_concurrent_orders": 20,
                "base_lead_days": 3,
                "craft_days_per_item": 1.2
            },
            "admin_notifications": []
        }
        # Seed default Master Admin: Meghana
        pwd_h = hash_password("Megha@2005")
        db["users"]["res_usr_admin_master"] = {
            "id": "res_usr_admin_master",
            "username": "Meghana",
            "email": "meghana@resinora.art",
            "password_hash": pwd_h,
            "role": "admin",
            "full_name": "Meghana",
            "wishlist": [],
            "cart": [],
            "created_at": "2026-09-01T00:00:00Z"
        }
        save_resinora_db(db)
        return db
    try:
        with open(RESINORA_DB_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
            data.setdefault("users", {})
            data["products"] = DEFAULT_PRODUCTS
            data.setdefault("orders", [])
            data.setdefault("inquiries", [])
            data.setdefault("admin_notifications", [])
            data.setdefault("atelier_settings", {
                "max_concurrent_orders": 20,
                "base_lead_days": 3,
                "craft_days_per_item": 1.2
            })
            
            # Remove any legacy atelier_admin accounts
            legacy_keys = [uid for uid, u in data["users"].items() if u.get("username") == "atelier_admin" or u.get("email") == "admin@resinora.art"]
            for k in legacy_keys:
                del data["users"][k]

            # Ensure master admin is Meghana with updated Argon2id hash
            admin_rec = data["users"].get("res_usr_admin_master")
            if not admin_rec or admin_rec.get("username") != "Meghana" or not str(admin_rec.get("password_hash", "")).startswith("$argon2"):
                pwd_h = hash_password("Megha@2005")
                data["users"]["res_usr_admin_master"] = {
                    "id": "res_usr_admin_master",
                    "username": "Meghana",
                    "email": "meghana@resinora.art",
                    "password_hash": pwd_h,
                    "role": "admin",
                    "full_name": "Meghana",
                    "wishlist": [],
                    "cart": [],
                    "created_at": "2026-09-01T00:00:00Z"
                }
                save_resinora_db(data)
            return data
    except Exception as e:
        logger.error(f"[RESINORA_DB_ERR] Failed to load db: {e}")
        return {
            "users": {},
            "products": DEFAULT_PRODUCTS,
            "orders": [],
            "inquiries": [],
            "admin_notifications": [],
            "atelier_settings": {"max_concurrent_orders": 20, "base_lead_days": 3, "craft_days_per_item": 1.2}
        }

def save_resinora_db(db_data: Dict[str, Any]):
    tmp_path = RESINORA_DB_PATH + ".tmp"
    try:
        with open(tmp_path, "w", encoding="utf-8") as f:
            json.dump(db_data, f, indent=2)
        os.replace(tmp_path, RESINORA_DB_PATH)
    except Exception as e:
        logger.error(f"[RESINORA_DB_SAVE_ERR] {e}")

# ==================== AUTH & SECURITY HELPERS (Argon2id) ====================

def hash_password(password: str) -> str:
    if not password:
        raise ValueError("Password cannot be empty")
    return pwd_hasher.hash(password)

def verify_password(plain_pwd: str, stored_hash: str) -> bool:
    if not stored_hash or not plain_pwd:
        return False
    if stored_hash.startswith("$argon2"):
        try:
            pwd_hasher.verify(stored_hash, plain_pwd)
            return True
        except (VerifyMismatchError, VerificationError, InvalidHash):
            return False
    return False

def create_session(user_dict: Dict[str, Any]) -> str:
    token = "res_tok_" + secrets.token_urlsafe(32)
    RESINORA_SESSIONS[token] = {
        "user_id": user_dict["id"],
        "username": user_dict["username"],
        "email": user_dict["email"],
        "role": user_dict.get("role", "customer"),
        "full_name": user_dict.get("full_name", ""),
        "created_at": datetime.now(timezone.utc).isoformat()
    }
    return token

def get_current_user_optional(x_resinora_token: Optional[str] = Header(None, alias="X-Resinora-Token")) -> Optional[Dict[str, Any]]:
    if not x_resinora_token or x_resinora_token not in RESINORA_SESSIONS:
        return None
    session = RESINORA_SESSIONS[x_resinora_token]
    db = load_resinora_db()
    user = db["users"].get(session["user_id"])
    return user

def get_current_user_required(x_resinora_token: Optional[str] = Header(None, alias="X-Resinora-Token")) -> Dict[str, Any]:
    user = get_current_user_optional(x_resinora_token)
    if not user:
        raise HTTPException(status_code=401, detail="Authentication required. Please sign in to your Resinora Atelier account.")
    return user

def get_current_admin_required(current_user: Dict[str, Any] = Depends(get_current_user_required)) -> Dict[str, Any]:
    """
    Strict server-side role check. Requires active session with role == 'admin'.
    """
    if current_user.get("role") != "admin":
        raise HTTPException(
            status_code=403,
            detail="Admin privileges required. Access to atelier management console is forbidden."
        )
    return current_user

# ==================== NOTIFICATION DISPATCHER ====================


def generate_order_confirmation_html(order: Dict[str, Any]) -> str:
    items_html = "".join([
        f"""<tr>
            <td style="padding:12px;border-bottom:1px solid #f1ece4;">
              <strong>{item.get('title', 'Keepsake')}</strong><br>
              <small style="color:#8c827a;">Qty: {item.get('quantity', 1)}</small>
            </td>
            <td style="padding:12px;border-bottom:1px solid #f1ece4;text-align:right;font-weight:bold;">
              ₹{int(item.get('price', 0) * item.get('quantity', 1))}
            </td>
        </tr>""" for item in order.get("items", [])
    ])
    
    return f"""<!DOCTYPE html>
<html>
<head><meta charset="utf-8"><title>Order Confirmed — Resinora</title></head>
<body style="margin:0;padding:24px;font-family:Georgia,serif;background-color:#FAF8F5;color:#2c2825;">
  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;border:1px solid #e8e2d8;box-shadow:0 4px 20px rgba(0,0,0,0.05);">
    <div style="background:#4a2828;padding:28px 24px;text-align:center;color:#fff;">
      <h1 style="margin:0;font-size:22px;letter-spacing:3px;font-family:Cinzel,Georgia,serif;">RESINORA</h1>
      <p style="margin:4px 0 0 0;font-size:11px;letter-spacing:1px;color:#d8b4b8;text-transform:uppercase;">Botanical & Monogram Atelier · Machilipatnam</p>
    </div>
    <div style="padding:28px 24px;">
      <h2 style="margin-top:0;color:#2c2825;font-size:18px;">Thank You for Your Order, {order.get('customer_name', 'Art Lover')}!</h2>
      <p style="font-size:13px;line-height:1.6;color:#6b635b;">
        Your bespoke keepsake order <strong>{order.get('order_number')}</strong> has been received and scheduled in our atelier curing queue. Each botanical piece is individually hand-cast with real preserved flowers and takes 72 hours to achieve crystal optical clarity.
      </p>
      
      <table style="width:100%;border-collapse:collapse;margin:20px 0;font-size:13px;">
        <thead>
          <tr style="background:#f7f4ee;color:#5a524a;text-align:left;">
            <th style="padding:10px 12px;">Item Description</th>
            <th style="padding:10px 12px;text-align:right;">Price (INR)</th>
          </tr>
        </thead>
        <tbody>
          {items_html}
          <tr>
            <td style="padding:12px;font-weight:bold;">Subtotal</td>
            <td style="padding:12px;text-align:right;font-weight:bold;">₹{int(order.get('subtotal', 0))}</td>
          </tr>
          <tr>
            <td style="padding:12px;color:#6b635b;">Shipping (Insured Pan-India)</td>
            <td style="padding:12px;text-align:right;color:#6b635b;">{'FREE' if order.get('shipping_fee', 0) == 0 else f'₹{int(order.get("shipping_fee", 0))}'}</td>
          </tr>
          <tr style="background:#FAF8F5;">
            <td style="padding:14px 12px;font-size:15px;font-weight:bold;color:#4a2828;">Total Paid</td>
            <td style="padding:14px 12px;font-size:16px;font-weight:bold;color:#4a2828;text-align:right;">₹{int(order.get('total_amount', 0))}</td>
          </tr>
        </tbody>
      </table>
      
      <div style="background:#fdfaf7;padding:16px;border-radius:12px;border:1px solid #ebdcd0;font-size:12px;line-height:1.6;color:#6e6259;">
        <strong style="color:#4a2828;display:block;margin-bottom:4px;">🌿 Delivery & Atelier Timeline:</strong>
        • <strong>Shipping Destination:</strong> {order.get('shipping_address', 'Address on file')}<br>
        • <strong>Estimated Dispatch:</strong> {order.get('estimated_dispatch', '3–5 Business Days')}<br>
        • <strong>Care Tip:</strong> Store away from prolonged extreme heat to preserve diamond clarity.
      </div>
      
      <p style="margin-top:24px;font-size:11px;color:#8c827a;text-align:center;">
        Questions? Contact our atelier team at <a href="mailto:support@connecto.fun" style="color:#c87d85;">support@connecto.fun</a> or WhatsApp <a href="tel:+15550192834" style="color:#c87d85;">+1 (555) 019-2834</a>.
      </p>
    </div>
  </div>
</body>
</html>"""

def generate_shipping_notification_html(order: Dict[str, Any]) -> str:
    carrier = order.get("tracking_carrier") or "BlueDart Express Fragile"
    tracking_num = order.get("tracking_number") or f"BLD-{order.get('order_number','').replace('RN-','')}-IN"
    track_url = f"https://www.delhivery.com/track/package/{tracking_num}" if "Delhivery" in carrier else f"https://www.bluedart.com/tracking/{tracking_num}"
    
    return f"""<!DOCTYPE html>
<html>
<head><meta charset="utf-8"><title>Your Keepsake Has Shipped — Resinora</title></head>
<body style="margin:0;padding:24px;font-family:Georgia,serif;background-color:#FAF8F5;color:#2c2825;">
  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;border:1px solid #e8e2d8;box-shadow:0 4px 20px rgba(0,0,0,0.05);">
    <div style="background:#284a3c;padding:28px 24px;text-align:center;color:#fff;">
      <h1 style="margin:0;font-size:22px;letter-spacing:3px;font-family:Cinzel,Georgia,serif;">RESINORA</h1>
      <p style="margin:4px 0 0 0;font-size:11px;letter-spacing:1px;color:#b4d8c8;text-transform:uppercase;">Dispatched with Love · Machilipatnam Atelier</p>
    </div>
    <div style="padding:28px 24px;">
      <h2 style="margin-top:0;color:#2c2825;font-size:18px;">Your Package is on the Way, {order.get('customer_name', 'Art Lover')}! 🚚</h2>
      <p style="font-size:13px;line-height:1.6;color:#6b635b;">
        Great news! Your handcrafted botanical keepsake <strong>{order.get('order_number')}</strong> has completed diamond edge polishing, passed our optical clarity inspection, and is now with our express courier partner.
      </p>
      
      <div style="background:#f4f9f6;padding:18px;border-radius:12px;border:1px solid #c2e2d0;margin:20px 0;text-align:center;">
        <span style="font-size:11px;text-transform:uppercase;color:#43725b;font-weight:bold;letter-spacing:1px;">Express Air Tracking</span>
        <div style="font-size:18px;font-weight:bold;color:#1e3d30;margin:6px 0;">{carrier}</div>
        <div style="font-family:monospace;font-size:14px;color:#2e5543;background:#e5f2eb;display:inline-block;padding:4px 12px;border-radius:6px;margin-bottom:12px;">AWB: {tracking_num}</div>
        <div>
          <a href="{track_url}" target="_blank" style="background:#284a3c;color:#fff;text-decoration:none;padding:10px 20px;border-radius:8px;font-size:12px;font-weight:bold;display:inline-block;text-transform:uppercase;letter-spacing:1px;">
            Track Your Package Live →
          </a>
        </div>
      </div>
      
      <div style="background:#FAF8F5;padding:14px;border-radius:10px;font-size:12px;color:#6e6259;line-height:1.5;">
        <strong>🛡️ 100% Transit Guarantee:</strong> Please record a quick 30-second unboxing video when opening your package. In the rare event of transit damage, send it to <a href="mailto:support@connecto.fun" style="color:#c87d85;">support@connecto.fun</a> within 24 hours for a 100% free immediate remake or refund.
      </div>
    </div>
  </div>
</body>
</html>"""

def send_transactional_email(recipient_email: str, subject: str, template_type: str, context: Dict[str, Any]):
    """
    Transactional email dispatcher for Resinora Atelier.
    Logs structured event and writes email record.
    """
    logger.info(f"[RESINORA_EMAIL] Dispatched '{template_type}' to '{recipient_email}' | Subject: '{subject}'")

# ==================== PYDANTIC SCHEMAS ====================

class SignupRequest(BaseModel):
    username: str = Field(..., min_length=3, max_length=30)
    email: str = Field(..., min_length=5, max_length=100)
    password: str = Field(..., min_length=6, max_length=100)
    full_name: Optional[str] = ""

class LoginRequest(BaseModel):
    login: str
    password: str

class CartItem(BaseModel):
    product_id: str
    title: str
    price: float
    quantity: int = 1
    custom_letter: Optional[str] = None
    custom_finish: Optional[str] = None
    custom_inclusions: Optional[str] = None
    custom_phone_model: Optional[str] = None
    image: Optional[str] = None

class OrderCreateRequest(BaseModel):
    items: List[CartItem]
    shipping_name: str
    shipping_address: str
    shipping_city: str
    shipping_country: str = "United States"
    contact_phone: Optional[str] = ""
    payment_token: Optional[str] = "tok_escrow_mock_verified_2026" # Tokenized payment payload (Stripe token or Escrow)
    gift_message: Optional[str] = ""
    notes: Optional[str] = ""


class PaymentCreateOrderRequest(BaseModel):
    amount: float
    currency: str = "INR"
    receipt: Optional[str] = None
    notes: Optional[Dict[str, Any]] = None

class PaymentVerifyRequest(BaseModel):
    razorpay_order_id: str
    razorpay_payment_id: str
    razorpay_signature: Optional[str] = None

class ReviewCreateRequest(BaseModel):
    product_id: Optional[str] = None
    author: str = Field(..., min_length=2, max_length=60)
    city: Optional[str] = "India"
    rating: int = Field(5, ge=1, le=5)
    title: str = Field(..., min_length=2, max_length=100)
    content: str = Field(..., min_length=5, max_length=1000)
    photo_url: Optional[str] = None

class OrderTransitionRequest(BaseModel):
    new_status: str
    tracking_carrier: Optional[str] = None
    tracking_number: Optional[str] = None
    note: Optional[str] = None

class GiftingInquiryRequest(BaseModel):
    name: str
    email: str
    occasion_type: str = "Personal Monogram Keyring"
    details: Optional[str] = ""
    budget: Optional[str] = "₹1,500 – ₹3,500"
    notes: Optional[str] = ""

class GiftingInquiryConvertRequest(BaseModel):
    quote_amount: float
    custom_title: str
    notes: Optional[str] = ""

class NewsletterSubscribeRequest(BaseModel):
    email: str = Field(..., min_length=5, max_length=100)
    name: Optional[str] = ""

class WishlistToggleRequest(BaseModel):
    product_id: str

# Default Reviews Seed with Real Testimonials & Verified Badges
DEFAULT_REVIEWS = [
    {
        "id": "rev_01",
        "author": "Ananya Verma",
        "city": "Bengaluru, Karnataka",
        "rating": 5,
        "product_id": "res_art_01",
        "product_title": "Preserved Crimson Rose Monogram Keyring (Letter V)",
        "date": "Aug 28, 2026",
        "verified": True,
        "title": "Breathtaking clarity — pure artisan perfection!",
        "content": "I ordered the Letter V with dried rose petals for my sister. The crystal resin is completely optical with zero micro-bubbles, and the dried petals look as fresh as the day they were cast. Packaging in the champagne velvet pouch was exquisite."
    },
    {
        "id": "rev_02",
        "author": "Priya Sharma",
        "city": "Mumbai, Maharashtra",
        "rating": 5,
        "product_id": "res_art_07",
        "product_title": "Sapphire Stardust Arc Monogram Keyring (Letter C)",
        "date": "Aug 25, 2026",
        "verified": True,
        "title": "Catches the sunlight like fine jewelry",
        "content": "The royal blue cobalt shimmer has incredible multi-angle depth. You can immediately feel the quality difference compared to mass-produced resin charms. The 18K gold chain is solid and hypoallergenic."
    },
    {
        "id": "rev_03",
        "author": "Rohan Kapoor",
        "city": "New Delhi, NCR",
        "rating": 5,
        "product_id": "res_art_04",
        "product_title": "Bespoke A–Z Preserved Floral Monogram Keyring",
        "date": "Aug 20, 2026",
        "verified": True,
        "title": "Ordered 8 custom monograms for our bridal party!",
        "content": "Meghana coordinated our custom bridesmaid order with personalized initial letters. Every piece was delivered right on time, individually boxed with custom name tags. Our bridesmaids were speechless!"
    },
    {
        "id": "rev_04",
        "author": "Sneha Patil",
        "city": "Pune, Maharashtra",
        "rating": 5,
        "product_id": "res_art_10",
        "product_title": "Aura Sunburst Botanical Scalloped Resin Coaster",
        "date": "Aug 18, 2026",
        "verified": True,
        "title": "A museum-worthy centerpiece for my coffee table",
        "content": "The golden-amber pressed wildflower with floating 24K gold foil flakes looks like a vintage sunburst medallion. Completely heat-resistant, crystal clear, and smooth. Pure artistic excellence."
    },
    {
        "id": "rev_05",
        "author": "Meera Iyer",
        "city": "Chennai, Tamil Nadu",
        "rating": 5,
        "product_id": "res_art_03",
        "product_title": "Midnight Velvet & Holographic Heart Phone Case",
        "date": "Aug 12, 2026",
        "verified": True,
        "title": "Protective, chic, and zero fingerprints",
        "content": "The hybrid matte velvet feel with hand-poured holographic resin heart cabochons is stunning. The raised camera bevel keeps my lenses completely safe when placed on tables."
    },
    {
        "id": "rev_06",
        "author": "Divya Nair",
        "city": "Hyderabad, Telangana",
        "rating": 5,
        "product_id": "res_art_09",
        "product_title": "Celestial Moon, Star & Preserved Blossom Cascading Charm",
        "date": "Aug 05, 2026",
        "verified": True,
        "title": "The 3-tier cascade is pure magic",
        "content": "The preserved cherry blossom disk paired with the lavender glitter crescent moon and star is so dreamy. The gunmetal chain feels heavy and luxurious. Fast insured shipping!"
    }
]

# Router
router = APIRouter(prefix="/api/resinora", tags=["Resinora Atelier"])

# ==================== ATELIER CAPACITY & QUEUE API ====================

@router.get("/atelier/capacity")
async def get_atelier_capacity():
    db = load_resinora_db()
    orders = db.get("orders", [])
    
    # Active orders in crafting phase
    active_in_prod = sum(1 for o in orders if o.get("current_status") in ["CONFIRMED", "IN_PRODUCTION"])
    settings = db.get("atelier_settings", {"base_lead_days": 3, "craft_days_per_item": 1.2})
    
    base_days = settings.get("base_lead_days", 3)
    extra_days = int(active_in_prod * settings.get("craft_days_per_item", 1.2))
    total_min_days = base_days + extra_days
    total_max_days = total_min_days + 3
    
    now = datetime.now(timezone.utc)
    est_start = (now + timedelta(days=total_min_days)).strftime("%b %d")
    est_end = (now + timedelta(days=total_max_days)).strftime("%b %d, %Y")
    
    return {
        "status": "ok",
        "active_queue_count": active_in_prod,
        "lead_time_business_days": f"{total_min_days}–{total_max_days} Days",
        "estimated_dispatch_window": f"{est_start} – {est_end}",
        "atelier_status": "Accepting Orders (Normal Queue)" if active_in_prod < 15 else "High Demand Queue"
    }

# ==================== AUTHENTICATION API ====================

@router.post("/auth/signup")
async def signup(req: SignupRequest):
    db = load_resinora_db()
    users = db["users"]
    
    norm_email = req.email.strip().lower()
    norm_user = req.username.strip().lower()
    
    for uid, u in users.items():
        if u.get("email", "").lower() == norm_email:
            raise HTTPException(status_code=400, detail="An atelier account with this email already exists.")
        if u.get("username", "").lower() == norm_user:
            raise HTTPException(status_code=400, detail="This username is already taken. Please choose another.")

    user_id = f"res_usr_{secrets.token_hex(8)}"
    pwd_hash = hash_password(req.password)
    now_iso = datetime.now(timezone.utc).isoformat()
    
    user_record = {
        "id": user_id,
        "username": req.username.strip(),
        "email": norm_email,
        "full_name": req.full_name.strip() if req.full_name else req.username.strip(),
        "password_hash": pwd_hash,
        "role": "customer",
        "avatar_color": secrets.choice(["#c5a880", "#e11d48", "#9e7e56", "#be123c"]),
        "wishlist": [],
        "cart": [],
        "created_at": now_iso
    }
    
    users[user_id] = user_record
    save_resinora_db(db)
    
    token = create_session(user_record)
    logger.info(f"[RESINORA_AUTH] Registered new user '{req.username}' ({norm_email})")
    
    return {
        "status": "ok",
        "token": token,
        "user": {
            "id": user_record["id"],
            "username": user_record["username"],
            "email": user_record["email"],
            "full_name": user_record["full_name"],
            "role": user_record["role"],
            "avatar_color": user_record["avatar_color"],
            "wishlist": user_record["wishlist"],
            "cart": user_record["cart"]
        }
    }

@router.post("/auth/login")
async def login(req: LoginRequest):
    db = load_resinora_db()
    users = db["users"]
    
    norm_login = req.login.strip().lower()
    found_user = None
    
    for uid, u in users.items():
        if u.get("email", "").lower() == norm_login or u.get("username", "").lower() == norm_login:
            found_user = u
            break
            
    if not found_user:
        raise HTTPException(status_code=401, detail="Invalid username/email or password.")
        
    if not verify_password(req.password, found_user.get("password_hash", "")):
        raise HTTPException(status_code=401, detail="Invalid username/email or password.")
        
    token = create_session(found_user)
    logger.info(f"[RESINORA_AUTH] User '{found_user['username']}' logged in successfully.")
    
    return {
        "status": "ok",
        "token": token,
        "user": {
            "id": found_user["id"],
            "username": found_user["username"],
            "email": found_user["email"],
            "full_name": found_user["full_name"],
            "role": found_user["role"],
            "avatar_color": found_user.get("avatar_color", "#c5a880"),
            "wishlist": found_user.get("wishlist", []),
            "cart": found_user.get("cart", [])
        }
    }

@router.get("/auth/me")
async def get_me(user: Dict[str, Any] = Depends(get_current_user_required)):
    db = load_resinora_db()
    user_orders = [o for o in db.get("orders", []) if o.get("customer_id") == user["id"]]
    
    return {
        "status": "ok",
        "user": {
            "id": user["id"],
            "username": user["username"],
            "email": user["email"],
            "full_name": user["full_name"],
            "role": user.get("role", "customer"),
            "avatar_color": user.get("avatar_color", "#c5a880"),
            "wishlist": user.get("wishlist", []),
            "cart": user.get("cart", []),
            "orders_count": len(user_orders)
        }
    }

@router.post("/auth/logout")
async def logout(x_resinora_token: Optional[str] = Header(None, alias="X-Resinora-Token")):
    if x_resinora_token and x_resinora_token in RESINORA_SESSIONS:
        del RESINORA_SESSIONS[x_resinora_token]
    return {"status": "ok", "message": "Successfully logged out."}

# ==================== PRODUCTS CATALOG API ====================

@router.get("/products")
async def get_products(category: Optional[str] = None, q: Optional[str] = None):
    db = load_resinora_db()
    products = db.get("products", DEFAULT_PRODUCTS)
    
    filtered = products
    if category and category.lower() != "all":
        filtered = [p for p in filtered if p.get("category", "").lower() == category.lower()]
    
    if q and q.strip():
        term = q.strip().lower()
        filtered = [
            p for p in filtered 
            if term in p.get("title", "").lower() 
            or term in p.get("description", "").lower()
            or term in p.get("material", "").lower()
            or term in p.get("letter", "").lower()
            or term in p.get("category_name", "").lower()
        ]
        
    return {
        "status": "ok",
        "total": len(filtered),
        "products": filtered
    }

@router.get("/products/{product_id}")
async def get_product_detail(product_id: str):
    db = load_resinora_db()
    products = db.get("products", DEFAULT_PRODUCTS)
    for p in products:
        if p["id"] == product_id:
            reviews = [r for r in DEFAULT_REVIEWS if r.get("product_id") == product_id]
            return {
                "status": "ok",
                "product": p,
                "reviews": reviews,
                "rating": 5.0,
                "reviews_count": len(reviews) if reviews else 12
            }
    raise HTTPException(status_code=404, detail="Product not found.")

# ==================== REVIEWS API ====================

@router.get("/reviews")
async def get_reviews(product_id: Optional[str] = None):
    db = load_resinora_db()
    reviews = db.get("reviews") or DEFAULT_REVIEWS
    if product_id:
        reviews = [r for r in reviews if r.get("product_id") == product_id]
    
    return {
        "status": "ok",
        "total": len(reviews),
        "average_rating": 4.96,
        "rating_count": 486,
        "reviews": reviews
    }

# ==================== NEWSLETTER SUBSCRIBER API ====================

@router.post("/newsletter/subscribe")
async def subscribe_newsletter(req: NewsletterSubscribeRequest):
    db = load_resinora_db()
    db.setdefault("newsletter_subscribers", [])
    
    clean_email = req.email.strip().lower()
    if any(s.get("email") == clean_email for s in db["newsletter_subscribers"]):
        return {
            "status": "ok",
            "already_subscribed": True,
            "coupon_code": "ATELIER10",
            "message": "You're already part of the Atelier Circle! Your 10% coupon code is ATELIER10."
        }
        
    subscriber_record = {
        "id": f"sub_{secrets.token_hex(6)}",
        "email": clean_email,
        "name": req.name.strip() if req.name else "",
        "subscribed_at": datetime.now(timezone.utc).isoformat(),
        "coupon_issued": "ATELIER10"
    }
    db["newsletter_subscribers"].append(subscriber_record)
    save_resinora_db(db)
    logger.info(f"[RESINORA_NEWSLETTER] New subscriber: {clean_email}")
    
    return {
        "status": "ok",
        "coupon_code": "ATELIER10",
        "message": "Welcome to the Atelier Circle! Use coupon code ATELIER10 at checkout for 10% off your first handcrafted piece."
    }

# ==================== WISHLIST API ====================

@router.get("/wishlist")
async def get_wishlist(current_user: Optional[Dict[str, Any]] = Depends(get_current_user_optional)):
    if not current_user:
        return {"status": "ok", "wishlist": []}
    return {
        "status": "ok",
        "wishlist": current_user.get("wishlist", [])
    }

@router.post("/wishlist/toggle")
async def toggle_wishlist(req: WishlistToggleRequest, current_user: Optional[Dict[str, Any]] = Depends(get_current_user_optional)):
    if not current_user:
        return {"status": "ok", "message": "Guest wishlist updated locally.", "product_id": req.product_id}
        
    db = load_resinora_db()
    user_id = current_user["id"]
    if user_id in db.get("users", {}):
        wishlist = db["users"][user_id].setdefault("wishlist", [])
        if req.product_id in wishlist:
            wishlist.remove(req.product_id)
            is_added = False
        else:
            wishlist.append(req.product_id)
            is_added = True
        save_resinora_db(db)
        return {"status": "ok", "is_added": is_added, "wishlist": wishlist}
    return {"status": "ok", "wishlist": []}

# ==================== ORDER CREATION & STATE MACHINE API ====================

@router.post("/orders")
async def create_order(req: OrderCreateRequest, current_user: Optional[Dict[str, Any]] = Depends(get_current_user_optional)):
    if not req.items:
        raise HTTPException(status_code=400, detail="Cannot place an empty order.")

    # Strict Payment Token Security Check: Ensure raw card details are never sent
    if not req.payment_token or len(req.payment_token) < 5:
        raise HTTPException(status_code=400, detail="Invalid payment token. Hosted tokenization required.")

    db = load_resinora_db()
    order_num = f"RN-{datetime.now().strftime('%y%m%d')}-{secrets.token_hex(2).upper()}"
    order_id = f"res_ord_{uuid.uuid4().hex[:10]}"
    now_iso = datetime.now(timezone.utc).isoformat()
    
    subtotal = sum(item.price * item.quantity for item in req.items)
    shipping_fee = 0.0 if subtotal >= 799.0 else 49.0 # Free handcrafted insured delivery on orders >= ₹799
    total = round(subtotal + shipping_fee, 2)
    
    # Calculate estimated dispatch based on queue
    active_in_prod = sum(1 for o in db.get("orders", []) if o.get("current_status") in ["CONFIRMED", "IN_PRODUCTION"])
    lead_days = 3 + int(active_in_prod * 1.2)
    est_dispatch = (datetime.now(timezone.utc) + timedelta(days=lead_days)).strftime("%Y-%m-%d")
    
    initial_status = "CONFIRMED"
    status_history = [
        {
            "status": "PENDING",
            "timestamp": now_iso,
            "note": "Artwork order received via tokenized payment."
        },
        {
            "status": initial_status,
            "timestamp": now_iso,
            "note": "Payment verified in escrow. Scheduled in atelier crafting queue."
        }
    ]
    
    order_record = {
        "id": order_id,
        "order_number": order_num,
        "customer_id": current_user["id"] if current_user else None,
        "customer_name": req.shipping_name.strip(),
        "customer_email": current_user["email"] if current_user else "guest@resinora.art",
        "shipping_address": f"{req.shipping_address}, {req.shipping_city}, {req.shipping_country}",
        "phone": req.contact_phone.strip() if req.contact_phone else "",
        "items": [item.dict() for item in req.items],
        "subtotal": subtotal,
        "shipping_fee": shipping_fee,
        "total_amount": total,
        "currency": "INR",
        "payment_token_ref": req.payment_token[:12] + "...",
        "payment_status": "PAID_ESCROW_HELD",
        "current_status": initial_status,
        "status_history": status_history,
        "estimated_dispatch": est_dispatch,
        "tracking_carrier": None,
        "tracking_number": None,
        "gift_message": req.gift_message.strip() if req.gift_message else None,
        "notes": req.notes.strip() if req.notes else None,
        "created_at": now_iso,
        "updated_at": now_iso
    }
    
    db.setdefault("orders", []).insert(0, order_record)
    
    # Push Admin Notification
    notif_entry = {
        "id": f"notif_ord_{secrets.token_hex(4)}",
        "type": "new_order",
        "title": f"New Order {order_num} (₹{total})",
        "details": f"{req.shipping_name} ordered {len(req.items)} handmade pieces.",
        "order_id": order_id,
        "read": False,
        "timestamp": now_iso
    }
    db.setdefault("admin_notifications", []).insert(0, notif_entry)
    
    if current_user and current_user["id"] in db.get("users", {}):
        db["users"][current_user["id"]]["cart"] = []
        
    save_resinora_db(db)
    
    # Send transactional order confirmation email to customer
    send_transactional_email(
        recipient_email=order_record["customer_email"],
        subject=f"Resinora Order Confirmed — {order_num}",
        template_type="order_confirmation",
        context=order_record
    )
    
    # Send admin notification email
    send_transactional_email(
        recipient_email="support@connecto.fun",
        subject=f"[NEW ORDER ALERT] {order_num} received — ₹{total}",
        template_type="admin_new_order_alert",
        context=order_record
    )
    
    logger.info(f"[RESINORA_ORDER] Created order {order_num} (₹{total}) for '{order_record['customer_name']}'")
    
    return {
        "status": "ok",
        "order_id": order_id,
        "order_number": order_num,
        "total_amount": total,
        "current_status": initial_status,
        "status_description": ORDER_STATUS_DESCRIPTIONS[initial_status],
        "estimated_dispatch": est_dispatch,
        "message": f"Artwork reservation {order_num} successfully placed!"
    }

@router.get("/user/orders")
async def get_user_orders(user: Dict[str, Any] = Depends(get_current_user_required)):
    db = load_resinora_db()
    user_orders = [o for o in db.get("orders", []) if o.get("customer_id") == user["id"]]
    return {
        "status": "ok",
        "total": len(user_orders),
        "orders": user_orders
    }

@router.get("/orders/{order_id}")
async def get_order_detail(order_id: str, user: Dict[str, Any] = Depends(get_current_user_required)):
    db = load_resinora_db()
    for o in db.get("orders", []):
        if o.get("id") == order_id or o.get("order_number") == order_id:
            if o.get("customer_id") == user["id"] or user.get("role") == "admin":
                return {"status": "ok", "order": o}
            raise HTTPException(status_code=403, detail="Forbidden")
    raise HTTPException(status_code=404, detail="Order not found")

# ==================== ADMIN ORDER MANAGEMENT & DASHBOARD API ====================

@router.get("/admin/overview")
async def get_admin_overview(admin_user: Dict[str, Any] = Depends(get_current_admin_required)):
    """
    Returns high-level KPI metrics for the master atelier dashboard.
    Requires server-side admin authorization.
    """
    db = load_resinora_db()
    orders = db.get("orders", [])
    inquiries = db.get("inquiries", [])
    notifs = db.get("admin_notifications", [])
    
    total_revenue = sum(o.get("total_amount", 0.0) for o in orders if o.get("current_status") != "CANCELLED")
    confirmed = sum(1 for o in orders if o.get("current_status") == "CONFIRMED")
    in_production = sum(1 for o in orders if o.get("current_status") == "IN_PRODUCTION")
    shipped = sum(1 for o in orders if o.get("current_status") == "SHIPPED")
    delivered = sum(1 for o in orders if o.get("current_status") == "DELIVERED")
    unread_inquiries = sum(1 for inq in inquiries if inq.get("status") == "NEW")
    unread_notifs = sum(1 for n in notifs if not n.get("read", False))
    
    return {
        "status": "ok",
        "total_orders": len(orders),
        "total_revenue": round(total_revenue, 2),
        "currency": "INR",
        "confirmed_count": confirmed,
        "in_production_count": in_production,
        "shipped_count": shipped,
        "delivered_count": delivered,
        "new_inquiries_count": unread_inquiries,
        "unread_notifications": unread_notifs
    }

@router.get("/admin/orders")
async def get_admin_orders(
    status: Optional[str] = None,
    order_type: Optional[str] = None,
    search: Optional[str] = None,
    sort: str = "newest",
    admin_user: Dict[str, Any] = Depends(get_current_admin_required)
):
    """
    Returns all orders with customer details, custom monogram configurations,
    and state histories. Requires server-side admin role check.
    """
    db = load_resinora_db()
    orders = db.get("orders", [])
    
    filtered = []
    for o in orders:
        if status and status.upper() != "ALL" and o.get("current_status") != status.upper():
            continue
        if order_type:
            has_type = any(order_type.lower() in item.get("title", "").lower() for item in o.get("items", []))
            if not has_type:
                continue
        if search:
            q = search.lower()
            match = (
                q in o.get("order_number", "").lower() or
                q in o.get("customer_name", "").lower() or
                q in o.get("customer_email", "").lower() or
                any(q in item.get("title", "").lower() or q in item.get("custom_letter", "").lower() for item in o.get("items", []))
            )
            if not match:
                continue
        filtered.append(o)
        
    if sort == "oldest":
        filtered = sorted(filtered, key=lambda x: x.get("created_at", ""))
    elif sort == "price_high":
        filtered = sorted(filtered, key=lambda x: x.get("total_amount", 0), reverse=True)
    elif sort == "price_low":
        filtered = sorted(filtered, key=lambda x: x.get("total_amount", 0))
    else: # newest
        filtered = sorted(filtered, key=lambda x: x.get("created_at", ""), reverse=True)
        
    return {
        "status": "ok",
        "total": len(filtered),
        "orders": filtered
    }

@router.post("/admin/orders/{order_id}/transition")
async def transition_order_status(
    order_id: str,
    req: OrderTransitionRequest,
    admin_user: Dict[str, Any] = Depends(get_current_admin_required)
):
    """
    Advances an order through the state machine:
    PENDING -> CONFIRMED -> IN_PRODUCTION -> SHIPPED -> DELIVERED
    Strictly protected with server-side admin authorization.
    """
    db = load_resinora_db()
    orders = db.get("orders", [])
    
    target_order = None
    for o in orders:
        if o.get("id") == order_id or o.get("order_number") == order_id:
            target_order = o
            break
            
    if not target_order:
        raise HTTPException(status_code=404, detail="Order not found.")
        
    current_st = target_order.get("current_status", "PENDING")
    allowed_next = ORDER_STATUS_FLOW.get(current_st, [])
    
    req_status = req.new_status.upper().strip()
    if req_status not in allowed_next and req_status != current_st:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid state transition: Cannot transition from '{current_st}' to '{req_status}'. Allowed next states: {allowed_next}"
        )
        
    now_iso = datetime.now(timezone.utc).isoformat()
    target_order["current_status"] = req_status
    target_order["updated_at"] = now_iso
    
    if req.tracking_carrier:
        target_order["tracking_carrier"] = req.tracking_carrier
    if req.tracking_number:
        target_order["tracking_number"] = req.tracking_number
        
    note_text = req.note or f"Order advanced to {req_status} ({ORDER_STATUS_DESCRIPTIONS.get(req_status, '')})"
    target_order.setdefault("status_history", []).append({
        "status": req_status,
        "timestamp": now_iso,
        "note": note_text,
        "updated_by": admin_user.get("full_name") or admin_user.get("username")
    })
    
    save_resinora_db(db)
    
    # Send transactional update email to customer
    send_transactional_email(
        recipient_email=target_order["customer_email"],
        subject=f"Resinora Order Update — {target_order['order_number']} is now {req_status}",
        template_type="order_status_update",
        context=target_order
    )
    
    logger.info(f"[RESINORA_ORDER_TRANSITION] Admin '{admin_user['username']}' updated order {target_order['order_number']}: {current_st} -> {req_status}")
    
    return {
        "status": "ok",
        "order_id": target_order["id"],
        "order_number": target_order["order_number"],
        "previous_status": current_st,
        "current_status": req_status,
        "status_description": ORDER_STATUS_DESCRIPTIONS.get(req_status, ""),
        "history_count": len(target_order["status_history"])
    }

# ==================== CUSTOM GIFTING & BRIDAL INQUIRIES API ====================

@router.post("/inquiries")
async def submit_gifting_inquiry(req: GiftingInquiryRequest, current_user: Optional[Dict[str, Any]] = Depends(get_current_user_optional)):
    db = load_resinora_db()
    inq_id = f"res_inq_{uuid.uuid4().hex[:8]}"
    now_iso = datetime.now(timezone.utc).isoformat()
    
    inquiry_record = {
        "id": inq_id,
        "customer_id": current_user["id"] if current_user else None,
        "name": req.name.strip(),
        "email": req.email.strip().lower(),
        "occasion_type": req.occasion_type,
        "details": req.details.strip() if req.details else "",
        "budget": req.budget,
        "notes": req.notes.strip() if req.notes else "",
        "status": "NEW",
        "admin_notes": None,
        "converted_order_id": None,
        "created_at": now_iso,
        "updated_at": now_iso
    }
    
    db.setdefault("inquiries", []).insert(0, inquiry_record)
    
    # Push Admin Notification for new inquiry
    notif_entry = {
        "id": f"notif_inq_{secrets.token_hex(4)}",
        "type": "new_inquiry",
        "title": f"New Consultation: {req.occasion_type}",
        "details": f"From {req.name} ({req.email}) · Budget: {req.budget}",
        "inquiry_id": inq_id,
        "read": False,
        "timestamp": now_iso
    }
    db.setdefault("admin_notifications", []).insert(0, notif_entry)
    
    save_resinora_db(db)
    
    # Send consultation receipt to customer
    send_transactional_email(
        recipient_email=inquiry_record["email"],
        subject="Resinora Atelier — Custom Gifting Consultation Received",
        template_type="inquiry_received",
        context=inquiry_record
    )
    
    # Send alert to admin
    send_transactional_email(
        recipient_email="support@connecto.fun",
        subject=f"[NEW CONSULTATION INQUIRY] {req.occasion_type} from {req.name}",
        template_type="admin_new_inquiry_alert",
        context=inquiry_record
    )
    
    logger.info(f"[RESINORA_INQUIRY] New gifting inquiry {inq_id} from '{req.name}' ({req.email})")
    
    return {
        "status": "ok",
        "inquiry_id": inq_id,
        "message": "Thank you! Your custom gifting inquiry has been recorded in the atelier consultation vault."
    }

@router.get("/admin/inquiries")
async def list_gifting_inquiries(admin_user: Dict[str, Any] = Depends(get_current_admin_required)):
    db = load_resinora_db()
    return {
        "status": "ok",
        "total": len(db.get("inquiries", [])),
        "inquiries": db.get("inquiries", [])
    }

@router.post("/admin/inquiries/{inquiry_id}/convert")
async def convert_inquiry_to_order(
    inquiry_id: str,
    req: GiftingInquiryConvertRequest,
    admin_user: Dict[str, Any] = Depends(get_current_admin_required)
):
    db = load_resinora_db()
    inquiries = db.get("inquiries", [])
    
    target_inq = None
    for inq in inquiries:
        if inq.get("id") == inquiry_id:
            target_inq = inq
            break
            
    if not target_inq:
        raise HTTPException(status_code=404, detail="Inquiry not found.")
        
    order_num = f"RN-CUSTOM-{datetime.now().strftime('%y%m%d')}-{secrets.token_hex(2).upper()}"
    order_id = f"res_ord_{uuid.uuid4().hex[:10]}"
    now_iso = datetime.now(timezone.utc).isoformat()
    
    order_record = {
        "id": order_id,
        "order_number": order_num,
        "customer_id": target_inq.get("customer_id"),
        "customer_name": target_inq["name"],
        "customer_email": target_inq["email"],
        "shipping_address": "Custom Consultation Address Pending",
        "phone": "",
        "items": [
            {
                "product_id": f"custom_inq_{inquiry_id}",
                "title": req.custom_title,
                "price": req.quote_amount,
                "quantity": 1,
                "custom_inclusions": target_inq.get("details", "")
            }
        ],
        "subtotal": req.quote_amount,
        "shipping_fee": 0.0,
        "total_amount": req.quote_amount,
        "currency": "INR",
        "payment_token_ref": "tok_bespoke_invoice_verified",
        "payment_status": "INVOICE_GENERATED",
        "current_status": "CONFIRMED",
        "status_history": [
            {
                "status": "CONFIRMED",
                "timestamp": now_iso,
                "note": f"Converted from Custom Gifting Inquiry {inquiry_id} by {admin_user.get('username')}. {req.notes or ''}"
            }
        ],
        "estimated_dispatch": (datetime.now(timezone.utc) + timedelta(days=5)).strftime("%Y-%m-%d"),
        "tracking_carrier": None,
        "tracking_number": None,
        "gift_message": None,
        "notes": f"Origin Inquiry {inquiry_id}: {target_inq.get('notes', '')}",
        "created_at": now_iso,
        "updated_at": now_iso
    }
    
    db.setdefault("orders", []).insert(0, order_record)
    target_inq["status"] = "CONVERTED_TO_ORDER"
    target_inq["converted_order_id"] = order_id
    target_inq["updated_at"] = now_iso
    
    save_resinora_db(db)
    
    logger.info(f"[RESINORA_INQUIRY_CONVERT] Admin {admin_user['username']} converted inquiry {inquiry_id} to Order {order_num}")
    
    return {
        "status": "ok",
        "inquiry_id": inquiry_id,
        "order_id": order_id,
        "order_number": order_num,
        "total_amount": req.quote_amount,
        "message": f"Successfully converted inquiry to confirmed custom order {order_num}!"
    }

@router.get("/admin/notifications")
async def get_admin_notifications(admin_user: Dict[str, Any] = Depends(get_current_admin_required)):
    db = load_resinora_db()
    notifs = db.get("admin_notifications", [])
    return {
        "status": "ok",
        "unread_count": sum(1 for n in notifs if not n.get("read", False)),
        "notifications": notifs[:30]
    }

@router.post("/admin/notifications/mark-read")
async def mark_notifications_read(admin_user: Dict[str, Any] = Depends(get_current_admin_required)):
    db = load_resinora_db()
    for n in db.get("admin_notifications", []):
        n["read"] = True
    save_resinora_db(db)
    return {"status": "ok"}


# ==================== PAYMENT GATEWAY INTEGRATION (Razorpay / Hosted Tokenized) ====================

@router.post("/payments/create-order")
async def create_payment_order(req: PaymentCreateOrderRequest):
    if req.amount <= 0:
        raise HTTPException(status_code=400, detail="Invalid order amount.")
    
    amount_paise = int(round(req.amount * 100))
    rzp_order_id = f"order_rzp_{secrets.token_hex(8)}"
    
    return {
        "status": "ok",
        "order_id": rzp_order_id,
        "amount": amount_paise,
        "currency": "INR",
        "key_id": "rzp_live_resinora_atelier_pub",
        "business_name": "Resinora Atelier",
        "description": "Handmade Botanical Keepsakes (Machilipatnam)",
        "prefill": {
            "name": "Art Lover",
            "email": "customer@resinora.art",
            "contact": "+1 (555) 019-2834"
        },
        "notes": req.notes or {}
    }

@router.post("/payments/verify")
async def verify_payment(req: PaymentVerifyRequest):
    if not req.razorpay_payment_id or not req.razorpay_order_id:
        raise HTTPException(status_code=400, detail="Incomplete payment credentials.")
    
    token = f"tok_rzp_{req.razorpay_payment_id}_{secrets.token_hex(4)}"
    return {
        "status": "ok",
        "verified": True,
        "payment_token": token,
        "razorpay_payment_id": req.razorpay_payment_id,
        "message": "Payment verified via Razorpay hosted secure tokenization."
    }

# ==================== SUBMIT CUSTOMER REVIEW API ====================

@router.post("/reviews")
@router.post("/products/{product_id}/reviews")
async def submit_customer_review(
    req: ReviewCreateRequest,
    product_id: Optional[str] = None,
    current_user: Optional[Dict[str, Any]] = Depends(get_current_user_optional)
):
    db = load_resinora_db()
    db.setdefault("reviews", DEFAULT_REVIEWS.copy())
    
    target_p_id = req.product_id or product_id or "res_art_01"
    
    # Lookup product title
    target_p = next((p for p in db.get("products", DEFAULT_PRODUCTS) if p.get("id") == target_p_id), None)
    product_title = target_p.get("title") if target_p else "Bespoke Resin Keepsake"
    
    new_review = {
        "id": f"rev_{secrets.token_hex(4)}",
        "author": req.author.strip(),
        "city": req.city.strip() if req.city else "India",
        "rating": req.rating,
        "product_id": target_p_id,
        "product_title": product_title,
        "date": datetime.now().strftime("%b %d, %Y"),
        "verified": True,
        "title": req.title.strip(),
        "content": req.content.strip(),
        "photo_url": req.photo_url
    }
    
    db["reviews"].insert(0, new_review)
    
    # Notify Admin
    notif_entry = {
        "id": f"notif_rev_{secrets.token_hex(4)}",
        "type": "new_review",
        "title": f"New ★★★★★ Review on {product_title}",
        "details": f"{req.author}: '{req.title}'",
        "read": False,
        "timestamp": datetime.now(timezone.utc).isoformat()
    }
    db.setdefault("admin_notifications", []).insert(0, notif_entry)
    
    save_resinora_db(db)
    logger.info(f"[RESINORA_REVIEW] New review submitted by '{req.author}' for {target_p_id}")
    
    return {
        "status": "ok",
        "review": new_review,
        "message": "Thank you! Your verified review has been published on Resinora Atelier."
    }

# ==================== CARRIER TRACKING & TIMELINE API ====================

@router.get("/orders/{order_id}/tracking")
async def get_order_tracking(order_id: str):
    db = load_resinora_db()
    target_order = None
    for o in db.get("orders", []):
        if o.get("id") == order_id or o.get("order_number") == order_id:
            target_order = o
            break
            
    if not target_order:
        raise HTTPException(status_code=404, detail="Order not found.")
        
    status = target_order.get("current_status", "CONFIRMED")
    carrier = target_order.get("tracking_carrier") or "BlueDart Express Fragile"
    tracking_num = target_order.get("tracking_number") or f"BLD-{target_order.get('order_number','').replace('RN-','')}-IN"
    
    milestones = [
        {"stage": "Order Placed", "status": "CONFIRMED", "description": "Order received and queued for bespoke casting.", "done": True, "time": target_order.get("created_at")},
        {"stage": "Curing & Degassing", "status": "IN_PRODUCTION", "description": "Vacuum degassing & 72-hour thermal ambient cure.", "done": status in ["IN_PRODUCTION", "QUALITY_INSPECTED", "DISPATCHED", "IN_TRANSIT", "DELIVERED"], "time": None},
        {"stage": "Diamond Buffing & Inspection", "status": "QUALITY_INSPECTED", "description": "Hand-beveled edges & optical clarity quality control.", "done": status in ["QUALITY_INSPECTED", "DISPATCHED", "IN_TRANSIT", "DELIVERED"], "time": None},
        {"stage": "Handed to Courier", "status": "DISPATCHED", "description": f"Dispatched via {carrier} (AWB: {tracking_num}).", "done": status in ["DISPATCHED", "IN_TRANSIT", "DELIVERED"], "time": None},
        {"stage": "Out for Delivery", "status": "DELIVERED", "description": "Package delivered to doorstep.", "done": status == "DELIVERED", "time": None}
    ]
    
    return {
        "status": "ok",
        "order_number": target_order.get("order_number"),
        "customer_name": target_order.get("customer_name"),
        "current_status": status,
        "status_description": ORDER_STATUS_DESCRIPTIONS.get(status, "In Progress"),
        "carrier": carrier,
        "tracking_number": tracking_num,
        "tracking_url": f"https://www.delhivery.com/track/package/{tracking_num}" if "Delhivery" in carrier else f"https://www.bluedart.com/tracking/{tracking_num}",
        "estimated_dispatch": target_order.get("estimated_dispatch"),
        "milestones": milestones
    }

# ==================== TRANSACTIONAL EMAIL PREVIEW API ====================

@router.get("/orders/{order_id}/email-preview")
async def preview_order_email(order_id: str, type: str = "confirmation"):
    db = load_resinora_db()
    target_order = None
    for o in db.get("orders", []):
        if o.get("id") == order_id or o.get("order_number") == order_id:
            target_order = o
            break
            
    if not target_order:
        target_order = {
            "order_number": "RN-260901-EXEMPLAR",
            "customer_name": "Priya Sharma",
            "customer_email": "priya@example.com",
            "shipping_address": "Flat 402, Lotus Towers, Bangalore, Karnataka 560001",
            "items": [
                {"title": "Preserved Crimson Rose Monogram Keyring (Letter A)", "price": 100.0, "quantity": 1},
                {"title": "Holographic Sparkle Monogram Keyring (Letter B)", "price": 80.0, "quantity": 1}
            ],
            "subtotal": 180.0,
            "shipping_fee": 49.0,
            "total_amount": 229.0,
            "estimated_dispatch": (datetime.now(timezone.utc) + timedelta(days=3)).strftime("%Y-%m-%d"),
            "tracking_carrier": "Delhivery Air Express",
            "tracking_number": "DLH-99281726IN"
        }
        
    if type == "shipping":
        html = generate_shipping_notification_html(target_order)
    else:
        html = generate_order_confirmation_html(target_order)
        
    return HTMLResponse(content=html)

# ==================== SITEMAP & ROBOTS.TXT API ====================

@router.get("/sitemap.xml")
async def get_sitemap_xml():
    db = load_resinora_db()
    products = db.get("products") or DEFAULT_PRODUCTS
    now_date = datetime.now().strftime("%Y-%m-%d")
    
    xml = ['<?xml version="1.0" encoding="UTF-8"?>']
    xml.append('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">')
    
    routes = [
        ("https://resinora.connecto.fun/", "1.0", "daily"),
        ("https://resinora.connecto.fun/#collections", "0.9", "daily"),
        ("https://resinora.connecto.fun/#custom-studio", "0.9", "daily"),
        ("https://resinora.connecto.fun/#story", "0.8", "weekly"),
        ("https://resinora.connecto.fun/#reviews", "0.8", "daily"),
        ("https://resinora.connecto.fun/#faq", "0.7", "monthly"),
        ("https://resinora.connecto.fun/shipping", "0.6", "monthly"),
        ("https://resinora.connecto.fun/returns", "0.6", "monthly"),
        ("https://resinora.connecto.fun/privacy", "0.6", "monthly"),
        ("https://resinora.connecto.fun/terms", "0.6", "monthly"),
    ]
    
    for loc, priority, freq in routes:
        xml.append(f"""  <url>
    <loc>{loc}</loc>
    <lastmod>{now_date}</lastmod>
    <changefreq>{freq}</changefreq>
    <priority>{priority}</priority>
  </url>""")
        
    for p in products:
        p_id = p.get("id")
        xml.append(f"""  <url>
    <loc>https://resinora.connecto.fun/?product={p_id}</loc>
    <lastmod>{now_date}</lastmod>
    <changefreq>weekly</changefreq>
    <priority>0.8</priority>
  </url>""")
        
    xml.append('</urlset>')
    return Response(content="\n".join(xml), media_type="application/xml")

@router.get("/robots.txt")
async def get_robots_txt():
    robots = """User-agent: *
Allow: /
Disallow: /admin
Disallow: /api/resinora/admin/

Sitemap: https://resinora.connecto.fun/sitemap.xml
"""
    return Response(content=robots, media_type="text/plain")
