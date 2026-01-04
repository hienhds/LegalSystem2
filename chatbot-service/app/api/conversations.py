from fastapi import APIRouter, Depends, HTTPException, Query
from typing import List
from app.dependencies.auth import get_current_user
from app.services.conversation_service import conversation_service
from app.models.conversation import (
    ConversationCreate,
    ConversationUpdate,
    ConversationListItem,
    ConversationResponse
)
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/chatbot/conversations", tags=["Conversations"])


@router.post("/", response_model=dict)
async def create_conversation(
    data: ConversationCreate,
    user=Depends(get_current_user)
):
    """
    Tạo conversation mới
    
    Body:
        - title: Tiêu đề conversation
        - first_message: Tin nhắn đầu tiên (optional)
    """
    user_id = user["user_id"]
    
    try:
        conversation_id = await conversation_service.create_conversation(
            user_id=user_id,
            title=data.title,
            first_message=data.first_message
        )
        
        return {
            "conversation_id": conversation_id,
            "message": "Conversation created successfully"
        }
        
    except Exception as e:
        logger.error(f"Error creating conversation: {e}")
        raise HTTPException(status_code=500, detail="Failed to create conversation")


@router.get("/", response_model=List[ConversationListItem])
async def list_conversations(
    skip: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    active_only: bool = Query(True),
    user=Depends(get_current_user)
):
    """
    Lấy danh sách conversations của user
    
    Query params:
        - skip: Số lượng skip (pagination)
        - limit: Số lượng tối đa (1-100)
        - active_only: Chỉ lấy active conversations
    """
    user_id = user["user_id"]
    
    try:
        conversations = await conversation_service.list_conversations(
            user_id=user_id,
            skip=skip,
            limit=limit,
            active_only=active_only
        )
        
        return conversations
        
    except Exception as e:
        logger.error(f"Error listing conversations: {e}")
        raise HTTPException(status_code=500, detail="Failed to list conversations")


@router.get("/{conversation_id}", response_model=ConversationResponse)
async def get_conversation(
    conversation_id: str,
    user=Depends(get_current_user)
):
    """
    Lấy chi tiết conversation (bao gồm tất cả messages)
    """
    user_id = user["user_id"]
    
    try:
        conversation = await conversation_service.get_conversation(
            conversation_id=conversation_id,
            user_id=user_id
        )
        
        if not conversation:
            raise HTTPException(status_code=404, detail="Conversation not found")
        
        return conversation
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting conversation: {e}")
        raise HTTPException(status_code=500, detail="Failed to get conversation")


@router.patch("/{conversation_id}", response_model=dict)
async def update_conversation(
    conversation_id: str,
    data: ConversationUpdate,
    user=Depends(get_current_user)
):
    """
    Update conversation (title, is_active)
    
    Body:
        - title: Tiêu đề mới (optional)
        - is_active: Active status (optional)
    """
    user_id = user["user_id"]
    
    try:
        success = await conversation_service.update_conversation(
            conversation_id=conversation_id,
            user_id=user_id,
            title=data.title,
            is_active=data.is_active
        )
        
        if not success:
            raise HTTPException(status_code=404, detail="Conversation not found or not updated")
        
        return {"message": "Conversation updated successfully"}
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error updating conversation: {e}")
        raise HTTPException(status_code=500, detail="Failed to update conversation")


@router.delete("/{conversation_id}", response_model=dict)
async def delete_conversation(
    conversation_id: str,
    hard_delete: bool = Query(False, description="True = xóa hẳn, False = soft delete"),
    user=Depends(get_current_user)
):
    """
    Xóa conversation
    
    Query params:
        - hard_delete: True = xóa hẳn khỏi DB, False = set is_active=False
    """
    user_id = user["user_id"]
    
    try:
        success = await conversation_service.delete_conversation(
            conversation_id=conversation_id,
            user_id=user_id,
            soft_delete=not hard_delete
        )
        
        if not success:
            raise HTTPException(status_code=404, detail="Conversation not found")
        
        return {
            "message": "Conversation deleted successfully",
            "hard_delete": hard_delete
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error deleting conversation: {e}")
        raise HTTPException(status_code=500, detail="Failed to delete conversation")


@router.get("/active/latest", response_model=dict)
async def get_active_conversation(user=Depends(get_current_user)):
    """
    Lấy conversation active gần nhất của user
    Dùng để continue conversation cũ
    """
    user_id = user["user_id"]
    
    try:
        conversation_id = await conversation_service.get_active_conversation(user_id)
        
        if not conversation_id:
            return {
                "conversation_id": None,
                "message": "No active conversation found"
            }
        
        return {
            "conversation_id": conversation_id,
            "message": "Active conversation found"
        }
        
    except Exception as e:
        logger.error(f"Error getting active conversation: {e}")
        raise HTTPException(status_code=500, detail="Failed to get active conversation")