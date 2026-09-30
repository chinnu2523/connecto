import os
import base64
import hmac
import hashlib
import logging
from typing import Optional, List
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from sqlalchemy.types import TypeDecorator, String
from app.core.config import settings

logger = logging.getLogger("connecto.encryption")

ENC_PREFIX = "enc:v1:aesgcm:"

def _get_primary_key_bytes() -> bytes:
    key_hex = getattr(settings, "CONNECTO_FIELD_ENCRYPTION_KEY", None) or os.environ.get("CONNECTO_FIELD_ENCRYPTION_KEY", "")
    if key_hex and len(key_hex) >= 64:
        try:
            return bytes.fromhex(key_hex[:64])
        except Exception:
            pass
    sec = getattr(settings, "SECRET_KEY", "default-fallback-secret-key-32b-long!!")
    return sec.encode("utf-8").ljust(32, b"0")[:32]

_primary_key_bytes = _get_primary_key_bytes()
_primary_cipher = AESGCM(_primary_key_bytes)

def _get_decryption_ciphers() -> List[AESGCM]:
    ciphers = [_primary_cipher]
    sec = getattr(settings, "SECRET_KEY", "")
    if sec:
        k1 = sec.encode("utf-8").ljust(32, b"0")[:32]
        if k1 != _primary_key_bytes:
            ciphers.append(AESGCM(k1))
        if len(sec) >= 64:
            try:
                k2 = bytes.fromhex(sec[:64])
                if k2 != _primary_key_bytes and k2 != k1:
                    ciphers.append(AESGCM(k2))
            except Exception:
                pass
        k3 = hashlib.sha256(sec.encode("utf-8")).digest()
        if k3 != _primary_key_bytes:
            ciphers.append(AESGCM(k3))
    return ciphers

_decryption_ciphers = _get_decryption_ciphers()

def encrypt_field(value: Optional[str]) -> Optional[str]:
    """Encrypts a sensitive PII field using authenticated AES-256-GCM with deterministic nonce."""
    if value is None:
        return None
    val_str = str(value).strip()
    if not val_str or val_str.startswith(ENC_PREFIX):
        return val_str
    try:
        # 96-bit nonce derived from HMAC of value ensures deterministic indexing for queries
        nonce = hmac.new(_primary_key_bytes, val_str.encode("utf-8"), hashlib.sha256).digest()[:12]
        ciphertext = _primary_cipher.encrypt(nonce, val_str.encode("utf-8"), None)
        payload = nonce + ciphertext
        return f"{ENC_PREFIX}{base64.urlsafe_b64encode(payload).decode('ascii')}"
    except Exception as e:
        logger.error(f"[FIELD_ENCRYPT_ERROR] Encryption failed: {e}")
        return val_str

def decrypt_field(enc_value: Optional[str]) -> Optional[str]:
    """Decrypts an AES-256-GCM ciphertext field with fallback key resilience and plaintext fallback."""
    if enc_value is None:
        return None
    val_str = str(enc_value).strip()
    if not val_str.startswith(ENC_PREFIX):
        return val_str
    try:
        raw_b64 = val_str[len(ENC_PREFIX):]
        pad_len = len(raw_b64) % 4
        if pad_len:
            raw_b64 += '=' * (4 - pad_len)
        payload = base64.urlsafe_b64decode(raw_b64.encode("ascii"))
        if len(payload) < 28:
            return None
        nonce = payload[:12]
        ciphertext = payload[12:]

        for cipher in _decryption_ciphers:
            try:
                plaintext = cipher.decrypt(nonce, ciphertext, None)
                return plaintext.decode("utf-8")
            except Exception:
                continue
        logger.debug(f"[FIELD_DECRYPT_FALLBACK] All ciphers failed for ciphertext: {val_str[:25]}...")
        return None
    except Exception as e:
        logger.debug(f"[FIELD_DECRYPT_FAIL] Failed to decode field payload: {e}")
        return None

class EncryptedString(TypeDecorator):
    """SQLAlchemy TypeDecorator that transparently encrypts strings on write and decrypts on read."""
    impl = String
    cache_ok = True

    def process_bind_param(self, value, dialect):
        if value is not None:
            return encrypt_field(value)
        return None

    def process_result_value(self, value, dialect):
        if value is not None:
            return decrypt_field(value)
        return None
