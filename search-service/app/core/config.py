import os

MONGO_URI = os.getenv(
    "MONGO_URI",
    "mongodb+srv://hienhoa20022003_db_user:231123@cluster0.wpmpzwz.mongodb.net/document_db?authSource=admin&retryWrites=true&w=majority"
)

MONGO_DB = "document_db"
COLLECTION_DIEU = "dieu"
VECTOR_INDEX = "dieu_vector_index"
EMBEDDING_MODEL_PATH = "models/all-MiniLM-L12-v2"