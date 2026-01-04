from app.core.gemini_client import GeminiClient
import logging
from typing import List, Dict

logger = logging.getLogger(__name__)

GENERATION_PROMPT = """Bạn là trợ lý tư vấn pháp luật Việt Nam chuyên nghiệp, thân thiện và chính xác.

Nhiệm vụ:
1. Dựa vào CÁC ĐIỀU LUẬT được cung cấp để trả lời câu hỏi
2. Trích dẫn rõ ràng: Điều X, Khoản Y, Luật Z
3. Giải thích dễ hiểu cho người dân
4. Nếu thông tin không đủ, hãy nói rõ và gợi ý tìm kiếm thêm
5. KHÔNG bịa đặt thông tin không có trong điều luật

Cấu trúc câu trả lời:
- Mở đầu: Tóm tắt ngắn gọn
- Nội dung: Trích dẫn và giải thích các điều luật liên quan
- Kết luận: Khuyến nghị hoặc lưu ý (nếu có)

Giọng điệu: Lịch sự, rõ ràng, dễ hiểu.
"""

class GenerationStage:
    """
    Giai đoạn 6: Tổng hợp và Phản hồi
    - Tổng hợp thông tin từ các điều luật
    - Tạo câu trả lời hoàn chỉnh, chính xác
    - Trích dẫn nguồn rõ ràng
    """
    
    def __init__(self):
        self.client = GeminiClient()
    
    def _format_context(self, legal_docs: List[Dict]) -> str:
        """
        Format các điều luật thành văn bản dễ đọc cho LLM
        
        Args:
            legal_docs: Danh sách điều luật từ Stage 4
            
        Returns:
            str: Văn bản đã format
        """
        if not legal_docs:
            return "Không tìm thấy điều luật liên quan."
        
        context_parts = []
        for idx, doc in enumerate(legal_docs, 1):
            tieu_de = doc.get("tieu_de", "Không rõ tiêu đề")
            noi_dung = doc.get("noi_dung", "")
            score = doc.get("score", 0)
            
            context_parts.append(f"""
--- ĐIỀU LUẬT {idx} (Độ liên quan: {score:.2f}) ---
Tiêu đề: {tieu_de}
Nội dung:
{noi_dung}
""")
        
        return "\n".join(context_parts)
    
    async def run(self, question: str, legal_docs: List[Dict]) -> str:
        """
        Tạo câu trả lời từ câu hỏi và điều luật
        
        Args:
            question: Câu hỏi gốc của người dùng
            legal_docs: Danh sách điều luật liên quan
            
        Returns:
            str: Câu trả lời hoàn chỉnh
        """
        logger.info(f"[STAGE 6] Bắt đầu tổng hợp câu trả lời từ {len(legal_docs)} điều luật")
        
        # Format context
        formatted_context = self._format_context(legal_docs)
        
        # Tạo prompt cho LLM
        user_prompt = f"""
CÂU HỎI TỪ NGƯỜI DÙNG:
{question}

CÁC ĐIỀU LUẬT LIÊN QUAN:
{formatted_context}

Hãy trả lời câu hỏi dựa trên các điều luật trên.
"""
        
        # Gọi LLM
        try:
            answer = await self.client.chat(
                system_prompt=GENERATION_PROMPT,
                user_prompt=user_prompt
            )
            
            answer = answer.strip()
            
            if not answer:
                logger.warning("[STAGE 6] LLM trả về rỗng")
                return "Xin lỗi, tôi không thể tạo câu trả lời lúc này. Vui lòng thử lại."
            
            logger.info(f"[STAGE 6] Tạo câu trả lời thành công ({len(answer)} ký tự)")
            return answer
            
        except Exception as e:
            logger.error(f"[STAGE 6] Lỗi khi tạo câu trả lời: {e}")
            return f"Đã xảy ra lỗi khi xử lý câu hỏi. Vui lòng thử lại sau. Chi tiết: {str(e)}"