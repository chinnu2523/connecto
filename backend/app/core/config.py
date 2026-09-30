import os
from pydantic_settings import BaseSettings, SettingsConfigDict

BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DEFAULT_DB_PATH = os.path.join(BACKEND_DIR, "connecto_staging.db")

class Settings(BaseSettings):
    PROJECT_NAME: str = "Connecto Real-Time Community Platform"
    ENV: str = "production"
    SECRET_KEY: str = "super-secret-connecto-key-change-in-production-32bytes!"
    DATABASE_URL: str = f"sqlite+aiosqlite:///{DEFAULT_DB_PATH}"

    COOKIE_NAME: str = "connecto_session"
    COOKIE_SECURE: bool = False
    COOKIE_SAMESITE: str = "lax"
    SESSION_EXPIRE_DAYS: int = 30

    UPLOAD_DIR: str = os.path.join(BACKEND_DIR, "uploads")
    MAX_AVATAR_SIZE_MB: int = 5

    # Resend Email Delivery API (https://resend.com) - RECOMMENDED
    RESEND_API_KEY: str = ""
    RESEND_FROM_EMAIL: str = "Connecto Security <security@connecto.fun>"

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

    # SMTP / Email Configuration for OTP delivery
    SMTP_HOST: str = ""
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""
    EMAILS_FROM: str = "support@connecto.fun"

    # Security: In production, actual OTP values are NEVER logged or returned in responses.
    ENABLE_DEV_OTP_FALLBACK: bool = False

    model_config = SettingsConfigDict(env_file=os.path.join(BACKEND_DIR, ".env"), extra="ignore")

settings = Settings()
