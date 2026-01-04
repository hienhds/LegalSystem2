from app.core.gemini_client import GeminiClient
from app.prompts import classification_prompt
import logging

logger = logging.getLogger(__name__)

class ClassificationStage:
    """
    Giai đoạn 1: Phân loại và Sàng lọc
    - Phân loại câu hỏi: LEGAL, NON_LEGAL, TOXIC
    - Tiết kiệm tài nguyên bằng cách từ chối câu hỏi không hợp lệ sớm
    """
    def __init__(self):
        self.client = GeminiClient()

    async def run(self, question: str) -> str:
        """
        Phân loại câu hỏi người dùng
        
        Args:
            question: Câu hỏi từ người dùng
            
        Returns:
            str: "LEGAL", "NON_LEGAL", hoặc "TOXIC"
        """
        logger.info(f"[STAGE 1] Bắt đầu phân loại câu hỏi")
        
        result = await self.client.chat(
            system_prompt=classification_prompt,
            user_prompt=question
        )

        result = result.strip().upper()
        
        # Xử lý kết quả với nhiều trường hợp
        if "TOXIC" in result:
            logger.warning(f"[STAGE 1] Phát hiện nội dung TOXIC")
            return "TOXIC"
        if "NON_LEGAL" in result or "NON-LEGAL" in result:
            logger.info(f"[STAGE 1] Câu hỏi không liên quan pháp luật")
            return "NON_LEGAL"
        if "LEGAL" in result:
            logger.info(f"[STAGE 1] Câu hỏi hợp lệ về pháp luật")
            return "LEGAL"

        # Fallback an toàn
        logger.warning(f"[STAGE 1] Không xác định được loại, mặc định NON_LEGAL")
        return "NON_LEGAL"