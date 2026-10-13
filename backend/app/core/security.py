import hashlib
import hmac
import secrets
from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError, VerificationError

ph = PasswordHasher(
    time_cost=3,
    memory_cost=65536,  # 64 MB
    parallelism=4,
    hash_len=32,
    salt_len=16
)

def hash_password_pbkdf2(password: str, iterations: int = 100000) -> str:
    """Hashes a raw password using PBKDF2-SHA256 (Cloudflare Worker & WebCrypto interoperable)."""
    salt = secrets.token_bytes(16)
    derived = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations, dklen=32)
    return f"pbkdf2_sha256${iterations}${salt.hex()}${derived.hex()}"

def hash_password(password: str) -> str:
    """Hashes a raw password using PBKDF2-SHA256 for edge and backend cross-compatibility."""
    return hash_password_pbkdf2(password)

def verify_password(password_hash: str, password: str) -> bool:
    """Verifies a raw password against PBKDF2-SHA256, Argon2id, or plain hashes."""
    if not password_hash or not password:
        return False
    try:
        if password_hash.startswith("pbkdf2_sha256$"):
            # Format: pbkdf2_sha256$<iterations>$<salt_hex>$<hash_hex>
            parts = password_hash.split("$")
            if len(parts) == 4:
                iterations = int(parts[1])
                salt = bytes.fromhex(parts[2])
                expected_hash = bytes.fromhex(parts[3])
                derived = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations, dklen=len(expected_hash))
                return hmac.compare_digest(derived, expected_hash)
        if password_hash.startswith("$argon2"):
            return ph.verify(password_hash, password)
        return hmac.compare_digest(password_hash, password)
    except Exception:
        return False

def generate_session_token() -> str:
    """Generates a cryptographically secure random session token."""
    return secrets.token_urlsafe(32)

def hash_session_token(token: str) -> str:
    """Hashes a session token for secure DB storage."""
    return hashlib.sha256(token.encode('utf-8')).hexdigest()

from typing import Optional, Dict, Any
from datetime import datetime, timezone, timedelta
import jwt
from app.core.config import settings

def create_access_jwt(data: Dict[str, Any], expires_delta: Optional[timedelta] = None) -> str:
    """Generates a signed JWT with HS256 algorithm and expiration."""
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + (expires_delta or timedelta(days=settings.SESSION_EXPIRE_DAYS))
    to_encode.update({"exp": expire, "iat": datetime.now(timezone.utc)})
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm="HS256")

def verify_access_jwt(token: str) -> Optional[Dict[str, Any]]:
    """Verifies and decodes a signed JWT. Returns payload dictionary or None if invalid/expired."""
    if not token or not isinstance(token, str):
        return None
    try:
        payload = jwt.decode(token.strip(), settings.SECRET_KEY, algorithms=["HS256"])
        return payload
    except Exception:
        return None

