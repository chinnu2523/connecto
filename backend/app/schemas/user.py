import re
from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field, ConfigDict, field_validator, model_validator

class UserSignup(BaseModel):
    """Signup form schema - contains ONLY account creation fields."""
    email: str = Field(..., min_length=3, max_length=128)
    username: str = Field(..., min_length=3, max_length=32)
    display_name: Optional[str] = Field(default="", max_length=64)
    password: str = Field(..., min_length=4, max_length=128)

    @field_validator("username")
    def validate_username(cls, v: str) -> str:
        v = v.strip()
        if not re.match(r"^[a-zA-Z0-9_]+$", v):
            raise ValueError("Username may only contain letters, numbers, and underscores without spaces.")
        return v.lower()

    @field_validator("email")
    def validate_email(cls, v: str) -> str:
        v = v.strip().lower()
        email_regex = r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$"
        if not re.match(email_regex, v) or len(v) < 5:
            raise ValueError("Please provide a valid email address (e.g. name@example.com).")
        return v

    @field_validator("display_name")
    def validate_display_name(cls, v: Optional[str]) -> str:
        return (v or "").strip()

class UserLogin(BaseModel):
    """Login form schema - contains ONLY login identifier and password."""
    login: str = Field(default="", description="Username or Email address")
    password: str = Field(..., min_length=1, description="Password")

    @model_validator(mode="before")
    def populate_login_from_username(cls, values):
        if isinstance(values, dict):
            if not values.get("login") and values.get("username"):
                values["login"] = values["username"]
        return values

class UserResponse(BaseModel):
    id: str
    username: str
    display_name: str
    email: str
    avatar_url: Optional[str] = None
    banner_url: Optional[str] = None
    bio: Optional[str] = ""
    phone_number: Optional[str] = None
    full_name: Optional[str] = ""
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    location: Optional[str] = None
    two_factor_enabled: Optional[bool] = False
    two_factor_method: Optional[str] = "sms"
    requires_2fa: Optional[bool] = False
    masked_destination: Optional[str] = None
    is_stealth: Optional[bool] = False
    is_online: Optional[bool] = True
    is_admin: Optional[bool] = False
    username_changed: bool
    created_at: datetime
    token: Optional[str] = None
    dev_otp: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)

class ProfileUpdate(BaseModel):
    display_name: Optional[str] = Field(None, min_length=1, max_length=64)
    avatar_url: Optional[str] = Field(None, max_length=512)
    avatar: Optional[str] = Field(None, max_length=512)
    picture: Optional[str] = Field(None, max_length=512)
    username: Optional[str] = Field(None, min_length=3, max_length=32)
    bio: Optional[str] = Field(None, max_length=255)
    full_name: Optional[str] = Field(None, max_length=128)
    date_of_birth: Optional[str] = Field(None, max_length=32)
    gender: Optional[str] = Field(None, max_length=32)
    location: Optional[str] = Field(None, max_length=128)
    phone_number: Optional[str] = Field(None, max_length=32)
    banner_url: Optional[str] = Field(None, max_length=512)
    is_stealth: Optional[bool] = None
    is_online: Optional[bool] = None

    @field_validator("username")
    def validate_username(cls, v: Optional[str]) -> Optional[str]:
        if v is not None:
            v = v.strip()
            if not re.match(r"^[a-zA-Z0-9_]+$", v):
                raise ValueError("Username may only contain letters, numbers, and underscores.")
            return v.lower()
        return v

class ForgotPasswordRequest(BaseModel):
    identifier: str = Field(..., min_length=1, description="Email address or phone number or username")
    method: str = Field(default="sms", description="'sms' or 'email'")

class ForgotPasswordVerify(BaseModel):
    identifier: str = Field(..., min_length=1)
    otp_code: str = Field(..., min_length=4, max_length=16)

class ForgotPasswordReset(BaseModel):
    identifier: str = Field(..., min_length=1)
    otp_code: str = Field(..., min_length=4, max_length=16)
    new_password: str = Field(..., min_length=4, max_length=128)

class TwoFactorToggleRequest(BaseModel):
    enabled: bool
    method: Optional[str] = Field(default="sms", description="'sms' or 'email'")
    phone_number: Optional[str] = None

class TwoFactorRequestOtp(BaseModel):
    method: Optional[str] = Field(default="sms")
    phone_number: Optional[str] = None

class TwoFactorVerifyOtp(BaseModel):
    otp_code: str = Field(..., min_length=4, max_length=16)
    method: Optional[str] = Field(default="sms")
    phone_number: Optional[str] = None

class TwoFactorLoginVerify(BaseModel):
    login: str = Field(..., min_length=1)
    otp_code: str = Field(..., min_length=4, max_length=16)

class ContactChangeRequest(BaseModel):
    new_contact: str = Field(..., min_length=3, max_length=255)
    method: str = Field(default="email", description="'email' or 'sms'")

class ContactChangeVerify(BaseModel):
    new_contact: str = Field(..., min_length=3, max_length=255)
    method: str = Field(default="email", description="'email' or 'sms'")
    otp_code: str = Field(..., min_length=4, max_length=16)


