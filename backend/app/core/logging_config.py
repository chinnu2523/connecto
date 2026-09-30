import json
import logging
import time
import uuid
import os
from datetime import datetime, timezone
from typing import Optional
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response

class JSONLogFormatter(logging.Formatter):
    """
    Standardized JSON Formatter for production structured logging.
    Emits timestamp, level, logger name, message, request ID, user ID,
    endpoint, status code, latency, and client IP.
    """
    def format(self, record: logging.LogRecord) -> str:
        log_data = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
        }
        for attr in ("request_id", "user_id", "endpoint", "method", "status_code", "duration_ms", "client_ip"):
            if hasattr(record, attr):
                log_data[attr] = getattr(record, attr)
        if record.exc_info:
            log_data["exception"] = self.formatException(record.exc_info)
        return json.dumps(log_data)

# Setup log directory and file handler
LOGS_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "logs")
os.makedirs(LOGS_DIR, exist_ok=True)
ACCESS_LOG_FILE = os.path.join(LOGS_DIR, "access.jsonl")

# Setup console and file handlers
json_formatter = JSONLogFormatter()

console_handler = logging.StreamHandler()
console_handler.setFormatter(json_formatter)

file_handler = logging.FileHandler(ACCESS_LOG_FILE, encoding="utf-8")
file_handler.setFormatter(json_formatter)

# App loggers
logger = logging.getLogger("connecto")
logger.setLevel(logging.INFO)
logger.handlers = [console_handler, file_handler]
logger.propagate = False

access_logger = logging.getLogger("connecto.access")
access_logger.setLevel(logging.INFO)
access_logger.handlers = [console_handler, file_handler]
access_logger.propagate = False

class StructuredLoggingMiddleware(BaseHTTPMiddleware):
    """
    FastAPI HTTP Middleware that logs every request with a unique request_id,
    user_id (if present), endpoint, HTTP method, response status code,
    client IP, and execution latency. Injects X-Request-ID into response headers.
    """
    async def dispatch(self, request: Request, call_next):
        req_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
        request.state.request_id = req_id
        start_time = time.time()

        # Extract client IP
        client_ip = (
            request.headers.get("CF-Connecting-IP")
            or request.headers.get("X-Forwarded-For")
            or (request.client.host if request.client else "127.0.0.1")
        )
        if "," in client_ip:
            client_ip = client_ip.split(",")[0].strip()

        # Extract user hint if available
        user_id = "anonymous"
        auth_header = request.headers.get("Authorization")
        if auth_header and auth_header.startswith("Bearer "):
            user_id = f"token:{auth_header[7:].strip()[:12]}..."
        elif "connecto_session" in request.cookies:
            user_id = f"cookie:{request.cookies['connecto_session'][:12]}..."

        response = await call_next(request)

        duration_ms = round((time.time() - start_time) * 1000, 2)
        response.headers["X-Request-ID"] = req_id

        # Skip spammy static polling unless error
        path = request.url.path
        is_static = path.startswith("/static") or path in ("/favicon.ico", "/sw.js", "/manifest.json")
        if not (is_static and response.status_code < 400):
            record = logging.LogRecord(
                name="connecto.access",
                level=logging.INFO if response.status_code < 400 else (logging.WARNING if response.status_code < 500 else logging.ERROR),
                pathname=__file__,
                lineno=0,
                msg=f"{request.method} {path} -> {response.status_code} ({duration_ms}ms)",
                args=(),
                exc_info=None
            )
            record.request_id = req_id
            record.user_id = user_id
            record.endpoint = path
            record.method = request.method
            record.status_code = response.status_code
            record.duration_ms = duration_ms
            record.client_ip = client_ip
            access_logger.handle(record)

        return response
