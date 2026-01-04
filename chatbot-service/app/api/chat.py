from fastapi import APIRouter, Depends, HTTPException
from app.dependencies.auth import get_current_user
from app.schemas.request import ChatRequest
from app.schemas.response import ChatResponse
from app.services.chatbot_service import handle_chat
from app.services.memory_workspace import MemoryWorkspace
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/chatbot", tags=["Chatbot"])

# Singleton memory instance
memory_workspace = MemoryWorkspace()


@router.post("/ask", response_model=ChatResponse)
async def ask_bot(req: ChatRequest, user=Depends(get_current_user)):
    """
    Endpoint chính để chat với chatbot
    
    Args:
        req: ChatRequest chứa message
        user: User info từ JWT token
        
    Returns:
        ChatResponse: Câu trả lời từ chatbot
    """
    logger.info(f"[API] ASK BOT CALLED by user {user['user_id']}")
    logger.info(f"[API] Message: {req.message[:100]}...")
    
    try:
        result = await handle_chat(req, user)
        logger.info("[API] PIPELINE RESULT OK")
        return result
    except Exception as e:
        logger.error(f"[API] Error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.delete("/memory/clear")
async def clear_memory(user=Depends(get_current_user)):
    """
    Xóa lịch sử hội thoại của user hiện tại
    
    Args:
        user: User info từ JWT token
        
    Returns:
        dict: Thông báo thành công
    """
    user_id = user["user_id"]
    logger.info(f"[API] CLEAR MEMORY for user {user_id}")
    
    try:
        memory_workspace.clear(user_id)
        return {
            "status": "success",
            "message": "Đã xóa lịch sử hội thoại",
            "user_id": user_id
        }
    except Exception as e:
        logger.error(f"[API] Error clearing memory: {e}")
        raise HTTPException(status_code=500, detail="Không thể xóa lịch sử")


@router.get("/memory/stats")
async def get_memory_stats(user=Depends(get_current_user)):
    """
    Lấy thống kê về memory của user hiện tại
    
    Args:
        user: User info từ JWT token
        
    Returns:
        dict: Thống kê memory
    """
    user_id = user["user_id"]
    logger.info(f"[API] GET MEMORY STATS for user {user_id}")
    
    try:
        stats = memory_workspace.get_stats(user_id)
        return stats
    except Exception as e:
        logger.error(f"[API] Error getting stats: {e}")
        raise HTTPException(status_code=500, detail="Không thể lấy thống kê")


@router.get("/health")
async def health_check():
    """
    Health check endpoint
    """
    return {
        "status": "UP",
        "service": "chatbot-service"
    }