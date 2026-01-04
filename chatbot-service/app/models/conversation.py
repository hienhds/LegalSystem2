from pydantic import BaseModel, Field
from pydantic.json_schema import JsonSchemaValue
from typing import List, Optional
from datetime import datetime
from bson import ObjectId
from pydantic import GetJsonSchemaHandler
from typing import Any

class PyObjectId(ObjectId):

    @classmethod
    def __get_pydantic_json_schema__(
        cls, core_schema: Any, handler: GetJsonSchemaHandler
    ) -> JsonSchemaValue:
        return {
            "type": "string",
            "examples": ["507f1f77bcf86cd799439011"],
        }

    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if isinstance(v, ObjectId):
            return v
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid ObjectId")
        return ObjectId(v)


class Message(BaseModel):
    """Model cho mỗi tin nhắn trong conversation"""
    role: str = Field(..., description="user hoặc assistant")
    content: str = Field(..., description="Nội dung tin nhắn")
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    metadata: Optional[dict] = Field(default=None, description="Metadata như context, queries, etc")

    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class Conversation(BaseModel):
    """Model cho cuộc hội thoại"""
    id: Optional[PyObjectId] = Field(default=None, alias="_id")
    user_id: int = Field(..., description="ID của user")
    title: str = Field(..., description="Tiêu đề cuộc hội thoại")
    messages: List[Message] = Field(default=[], description="Danh sách tin nhắn")
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)
    is_active: bool = Field(default=True, description="Conversation có đang active không")
    
    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True
        json_encoders = {
            ObjectId: str,
            datetime: lambda v: v.isoformat()
        }


class ConversationCreate(BaseModel):
    """Schema để tạo conversation mới"""
    title: str = Field(..., min_length=1, max_length=200)
    first_message: Optional[str] = Field(None, description="Tin nhắn đầu tiên (optional)")


class ConversationUpdate(BaseModel):
    """Schema để update conversation"""
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    is_active: Optional[bool] = None


class ConversationListItem(BaseModel):
    """Schema cho danh sách conversations (không có messages)"""
    id: str = Field(..., alias="_id")
    user_id: int
    title: str
    message_count: int
    last_message: Optional[str] = None
    created_at: datetime
    updated_at: datetime
    is_active: bool
    
    class Config:
        allow_population_by_field_name = True


class ConversationResponse(BaseModel):
    """Response khi lấy chi tiết conversation"""
    id: str = Field(..., alias="_id")
    user_id: int
    title: str
    messages: List[Message]
    created_at: datetime
    updated_at: datetime
    is_active: bool
    message_count: int
    
    class Config:
        allow_population_by_field_name = True