import os
import logging
from google import genai
from google.genai import types

logger = logging.getLogger(__name__)

class GeminiClient:
    """
    Client đã sửa lỗi để tương thích với Gemini 2.5 Flash.
    Sử dụng thinking_budget thay vì thinking_level.
    """
    _instance = None
    _client = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(GeminiClient, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        if GeminiClient._client is None:
            # Lưu ý: Nên dùng os.environ.get("GEMINI_API_KEY") để bảo mật
            api_key = "AIzaSyA8eIe7xHleX1auoWZxdyu-hT6fuZAzvdU"
            
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