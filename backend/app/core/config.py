import os
from pydantic_settings import BaseSettings, SettingsConfigDict

BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DEFAULT_DB_PATH = os.path.join(BACKEND_DIR, "connecto_staging.db")

class Settings(BaseSettings):
    PROJECT_NAME: str = "Connecto Real-Time Community Platform"
    ENV: str = os.getenv("CONNECTO_ENV", os.getenv("ENV", "production"))
    SECRET_KEY: str = os.getenv("SECRET_KEY", "")

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        if not self.SECRET_KEY or self.SECRET_KEY == "super-secret-connecto-key-change-in-production-32bytes!":
            import secrets
            import logging
            key_file = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".secret_key")
            if os.path.exists(key_file):
                try:
                    with open(key_file, "r") as f:
                        k = f.read().strip()
                        if len(k) >= 32:
                            self.SECRET_KEY = k
                except Exception:
                    pass
            if not self.SECRET_KEY or len(self.SECRET_KEY) < 32:
                logging.getLogger("uvicorn.error").warning("[SECURITY NOTICE] SECRET_KEY missing in environment. Generating and persisting cryptographically strong key.")
                self.SECRET_KEY = secrets.token_hex(32)
                try:
                    with open(key_file, "w") as f:
                        f.write(self.SECRET_KEY)
                except Exception:
                    pass

    DATABASE_URL: str = f"sqlite+aiosqlite:///{DEFAULT_DB_PATH}"

    COOKIE_NAME: str = "connecto_session"
    COOKIE_SECURE: bool = (os.getenv("CONNECTO_ENV", os.getenv("ENV", "production")) == "production")
    COOKIE_SAMESITE: str = "lax"
    SESSION_EXPIRE_DAYS: int = 30

    UPLOAD_DIR: str = os.path.join(BACKEND_DIR, "uploads")
    MAX_AVATAR_SIZE_MB: int = 5

    # Resend Email Delivery API (https://resend.com) - RECOMMENDED
    RESEND_API_KEY: str = os.getenv("RESEND_API_KEY", "")
    RESEND_FROM_EMAIL: str = os.getenv("RESEND_FROM_EMAIL", "Connecto Security <security@connecto.fun>")

    # MSG91 SMS Gateway for India (https://msg91.com) - RECOMMENDED FOR INDIA
    MSG91_AUTH_KEY: str = ""
    MSG91_TEMPLATE_ID: str = ""
    MSG91_SENDER_ID: str = "CONNTO"

    # Fast2SMS Gateway for India (https://fast2sms.com)
    FAST2SMS_API_KEY: str = ""

    # Twilio SMS Gateway (https://twilio.com) - Secondary International Fallback
    TWILIO_ACCOUNT_SID: str = ""
    TWILIO_AUTH_TOKEN: str = ""
    TWILIO_FROM_NUMBER: str = ""

    # httpSMS Gateway Configuration (https://github.com/NdoleStudio/httpsms.git)
    HTTPSMS_API_KEY: str = ""
    HTTPSMS_FROM_NUMBER: str = ""
    HTTPSMS_BASE_URL: str = "https://api.httpsms.com"
    HTTPSMS_WEBHOOK_SECRET: str = os.getenv("HTTPSMS_WEBHOOK_SECRET", "")

    # SMTP / Email Configuration for OTP delivery
    SMTP_HOST: str = ""
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""
    EMAILS_FROM: str = "support@connecto.fun"

    # Security: Fallback to dev OTP if live carrier/SMTP is unconfigured (Disabled for security)
    ENABLE_DEV_OTP_FALLBACK: bool = False

    # Cloudflare D1 Native Edge Database Configuration
    D1_ENABLED: bool = True
    D1_DATABASE_NAME: str = "connecto-db"
    D1_DATABASE_ID: str = "c95366cb-6dde-4bfe-8a3e-f466969f1a16"
    D1_ENDPOINT: str = "https://connecto.fun/api/v1/db/query"
    D1_HEALTH_ENDPOINT: str = "https://connecto.fun/api/v1/db/health"
    D1_API_KEY: str  # SEC-04 FIX: Required env var — no hardcoded fallback. Set D1_API_KEY in .env

    model_config = SettingsConfigDict(env_file=os.path.join(BACKEND_DIR, ".env"), extra="ignore")

settings = Settings()
