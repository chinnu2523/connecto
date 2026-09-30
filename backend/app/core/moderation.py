import re
import time
from collections import defaultdict
from typing import Dict, List
from fastapi import HTTPException, status

# Default Auto-Mod Blocklist (Slurs, scam phishing links, malware keywords)
DEFAULT_BLOCKLIST = [
    r"\b(nigger|faggot|retard|chink|spic)\b",
    r"discord(app)?\.gg\/[a-zA-Z0-9]+",  # Unsolicited discord spam invites
    r"free-nitro-[a-z0-9-]+\.(xyz|top|ru|site|online)",
    r"steamcommunity-login\.[a-z0-9-]+\.",
    r"bit\.ly\/[a-zA-Z0-9]+", # Obfuscated links
    r"\b(crypto-giveaway|free-bitcoin-drop|claim-eth-now)\b"
]

COMPILED_BLOCKLIST = [re.compile(pattern, re.IGNORECASE) for pattern in DEFAULT_BLOCKLIST]

# In-memory sliding window rate limiter: user_id -> list of timestamps
USER_MESSAGE_TIMESTAMPS: Dict[str, List[float]] = defaultdict(list)
MAX_MESSAGES_PER_WINDOW = 5
WINDOW_SECONDS = 3.0

def check_auto_mod(content: str) -> None:
    """
    Scans message content against the auto-mod blocklist.
    Raises HTTPException 400 Bad Request if content contains forbidden terms/phishing links.
    """
    for pattern in COMPILED_BLOCKLIST:
        if pattern.search(content):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Message blocked by Auto-Mod filter (contained inappropriate terms or unauthorized links)."
            )

def check_rate_limit(user_id: str) -> None:
    """
    Enforces sliding window rate limit on message sending per user.
    Raises HTTPException 429 Too Many Requests if exceeded.
    """
    now = time.time()
    timestamps = USER_MESSAGE_TIMESTAMPS[user_id]

    # Prune timestamps older than window
    USER_MESSAGE_TIMESTAMPS[user_id] = [ts for ts in timestamps if now - ts < WINDOW_SECONDS]

    if len(USER_MESSAGE_TIMESTAMPS[user_id]) >= MAX_MESSAGES_PER_WINDOW:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Sending messages too fast. Rate limit exceeded (max 5 messages per 3s)."
        )

    USER_MESSAGE_TIMESTAMPS[user_id].append(now)
