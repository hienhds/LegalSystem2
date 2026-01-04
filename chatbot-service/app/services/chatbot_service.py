from app.services.chatbot_pipeline import ChatbotPipeline
from app.schemas.request import ChatRequest
from app.schemas.response import ChatResponse
import logging

logger = logging.getLogger(__name__)

# Singleton pipeline instance
pipeline = ChatbotPipeline()

async def handle_chat(req: ChatRequest, user: dict) -> dict:
    """
    Handler chính cho request chat từ người dùng
    
    Args:
        req: ChatRequest chứa message từ người dùng
        user: Thông tin user từ JWT (chứa user_id)
        
    Returns:
        dict: Response với answer và context
    """
    user_id = user["user_id"]
    
    logger.info(f"[CHATBOT SERVICE] Nhận request từ user {user_id}")
    logger.info(f"[CHATBOT SERVICE] Message: {req.message[:100]}...")
    
    try:
        # Chạy toàn bộ pipeline
        result = await pipeline.process(
            user_id=user_id,
            question=req.message
        )
        
        # Format response
        response = {
            "answer": result["answer"],
            "context": result.get("context"),
            "status": result["status"],
            "metadata": result.get("metadata", {})
        }
        
        logger.info(f"[CHATBOT SERVICE] Response status: {result['status']}")
        return ChatResponse(
            answer=response["answer"],
            context=response["context"],
            user=user
        )
        # return response
        
    except Exception as e:
        logger.error(f"[CHATBOT SERVICE] Lỗi xử lý: {e}", exc_info=True)
        return {
            "answer": "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
            "context": None,
            "status": "ERROR",
            "metadata": {}
        }