from fastapi import Header, HTTPException
from app.core.config import get_settings

settings = get_settings()

def require_internal_token(x_internal_token: str = Header(default=None)):
    expected = settings.internal_service_token
    if expected:
        if not x_internal_token or x_internal_token != expected:
            raise HTTPException(status_code=401, detail="Unauthorized")
    return True
