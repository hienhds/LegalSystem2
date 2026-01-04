from pydantic import BaseModel, Field
from typing import Optional

class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000, description="Câu hỏi từ người dùng")
    conversation_id: Optional[str] = Field(None, description="ID của conversation (nếu continue)")
    
    class Config:
        json_schema_extra = {
            "example": {
                "message": "Tội trộm cắp tài sản bị xử phạt như thế nào?",
                "conversation_id": "507f1f77bcf86cd799439011"
            }
        }