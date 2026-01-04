from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.chat import router as chat_router
from app.api.conversations import router as conversations_router  # ✅ Thêm router mới
import logging
import sys

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger(__name__)

app = FastAPI(
    title="Chatbot Service - Legal Advisory System",
    version="2.0.0",
    description="Hệ thống tư vấn pháp luật tự động với lưu trữ conversations",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(chat_router)
app.include_router(conversations_router)  # ✅ Thêm conversations router

@app.on_event("startup")
async def startup_event():
    """
    Event chạy khi service khởi động
    """
    logger.info("=" * 50)
    logger.info("🚀 CHATBOT SERVICE STARTING...")
    logger.info("=" * 50)
    
    logger.info("✅ Đang kiểm tra kết nối Redis...")
    try:
        from app.services.memory_workspace import MemoryWorkspace
        memory = MemoryWorkspace()
        logger.info("✅ Redis connection OK")
    except Exception as e:
        logger.error(f"❌ Redis connection FAILED: {e}")
    
    logger.info("✅ Đang kiểm tra kết nối MongoDB...")
    try:
        from app.services.conversation_service import conversation_service
        logger.info("✅ MongoDB connection OK")
    except Exception as e:
        logger.error(f"❌ MongoDB connection FAILED: {e}")
    
    logger.info("✅ Đang khởi tạo Gemini Client...")
    try:
        from app.core.gemini_client import GeminiClient
        client = GeminiClient()
        logger.info("✅ Gemini Client loaded successfully")
    except Exception as e:
        logger.error(f"❌ Gemini Client loading FAILED: {e}")
    
    logger.info("=" * 50)
    logger.info("✅ CHATBOT SERVICE READY")
    logger.info("=" * 50)

@app.on_event("shutdown")
async def shutdown_event():
    """
    Event chạy khi service tắt
    """
    logger.info("🛑 CHATBOT SERVICE SHUTTING DOWN...")

@app.get("/")
def root():
    """
    Root endpoint
    """
    return {
        "service": "chatbot-service",
        "version": "2.0.0",
        "status": "running",
        "endpoints": {
            "chat": "/api/chatbot/ask",
            "conversations": {
                "create": "POST /api/conversations/",
                "list": "GET /api/conversations/",
                "get": "GET /api/conversations/{id}",
                "update": "PATCH /api/conversations/{id}",
                "delete": "DELETE /api/conversations/{id}",
                "active": "GET /api/conversations/active/latest"
            },
            "memory": {
                "clear": "DELETE /api/chatbot/memory/clear",
                "stats": "GET /api/chatbot/memory/stats",
                "history": "GET /api/chatbot/memory/history"
            },
            "health": "/health",
            "docs": "/docs"
        }
    }

@app.get("/health")
def health():
    """
    Health check endpoint
    """
    return {
        "status": "UP",
        "service": "chatbot-service"
    }