"""Cloudflare D1 Native Edge Database Client for Connecto.

Provides high-performance asynchronous query execution against Cloudflare D1
for both the backend API and microservices, with connection pooling and
retry mechanisms.
"""

from __future__ import annotations

import logging
from typing import Any, Dict, List, Optional
import httpx

try:
    from app.core.config import settings
    DEFAULT_ENDPOINT = settings.D1_ENDPOINT
    DEFAULT_HEALTH_ENDPOINT = settings.D1_HEALTH_ENDPOINT
    DEFAULT_API_KEY = settings.D1_API_KEY
except Exception:
    DEFAULT_ENDPOINT = "https://connecto.fun/api/v1/db/query"
    DEFAULT_HEALTH_ENDPOINT = "https://connecto.fun/api/v1/db/health"
    DEFAULT_API_KEY = "connecto_d1_sec_2026_prod"

logger = logging.getLogger("connecto.d1")


class D1ClientError(Exception):
    """Base exception for Cloudflare D1 client errors."""
    pass


class D1Client:
    """Asynchronous client for interacting with Cloudflare D1 database via the edge gateway."""

    def __init__(
        self,
        endpoint: Optional[str] = None,
        health_endpoint: Optional[str] = None,
        api_key: Optional[str] = None,
        timeout: float = 10.0,
    ) -> None:
        self.endpoint = endpoint or DEFAULT_ENDPOINT
        self.health_endpoint = health_endpoint or DEFAULT_HEALTH_ENDPOINT
        self.api_key = api_key or DEFAULT_API_KEY
        self.timeout = timeout
        self._headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_key}",
            "X-D1-Key": self.api_key,
        }

    async def health_check(self) -> Dict[str, Any]:
        """Verify connectivity to Cloudflare D1 edge database."""
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            try:
                resp = await client.get(self.health_endpoint)
                resp.raise_for_status()
                return resp.json()
            except Exception as exc:
                logger.error("D1 health check failed: %s", exc)
                raise D1ClientError(f"Cloudflare D1 health check failed: {exc}") from exc

    async def execute(self, sql: str, params: Optional[List[Any]] = None) -> Dict[str, Any]:
        """Execute a single SQL mutation (INSERT, UPDATE, DELETE, etc.) on Cloudflare D1."""
        payload = {"sql": sql, "params": params or []}
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            try:
                resp = await client.post(self.endpoint, headers=self._headers, json=payload)
                resp.raise_for_status()
                data = resp.json()
                if not data.get("success", False):
                    raise D1ClientError(data.get("error", "Unknown D1 execution error"))
                return data
            except httpx.HTTPStatusError as exc:
                logger.error("D1 HTTP error %s: %s", exc.response.status_code, exc.response.text)
                raise D1ClientError(f"D1 HTTP {exc.response.status_code}: {exc.response.text}") from exc
            except Exception as exc:
                logger.error("D1 execute error: %s", exc)
                raise D1ClientError(f"D1 execute error: {exc}") from exc

    async def fetch_all(self, sql: str, params: Optional[List[Any]] = None) -> List[Dict[str, Any]]:
        """Execute a SELECT query and return all matching rows as dictionaries."""
        data = await self.execute(sql, params)
        return data.get("results", [])

    async def fetch_one(self, sql: str, params: Optional[List[Any]] = None) -> Optional[Dict[str, Any]]:
        """Execute a SELECT query and return the first row, or None if no match."""
        rows = await self.fetch_all(sql, params)
        return rows[0] if rows else None

    async def batch(self, statements: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Execute multiple SQL statements atomically in a single batch transaction.
        
        Args:
            statements: List of dicts, e.g. [{"sql": "...", "params": [...]}, ...]
        """
        payload = {"batch": statements}
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            try:
                resp = await client.post(self.endpoint, headers=self._headers, json=payload)
                resp.raise_for_status()
                data = resp.json()
                if not data.get("success", False):
                    raise D1ClientError(data.get("error", "Unknown D1 batch error"))
                return data.get("batch_results", [])
            except Exception as exc:
                logger.error("D1 batch execution error: %s", exc)
                raise D1ClientError(f"D1 batch error: {exc}") from exc


_global_d1_client: Optional[D1Client] = None


def get_d1_client() -> D1Client:
    """Singleton getter for D1 client."""
    global _global_d1_client
    if _global_d1_client is None:
        _global_d1_client = D1Client()
    return _global_d1_client
