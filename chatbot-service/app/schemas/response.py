from pydantic import BaseModel
from typing import Any


class ChatResponse(BaseModel):
    answer: str
    context: Any
    user: dict
    conversation_id: str
    title: str
