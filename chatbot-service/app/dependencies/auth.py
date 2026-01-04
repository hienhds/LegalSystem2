from fastapi import Header, HTTPException
from app.core.jwt import decode_jwt


def get_current_user(authorization: str = Header(None)):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(401, "Invalid Authorization header")


    token = authorization[7:]
    claims = decode_jwt(token)

    return {
        "user_id": claims.get("uid"),
        "email": claims.get("sub"),
        "full_name": claims.get("fullName"),
        "avatar": claims.get("avatar")
    }
