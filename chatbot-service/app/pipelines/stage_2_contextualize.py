from app.core.gemini_client import GeminiClient
from app.prompts import contextualize_prompt
import logging

logger = logging.getLogger(__name__)

class ContextualizeStage:
    """
    Giai đoạn 2: Tái cấu trúc dựa trên ngữ cảnh
    - Kết hợp câu hỏi hiện tại với lịch sử hội thoại
    - Tạo ra câu hỏi rõ ràng, đầy đủ ngữ cảnh
    
    Ví dụ:
    - User trước: "Tội trộm cắp bị phạt như thế nào?"
    - User hiện tại: "Mức phạt là bao nhiêu?"
    - Output: "Mức phạt cho tội trộm cắp là bao nhiêu?"
    """
    def __init__(self):
        self.client = GeminiClient()

    async def run(self, question: str, memory: list[str]) -> str:
        """
        Tái cấu trúc câu hỏi dựa trên ngữ cảnh
        
        Args:
            question: Câu hỏi hiện tại
            memory: Lịch sử hội thoại (3-5 câu gần nhất)
            
        Returns:
            str: Câu hỏi đã được tái cấu trúc, đầy đủ ngữ cảnh
        """
        logger.info(f"[STAGE 2] Bắt đầu tái cấu trúc với {len(memory)} câu lịch sử")
        
        # Lấy 3-5 câu gần nhất để tránh quá tải context
        memory_text = "\n".join(memory[-5:]) if memory else "Không có lịch sử"

        user_prompt = f"""
Câu hỏi hiện tại:
{question}

Lịch sử hội thoại gần đây:
{memory_text}

Hãy tái cấu trúc câu hỏi hiện tại thành một câu hỏi độc lập, rõ ràng, chứa đủ ngữ cảnh từ lịch sử.
"""
        
        result = await self.client.chat(
            system_prompt=contextualize_prompt,
            user_prompt=user_prompt
        )
        
        contextualized_question = result.strip()
        logger.info(f"[STAGE 2] Câu hỏi sau tái cấu trúc: {contextualized_question[:100]}...")
        
        return contextualized_question