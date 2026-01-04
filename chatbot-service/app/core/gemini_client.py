import os
import logging
from google import genai
from google.genai import types
import asyncio
import time

logger = logging.getLogger(__name__)

class GeminiClient:
    """
    Client đã sửa lỗi để tương thích với Gemini 2.5 Flash.
    Sử dụng thinking_budget thay vì thinking_level.
    """
    _instance = None
    _client = None
    _last_request_ts: float = 0.0
    _min_interval_sec: float = 1.2

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(GeminiClient, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        if GeminiClient._client is None:
            # Lưu ý: Nên dùng os.environ.get("GEMINI_API_KEY") để bảo mật
            api_key = "AIzaSyCDqSizRQE_Yz8gxhHJ1fN-4ZagDqs2tuU"
            
            if not api_key:
                logger.error("❌ GEMINI_API_KEY không được cấu hình")
                raise ValueError("GEMINI_API_KEY is required")
            
            try:
                GeminiClient._client = genai.Client(api_key=api_key)
                logger.info("✅ Gemini Client đã sẵn sàng!")
            except Exception as e:
                logger.error(f"❌ Lỗi khởi tạo Gemini Client: {e}")
                raise

    async def chat(self, system_prompt: str, user_prompt: str) -> str:
        if not self._client:
            raise RuntimeError("Gemini Client chưa được khởi tạo")

        try:
            # Chọn model phù hợp
            # model = "gemini-2.5-flash" 
            model = "gemini-2.5-flash-lite" # Model này có Quota RPM tốt hơn cho bản Free
            
            contents = [
                types.Content(
                    role="user",
                    parts=[types.Part.from_text(text=f"{system_prompt}\n\n{user_prompt}")],
                ),
            ]
            
            tools = [types.Tool(googleSearch=types.GoogleSearch())]
            
            # CHỈNH SỬA TẠI ĐÂY: Dùng thinking_budget cho dòng 2.5
            generate_content_config = types.GenerateContentConfig(
                thinking_config=types.ThinkingConfig(
                    include_thoughts=True, # Cho phép nhận cả phần suy nghĩ của AI
                    thinking_budget=1024,   # Số token dành cho việc suy nghĩ (thay vì HIGH/LOW)
                ),
                tools=tools,
                temperature=1.0,  # Lưu ý: Khi bật Thinking, Google khuyến khích temp=1.0
                max_output_tokens=2048,
            )
            
            response_text = ""
            # Thực hiện stream response
            for chunk in self._client.models.generate_content_stream(
                model=model,
                contents=contents,
                config=generate_content_config,
            ):
                if chunk.text:
                    response_text += chunk.text
            
            if not response_text:
                logger.warning("Gemini trả về response rỗng")
                return ""
            
            logger.info(f"✅ Gemini response thành công")
            return response_text.strip()
            
        except Exception as e:
            logger.error(f"❌ Lỗi Gemini API: {e}", exc_info=True)
            raise
    
    async def generate_conversation_title(self, first_message: str) -> str:
        """
        Tạo tiêu đề conversation từ câu hỏi đầu tiên
        
        Args:
            first_message: Câu hỏi đầu tiên của user
            
        Returns:
            str: Tiêu đề ngắn gọn (tối đa 50 ký tự)
        """
        if not self._client:
            raise RuntimeError("Gemini Client chưa được khởi tạo")

        # Nếu câu hỏi quá ngắn, dùng luôn
        if len(first_message) <= 50:
            return first_message

        # Đợi rate limit
        await self._wait_for_rate_limit()

        try:
            system_prompt = """Bạn là chuyên gia tóm tắt. 
Nhiệm vụ: Tạo tiêu đề ngắn gọn (tối đa 50 ký tự) cho cuộc hội thoại dựa trên câu hỏi đầu tiên.
Tiêu đề phải:
- Ngắn gọn, súc tích
- Thể hiện chủ đề chính
- Không có dấu chấm câu ở cuối
- Tối đa 50 ký tự

Ví dụ:
Input: "Tội trộm cắp tài sản theo Bộ luật Hình sự 2015 bị xử phạt như thế nào?"
Output: "Hỏi về tội trộm cắp tài sản"

Input: "Hợp đồng lao động có thời hạn được ký tối đa bao lâu?"
Output: "Hợp đồng lao động có thời hạn"
"""

            user_prompt = f"Câu hỏi: {first_message}\n\nTiêu đề ngắn gọn:"

            # Không dùng thinking mode và search cho việc đặt tên
            model = "gemini-2.5-flash"
            
            contents = [
                types.Content(
                    role="user",
                    parts=[types.Part.from_text(text=f"{system_prompt}\n\n{user_prompt}")],
                ),
            ]
            
            generate_content_config = types.GenerateContentConfig(
                temperature=0.3,  # Hơi cao để đa dạng
                max_output_tokens=100,  # Ngắn thôi
            )
            
            response_text = ""
            for chunk in self._client.models.generate_content_stream(
                model=model,
                contents=contents,
                config=generate_content_config,
            ):
                if chunk.text:
                    response_text += chunk.text
            
            title = response_text.strip()
            
            # Giới hạn 50 ký tự
            if len(title) > 50:
                title = title[:47] + "..."
            
            logger.info(f"✅ Generated title: {title}")
            return title
            
        except Exception as e:
            logger.error(f"❌ Lỗi tạo tiêu đề: {e}")
            # Fallback: dùng 50 ký tự đầu của câu hỏi
            return first_message[:47] + "..." if len(first_message) > 50 else first_message
    
    async def _wait_for_rate_limit(self):
        """
        Đảm bảo khoảng cách tối thiểu giữa các request Gemini
        - Async (không block event loop)
        - Dùng chung cho toàn bộ app (singleton-safe)
        """
        now = time.monotonic()
        elapsed = now - GeminiClient._last_request_ts

        if elapsed < GeminiClient._min_interval_sec:
            sleep_time = GeminiClient._min_interval_sec - elapsed
            logger.debug(
                f"[GEMINI] Rate limit sleep {sleep_time:.2f}s"
            )
            await asyncio.sleep(sleep_time)

        GeminiClient._last_request_ts = time.monotonic()