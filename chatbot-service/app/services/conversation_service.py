
from motor.motor_asyncio import AsyncIOMotorClient
from datetime import datetime
from typing import List, Optional
from bson import ObjectId
import os
import logging

from app.models.conversation import (
    Conversation, 
    Message, 
    ConversationListItem,
    ConversationResponse
)

logger = logging.getLogger(__name__)

class ConversationService:
    """Service để quản lý conversations trong MongoDB"""
    
    def __init__(self):
        mongo_uri = os.getenv("MONGO_URI")
        mongo_db = os.getenv("MONGO_DB", "chatbot_db")
        
        self.client = AsyncIOMotorClient(mongo_uri)
        self.db = self.client[mongo_db]
        self.collection = self.db["conversations"]
        
        logger.info(f"✅ ConversationService connected to MongoDB: {mongo_db}")
    
    async def create_conversation(
        self, 
        user_id: int, 
        title: str,
        first_message: Optional[str] = None
    ) -> str:
        """
        Tạo conversation mới
        
        Args:
            user_id: ID của user
            title: Tiêu đề conversation
            first_message: Tin nhắn đầu tiên (optional)
            
        Returns:
            str: ID của conversation mới
        """
        try:
            messages = []
            if first_message:
                messages.append(Message(
                    role="user",
                    content=first_message,
                    timestamp=datetime.utcnow()
                ).dict())
            
            conversation = {
                "user_id": user_id,
                "title": title,
                "messages": messages,
                "created_at": datetime.utcnow(),
                "updated_at": datetime.utcnow(),
                "is_active": True
            }
            
            result = await self.collection.insert_one(conversation)
            conversation_id = str(result.inserted_id)
            
            logger.info(f"✅ Created conversation {conversation_id} for user {user_id}")
            return conversation_id
            
        except Exception as e:
            logger.error(f"❌ Error creating conversation: {e}")
            raise
    
    async def get_conversation(self, conversation_id: str, user_id: int) -> Optional[ConversationResponse]:
        """
        Lấy chi tiết conversation
        
        Args:
            conversation_id: ID của conversation
            user_id: ID của user (để verify ownership)
            
        Returns:
            ConversationResponse hoặc None nếu không tìm thấy
        """
        try:
            conversation = await self.collection.find_one({
                "_id": ObjectId(conversation_id),
                "user_id": user_id
            })
            
            if not conversation:
                return None
            
            # Convert ObjectId to string
            conversation["_id"] = str(conversation["_id"])
            conversation["message_count"] = len(conversation.get("messages", []))
            
            return ConversationResponse(**conversation)
            
        except Exception as e:
            logger.error(f"❌ Error getting conversation: {e}")
            return None
    
    async def list_conversations(
        self, 
        user_id: int,
        skip: int = 0,
        limit: int = 20,
        active_only: bool = True
    ) -> List[ConversationListItem]:
        """
        Lấy danh sách conversations của user
        
        Args:
            user_id: ID của user
            skip: Số lượng skip (pagination)
            limit: Số lượng tối đa
            active_only: Chỉ lấy active conversations
            
        Returns:
            List[ConversationListItem]
        """
        try:
            query = {"user_id": user_id}
            if active_only:
                query["is_active"] = True
            
            cursor = self.collection.find(query).sort("updated_at", -1).skip(skip).limit(limit)
            
            conversations = []
            async for conv in cursor:
                messages = conv.get("messages", [])
                last_message = messages[-1]["content"] if messages else None
                
                conversations.append(ConversationListItem(
                    _id=str(conv["_id"]),
                    user_id=conv["user_id"],
                    title=conv["title"],
                    message_count=len(messages),
                    last_message=last_message[:100] if last_message else None,  # Truncate
                    created_at=conv["created_at"],
                    updated_at=conv["updated_at"],
                    is_active=conv["is_active"]
                ))
            
            logger.info(f"✅ Listed {len(conversations)} conversations for user {user_id}")
            return conversations
            
        except Exception as e:
            logger.error(f"❌ Error listing conversations: {e}")
            return []
    
    async def add_message(
        self, 
        conversation_id: str,
        user_id: int,
        role: str,
        content: str,
        metadata: Optional[dict] = None
    ) -> bool:
        """
        Thêm message vào conversation
        
        Args:
            conversation_id: ID của conversation
            user_id: ID của user (để verify)
            role: "user" hoặc "assistant"
            content: Nội dung message
            metadata: Metadata (context, queries, etc)
            
        Returns:
            bool: Success or not
        """
        try:
            message = Message(
                role=role,
                content=content,
                timestamp=datetime.utcnow(),
                metadata=metadata
            ).dict()
            
            result = await self.collection.update_one(
                {
                    "_id": ObjectId(conversation_id),
                    "user_id": user_id
                },
                {
                    "$push": {"messages": message},
                    "$set": {"updated_at": datetime.utcnow()}
                }
            )
            
            if result.modified_count > 0:
                logger.info(f"✅ Added {role} message to conversation {conversation_id}")
                return True
            else:
                logger.warning(f"⚠️ No conversation found or not modified: {conversation_id}")
                return False
                
        except Exception as e:
            logger.error(f"❌ Error adding message: {e}")
            return False
    
    async def update_conversation(
        self,
        conversation_id: str,
        user_id: int,
        title: Optional[str] = None,
        is_active: Optional[bool] = None
    ) -> bool:
        """
        Update conversation metadata
        
        Args:
            conversation_id: ID của conversation
            user_id: ID của user
            title: Tiêu đề mới (optional)
            is_active: Active status (optional)
            
        Returns:
            bool: Success or not
        """
        try:
            update_data = {"updated_at": datetime.utcnow()}
            
            if title is not None:
                update_data["title"] = title
            if is_active is not None:
                update_data["is_active"] = is_active
            
            result = await self.collection.update_one(
                {
                    "_id": ObjectId(conversation_id),
                    "user_id": user_id
                },
                {"$set": update_data}
            )
            
            if result.modified_count > 0:
                logger.info(f"✅ Updated conversation {conversation_id}")
                return True
            else:
                return False
                
        except Exception as e:
            logger.error(f"❌ Error updating conversation: {e}")
            return False
    
    async def delete_conversation(self, conversation_id: str, user_id: int, soft_delete: bool = True) -> bool:
        """
        Xóa conversation (soft hoặc hard delete)
        
        Args:
            conversation_id: ID của conversation
            user_id: ID của user
            soft_delete: True = set is_active=False, False = xóa hẳn
            
        Returns:
            bool: Success or not
        """
        try:
            if soft_delete:
                result = await self.collection.update_one(
                    {
                        "_id": ObjectId(conversation_id),
                        "user_id": user_id
                    },
                    {
                        "$set": {
                            "is_active": False,
                            "updated_at": datetime.utcnow()
                        }
                    }
                )
                success = result.modified_count > 0
            else:
                result = await self.collection.delete_one({
                    "_id": ObjectId(conversation_id),
                    "user_id": user_id
                })
                success = result.deleted_count > 0
            
            if success:
                action = "Soft deleted" if soft_delete else "Deleted"
                logger.info(f"✅ {action} conversation {conversation_id}")
            
            return success
            
        except Exception as e:
            logger.error(f"❌ Error deleting conversation: {e}")
            return False
    
    async def get_active_conversation(self, user_id: int) -> Optional[str]:
        """
        Lấy conversation active gần nhất của user
        
        Args:
            user_id: ID của user
            
        Returns:
            str: Conversation ID hoặc None
        """
        try:
            conversation = await self.collection.find_one(
                {"user_id": user_id, "is_active": True},
                sort=[("updated_at", -1)]
            )
            
            if conversation:
                return str(conversation["_id"])
            return None
            
        except Exception as e:
            logger.error(f"❌ Error getting active conversation: {e}")
            return None


# Singleton instance
conversation_service = ConversationService()