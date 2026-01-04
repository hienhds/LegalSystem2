import redis
import json
import os
import logging
from typing import List, Dict
from datetime import datetime

logger = logging.getLogger(__name__)

class MemoryWorkspace:
    """
    Quản lý lịch sử hội thoại của người dùng trong Redis với chiến lược LRU
    - Key format: "chatbot:memory:{user_id}"
    - Value: JSON object chứa danh sách conversations
    - Mỗi conversation: {"question": str, "answer": str, "timestamp": str}
    - Max: 10 lượt hỏi-trả lời (20 messages)
    - TTL: 7 ngày (tự động xóa sau 1 tuần không hoạt động)
    - Strategy: LRU - xóa từ đầu khi vượt quá giới hạn
    """
    
    def __init__(self):
        redis_host = os.getenv("REDIS_HOST", "redis")
        redis_port = int(os.getenv("REDIS_PORT", "6379"))
        redis_db = int(os.getenv("REDIS_DB", "1"))
        redis_password = os.getenv("REDIS_PASSWORD", None)
        
        try:
            self.redis_client = redis.Redis(
                host=redis_host,
                port=redis_port,
                db=redis_db,
                password=redis_password,
                decode_responses=True,
                socket_timeout=5,
                socket_connect_timeout=5
            )
            # Test connection
            self.redis_client.ping()
            logger.info(f"✅ Kết nối Redis thành công: {redis_host}:{redis_port} DB={redis_db}")
        except redis.ConnectionError as e:
            logger.error(f"❌ Không thể kết nối Redis: {e}")
            raise
        
        self.max_turns = 10  # Tối đa 10 lượt hỏi-trả lời
        self.ttl_seconds = 604800  # 7 ngày = 7 * 24 * 60 * 60

    def _get_key(self, user_id: str) -> str:
        """Tạo Redis key cho user"""
        return f"chatbot:memory:{user_id}"

    def _get_memory_data(self, user_id: str) -> Dict:
        """
        Lấy dữ liệu memory dạng dict từ Redis
        
        Returns:
            Dict với cấu trúc:
            {
                "conversations": [
                    {"question": "...", "answer": "...", "timestamp": "..."},
                    ...
                ],
                "total_turns": int
            }
        """
        try:
            key = self._get_key(user_id)
            data = self.redis_client.get(key)
            
            if data:
                memory_data = json.loads(data)
                return memory_data
            else:
                return {"conversations": [], "total_turns": 0}
                
        except json.JSONDecodeError as e:
            logger.error(f"[MEMORY] Lỗi parse JSON: {e}")
            return {"conversations": [], "total_turns": 0}
        except redis.RedisError as e:
            logger.error(f"[MEMORY] Lỗi Redis khi get: {e}")
            return {"conversations": [], "total_turns": 0}

    def _save_memory_data(self, user_id: str, memory_data: Dict):
        """Lưu dữ liệu memory vào Redis với TTL"""
        try:
            key = self._get_key(user_id)
            self.redis_client.setex(
                key,
                self.ttl_seconds,
                json.dumps(memory_data, ensure_ascii=False)
            )
        except redis.RedisError as e:
            logger.error(f"[MEMORY] Lỗi Redis khi save: {e}")
            raise

    def get_conversation_history(self, user_id: str) -> List[str]:
        """
        Lấy lịch sử hội thoại dạng danh sách messages (để dùng cho contextualize)
        Trả về tối đa 5 câu hỏi gần nhất
        
        Args:
            user_id: ID người dùng
            
        Returns:
            List[str]: ["Q: câu hỏi 1", "A: câu trả lời 1", "Q: câu hỏi 2", ...]
        """
        try:
            memory_data = self._get_memory_data(user_id)
            conversations = memory_data.get("conversations", [])
            
            # Lấy 5 câu hỏi gần nhất (tối đa 10 messages)
            recent_conversations = conversations[-5:]
            
            # Flatten thành list messages
            messages = []
            for conv in recent_conversations:
                messages.append(f"Q: {conv['question']}")
                if conv.get('answer'):
                    messages.append(f"A: {conv['answer']}")
            
            logger.info(f"[MEMORY] Lấy {len(messages)} messages từ Redis cho user {user_id}")
            return messages
                
        except Exception as e:
            logger.error(f"[MEMORY] Lỗi khi get history: {e}")
            return []

    def append_question(self, user_id: str, question: str):
        """
        Thêm câu hỏi mới vào memory
        
        Args:
            user_id: ID người dùng
            question: Câu hỏi từ user
        """
        try:
            memory_data = self._get_memory_data(user_id)
            conversations = memory_data.get("conversations", [])
            
            # Thêm câu hỏi mới
            new_conversation = {
                "question": question,
                "answer": None,  # Sẽ được cập nhật sau
                "timestamp": datetime.utcnow().isoformat()
            }
            conversations.append(new_conversation)
            
            # Kiểm tra giới hạn: nếu vượt quá max_turns, xóa conversation đầu tiên
            if len(conversations) > self.max_turns:
                removed = conversations.pop(0)
                logger.info(f"[MEMORY] Xóa conversation cũ nhất (LRU): {removed['question'][:50]}...")
            
            memory_data["conversations"] = conversations
            memory_data["total_turns"] = len(conversations)
            
            self._save_memory_data(user_id, memory_data)
            
            logger.info(f"[MEMORY] Đã lưu câu hỏi cho user {user_id} (total: {len(conversations)} turns)")
            
        except Exception as e:
            logger.error(f"[MEMORY] Lỗi khi append question: {e}")

    def update_answer(self, user_id: str, answer: str):
        """
        Cập nhật câu trả lời cho câu hỏi cuối cùng
        
        Args:
            user_id: ID người dùng
            answer: Câu trả lời từ chatbot
        """
        try:
            memory_data = self._get_memory_data(user_id)
            conversations = memory_data.get("conversations", [])
            
            if conversations:
                # Cập nhật answer cho conversation cuối cùng
                conversations[-1]["answer"] = answer
                memory_data["conversations"] = conversations
                
                self._save_memory_data(user_id, memory_data)
                logger.info(f"[MEMORY] Đã cập nhật answer cho user {user_id}")
            else:
                logger.warning(f"[MEMORY] Không có conversation nào để cập nhật answer")
                
        except Exception as e:
            logger.error(f"[MEMORY] Lỗi khi update answer: {e}")

    def clear(self, user_id: str):
        """
        Xóa toàn bộ lịch sử của user
        
        Args:
            user_id: ID người dùng
        """
        try:
            key = self._get_key(user_id)
            self.redis_client.delete(key)
            logger.info(f"[MEMORY] Đã xóa lịch sử cho user {user_id}")
        except redis.RedisError as e:
            logger.error(f"[MEMORY] Lỗi Redis khi clear: {e}")

    def get_stats(self, user_id: str) -> dict:
        """
        Lấy thống kê về memory của user
        
        Args:
            user_id: ID người dùng
            
        Returns:
            dict: Thông tin thống kê
        """
        try:
            key = self._get_key(user_id)
            memory_data = self._get_memory_data(user_id)
            conversations = memory_data.get("conversations", [])
            ttl = self.redis_client.ttl(key)
            
            # Tính số messages
            total_messages = sum(
                2 if conv.get('answer') else 1 
                for conv in conversations
            )
            
            return {
                "user_id": user_id,
                "total_turns": len(conversations),
                "max_turns": self.max_turns,
                "total_messages": total_messages,
                "ttl_seconds": ttl if ttl > 0 else 0,
                "ttl_days": round(ttl / 86400, 2) if ttl > 0 else 0,
                "exists": len(conversations) > 0,
                "oldest_conversation": conversations[0]["timestamp"] if conversations else None,
                "newest_conversation": conversations[-1]["timestamp"] if conversations else None
            }
        except Exception as e:
            logger.error(f"[MEMORY] Lỗi khi lấy stats: {e}")
            return {
                "user_id": user_id,
                "total_turns": 0,
                "max_turns": self.max_turns,
                "total_messages": 0,
                "ttl_seconds": 0,
                "ttl_days": 0,
                "exists": False
            }

    def get_full_history(self, user_id: str) -> List[Dict]:
        """
        Lấy toàn bộ lịch sử conversations (để hiển thị UI)
        
        Args:
            user_id: ID người dùng
            
        Returns:
            List[Dict]: Danh sách conversations
        """
        try:
            memory_data = self._get_memory_data(user_id)
            return memory_data.get("conversations", [])
        except Exception as e:
            logger.error(f"[MEMORY] Lỗi khi get full history: {e}")
            return []