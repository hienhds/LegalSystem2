from app.services.chatbot_pipeline import ChatbotPipeline
from app.services.conversation_service import conversation_service
from app.core.gemini_client import GeminiClient
from app.schemas.request import ChatRequest
from app.schemas.response import ChatResponse
import logging

logger = logging.getLogger(__name__)

# Singleton instances
pipeline = ChatbotPipeline()
gemini_client = GeminiClient()

async def handle_chat(req: ChatRequest, user: dict) -> ChatResponse:
    """
    Handler chính cho request chat từ người dùng
    
    Logic:
    - Nếu KHÔNG có conversation_id → Tạo conversation mới với title do Gemini đặt
    - Nếu CÓ conversation_id → Continue conversation cũ
    
    Args:
        req: ChatRequest chứa message và conversation_id (optional)
        user: Thông tin user từ JWT
        
    Returns:
        ChatResponse: Response với answer, conversation info
    """
    user_id = user["user_id"]
    
    logger.info(f"[CHATBOT SERVICE] Nhận request từ user {user_id}")
    logger.info(f"[CHATBOT SERVICE] Message: {req.message[:100]}...")
    
    try:
        conversation_id = req.conversation_id
        conversation_title = None
        
        # ===== TẠO CONVERSATION MỚI (nếu không có conversation_id) =====
        if not conversation_id:
            logger.info(f"[CONVERSATION] Không có ID, tạo conversation mới...")
            
            # Gọi Gemini để đặt tên conversation
            logger.info(f"[GEMINI] Đang tạo tiêu đề conversation...")
            conversation_title = await gemini_client.generate_conversation_title(req.message)
            logger.info(f"[CONVERSATION] Tiêu đề: {conversation_title}")
            
            # Tạo conversation mới
            conversation_id = await conversation_service.create_conversation(
                user_id=user_id,
                title=conversation_title,
                first_message=None  # Sẽ add message sau
            )
            logger.info(f"[CONVERSATION] Đã tạo mới: {conversation_id}")
        else:
            logger.info(f"[CONVERSATION] Continue conversation: {conversation_id}")
            # Lấy title của conversation cũ
            conv = await conversation_service.get_conversation(conversation_id, user_id)
            if conv:
                conversation_title = conv.title
            else:
                logger.error(f"[CONVERSATION] Không tìm thấy conversation {conversation_id}")
                raise ValueError(f"Conversation {conversation_id} not found")
        
        # ===== LƯU USER MESSAGE =====
        await conversation_service.add_message(
            conversation_id=conversation_id,
            user_id=user_id,
            role="user",
            content=req.message,
            metadata=None
        )
        logger.info(f"[CONVERSATION] Đã lưu user message")
        
        # ===== CHẠY PIPELINE =====
        result = await pipeline.process(
            user_id=user_id,
            question=req.message
        )
        
        # ===== LƯU ASSISTANT MESSAGE =====
        await conversation_service.add_message(
            conversation_id=conversation_id,
            user_id=user_id,
            role="assistant",
            content=result["answer"],
            metadata={
                "status": result["status"],
                "classification": result.get("metadata", {}).get("classification"),
                "num_legal_docs": result.get("metadata", {}).get("num_legal_docs"),
                "queries": result.get("metadata", {}).get("queries")
            }
        )
        logger.info(f"[CONVERSATION] Đã lưu assistant message")
        
        # ===== FORMAT RESPONSE =====
        response = ChatResponse(
            status=result["status"],
            answer=result["answer"],
            context=result.get("context"),
            metadata={
                **result.get("metadata", {}),
                # ✅ Trả về đầy đủ thông tin conversation
                "user_id": user_id,
                "conversation_id": conversation_id,
                "conversation_title": conversation_title
            },
            user=user,
            conversation_id=conversation_id,
            title=conversation_title
        )
        
        logger.info(f"[CHATBOT SERVICE] ✅ Response status: {result['status']}")
        logger.info(f"[CONVERSATION] ✅ Saved to: {conversation_id}")
        
        return response
        
    except Exception as e:
        logger.error(f"[CHATBOT SERVICE] ❌ Lỗi xử lý: {e}", exc_info=True)
        return ChatResponse(
            status="ERROR",
            answer="Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
            context=None,
            metadata={
                "error": str(e),
                "user_id": user_id
            }
        )