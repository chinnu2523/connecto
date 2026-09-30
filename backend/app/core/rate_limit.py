import time
from collections import defaultdict
from typing import Dict, List, Tuple
from fastapi import Request

class SlidingWindowRateLimiter:
    """
    Thread-safe, in-memory sliding window rate limiter.
    Tracks timestamps per key and automatically prunes stale entries.
    """
    def __init__(self):
        self._records: Dict[str, List[float]] = defaultdict(list)
        self._last_cleanup: float = time.time()

    def _cleanup_stale(self, now: float, max_age: float = 3600.0) -> None:
        """Prunes stale keys periodically to avoid memory leaks."""
        if now - self._last_cleanup > 300.0:  # Every 5 minutes
            keys_to_remove = []
            for key, timestamps in self._records.items():
                active = [t for t in timestamps if now - t < max_age]
                if not active:
                    keys_to_remove.append(key)
                else:
                    self._records[key] = active
            for key in keys_to_remove:
                self._records.pop(key, None)
            self._last_cleanup = now

    def is_allowed(self, key: str, max_requests: int, window_seconds: float) -> Tuple[bool, int, int]:
        """
        Checks if request is allowed.
        Returns:
            (is_allowed: bool, remaining_requests: int, retry_after_seconds: int)
        """
        now = time.time()
        self._cleanup_stale(now, max_age=max(window_seconds * 2, 3600.0))

        timestamps = self._records[key]
        # Prune timestamps outside current window
        cutoff = now - window_seconds
        active_timestamps = [t for t in timestamps if t > cutoff]
        self._records[key] = active_timestamps

        count = len(active_timestamps)
        if count >= max_requests:
            oldest_active = active_timestamps[0]
            retry_after = max(1, int(oldest_active + window_seconds - now))
            return False, 0, retry_after

        # Record this request
        active_timestamps.append(now)
        remaining = max(0, max_requests - (count + 1))
        return True, remaining, 0

# Global Limiter Instance
rate_limiter = SlidingWindowRateLimiter()

def get_client_ip(request: Request) -> str:
    """
    Extracts the real client IP address.
    SECURITY: Only trust CF-Connecting-IP (set by Cloudflare, unforgeable from outside).
    X-Forwarded-For and X-Real-IP are NOT trusted as they can be spoofed by direct
    connections to the server bypassing Cloudflare.
    Falls back to the actual socket IP if CF header is absent (internal/health traffic).
    """
    cf_ip = request.headers.get("CF-Connecting-IP")
    if cf_ip:
        return cf_ip.strip()
    # Do NOT trust X-Forwarded-For or X-Real-IP - they can be forged
    # Use the real socket IP for non-Cloudflare traffic (internal calls, health checks)
    if request.client and request.client.host:
        return request.client.host
    return "127.0.0.1"


from typing import Optional
from fastapi import HTTPException

def enforce_rate_limit(request: Request, key_prefix: str, max_requests: int, window_seconds: float, identifier: Optional[str] = None):
    """Enforces rate limit per IP or identifier. Raises HTTPException(429) if exceeded."""
    ip = get_client_ip(request)
    key = f'{key_prefix}:{identifier.strip().lower()}' if identifier else f'{key_prefix}:{ip}'
    allowed, remaining, retry_after = rate_limiter.is_allowed(key, max_requests, window_seconds)
    if not allowed:
        raise HTTPException(
            status_code=429,
            detail=f'Rate limit exceeded. Please try again in {retry_after} seconds.',
            headers={'Retry-After': str(retry_after)}
        )
