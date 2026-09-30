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
