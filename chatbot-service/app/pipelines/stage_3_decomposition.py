from app.core.gemini_client import GeminiClient
import logging
import json

logger = logging.getLogger(__name__)

DECOMPOSITION_PROMPT = """Bạn là chuyên gia phân tích pháp luật. Nhiệm vụ của bạn là phân rá câu hỏi phức tạp thành nhiều truy vấn đơn giản.

Quy tắc:
1. Nếu câu hỏi đơn giản (1 khía cạnh pháp lý), trả về 1 query
2. Nếu câu hỏi phức tạp (nhiều khía cạnh), chia thành 2-4 queries
3. Mỗi query phải độc lập, rõ ràng, có thể tìm kiếm riêng biệt
4. Trả về ĐÚNG định dạng JSON: {"queries": ["query1", "query2", ...]}

Ví dụ:
Input: "Tôi bị người khác đánh và lấy mất điện thoại, tôi có quyền gì?"
Output: {"queries": ["Hành vi cố ý gây thương tích bị xử lý như thế nào?", "Hành vi cướp giật tài sản bị xử phạt ra sao?", "Quyền của nạn nhân trong vụ án hình sự là gì?"]}

Input: "Hợp đồng thuê nhà bao lâu thì có hiệu lực?"
Output: {"queries": ["Thời hạn hiệu lực của hợp đồng thuê nhà theo quy định pháp luật"]}
"""

class DecompositionStage:
    """
    Giai đoạn 3: Phân rã truy vấn
    - Chia câu hỏi phức tạp thành nhiều truy vấn đơn giản
    - Mỗi truy vấn tập trung vào 1 khía cạnh pháp lý cụ thể
    - Giúp tìm kiếm chính xác hơn trong các chương/điều luật khác nhau
    """
    def __init__(self):
        self.client = GeminiClient()

    async def run(self, question: str) -> list[str]:
        """
        Phân rá câu hỏi thành nhiều truy vấn
        
        Args:
            question: Câu hỏi đã được tái cấu trúc từ Stage 2
            
        Returns:
            list[str]: Danh sách các truy vấn con (1-4 queries)
        """
        logger.info(f"[STAGE 3] Bắt đầu phân rã câu hỏi")
        
        user_prompt = f"Câu hỏi cần phân rã:\n{question}"
        
        result = await self.client.chat(
            system_prompt=DECOMPOSITION_PROMPT,
            user_prompt=user_prompt
        )
        
        # Xử lý kết quả JSON
        try:
            # Loại bỏ markdown code blocks nếu có
            clean_result = result.strip()
            if "```json" in clean_result:
                clean_result = clean_result.split("```json")[1].split("```")[0].strip()
            elif "```" in clean_result:
                clean_result = clean_result.split("```")[1].split("```")[0].strip()
            
            parsed = json.loads(clean_result)
            queries = parsed.get("queries", [question])
            
            # Validate
            if not queries or not isinstance(queries, list):
                logger.warning("[STAGE 3] Kết quả không hợp lệ, sử dụng câu hỏi gốc")
                return [question]
            
            logger.info(f"[STAGE 3] Phân rã thành {len(queries)} truy vấn")
            for idx, q in enumerate(queries, 1):
                logger.info(f"  Query {idx}: {q[:80]}...")
            
            return queries
            
        except json.JSONDecodeError as e:
            logger.error(f"[STAGE 3] Lỗi parse JSON: {e}, sử dụng câu hỏi gốc")
            return [question]
        except Exception as e:
            logger.error(f"[STAGE 3] Lỗi không xác định: {e}")
            return [question]