import hashlib
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

def hash_password(password: str) -> str:
    """Hashes a raw password using Argon2id."""
    return ph.hash(password)

def verify_password(password_hash: str, password: str) -> bool:
    """Verifies a raw password against an Argon2id hash."""
    try:
        return ph.verify(password_hash, password)
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

