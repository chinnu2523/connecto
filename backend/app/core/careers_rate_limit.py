import time
from collections import defaultdict
from typing import Dict, List
from fastapi import HTTPException, status

# Sliding window rate limiter for public application submission
SUBMISSION_TIMESTAMPS: Dict[str, List[float]] = defaultdict(list)
MAX_SUBMISSIONS_PER_WINDOW = 3
WINDOW_SECONDS = 600.0  # 10 minutes

def check_careers_submission_rate_limit(key: str) -> None:
    """
    Enforces rate limit on public job application submissions.
    Raises HTTPException 429 Too Many Requests if rate limit exceeded.
    """
    now = time.time()
    timestamps = SUBMISSION_TIMESTAMPS[key]

    SUBMISSION_TIMESTAMPS[key] = [ts for ts in timestamps if now - ts < WINDOW_SECONDS]

    if len(SUBMISSION_TIMESTAMPS[key]) >= MAX_SUBMISSIONS_PER_WINDOW:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Rate limit exceeded. Maximum 3 job applications permitted per 10 minutes."
        )

    SUBMISSION_TIMESTAMPS[key].append(now)
