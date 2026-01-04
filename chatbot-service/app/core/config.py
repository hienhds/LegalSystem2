import os
from dotenv import load_dotenv
load_dotenv()  
JWT_SECRET = os.getenv("JWT_SECRET")
JWT_ALGORITHM = "HS256"

MONGO_URI = os.getenv("MONGO_URI")
MONGO_DB = "document_db"

SEARCH_SERVICE_URL =  "http://search-service:8090"

# OPENROUTER_API_KEY = "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"
# OPENROUTER_BASE = os.getenv("OPENROUTER_BASE", "https://openrouter.ai/api/v1")
# GEN_MODEL = os.getenv("GEN_MODEL", "openai/gpt-oss-20b:free")

GEMINI_API_KEY = "AIzaSyBkKBW75Oklch-DXEkHiCTKjWUybreubZA"

GEN_MODEL = os.getenv("GEN_MODEL", "gemini-1.5-flash")