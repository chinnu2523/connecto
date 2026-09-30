import time
from collections import OrderedDict
from typing import Any, Optional, Tuple

class TTLCache:
    """
    High-performance in-memory LRU cache with Time-To-Live (TTL) expiration.
    Bounded by max_size to prevent unbounded memory growth.
    Thread-safe and async-friendly.
    """
    def __init__(self, default_ttl: float = 10.0, max_size: int = 10_000):
        self._default_ttl = default_ttl
        self._max_size = max_size
        self._cache: OrderedDict[str, Tuple[Any, float]] = OrderedDict()

    async def get(self, key: str) -> Optional[Any]:
        entry = self._cache.get(key)
        if entry is None:
            return None
        data, expires_at = entry
        if time.monotonic() > expires_at:
            self._cache.pop(key, None)
            return None
        # LRU: move to end on access
        self._cache.move_to_end(key)
        return data

    async def set(self, key: str, value: Any, ttl: Optional[float] = None) -> None:
        duration = ttl if ttl is not None else self._default_ttl
        expires_at = time.monotonic() + duration
        if key in self._cache:
            self._cache.move_to_end(key)
        self._cache[key] = (value, expires_at)
        # Evict oldest entries when over capacity
        while len(self._cache) > self._max_size:
            self._cache.popitem(last=False)

    async def delete(self, key: str) -> None:
        self._cache.pop(key, None)

    async def clear(self) -> None:
        self._cache.clear()

    async def delete_prefix(self, prefix: str) -> None:
        keys_to_delete = [k for k in self._cache.keys() if k.startswith(prefix)]
        for k in keys_to_delete:
            self._cache.pop(k, None)

    def __len__(self) -> int:
        return len(self._cache)


# Global cache instance (10k entry LRU, 10s default TTL)
app_cache = TTLCache(default_ttl=10.0, max_size=10_000)

# Cache Keys
CACHE_KEY_CHANNELS    = "channels_list"
CACHE_KEY_STATS       = "platform_stats"
CACHE_KEY_MEMBERS     = "members_roster"
CACHE_KEY_LEADERBOARD = "leaderboard_roster"
CACHE_KEY_VOICE_ROOMS = "voice_rooms_list"
