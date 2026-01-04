from jose import jwt, JWTError
from fastapi import HTTPException, status
from app.core.config import JWT_SECRET, JWT_ALGORITHM


def decode_jwt(token: str) -> dict:
    try:
        return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token"
        )
