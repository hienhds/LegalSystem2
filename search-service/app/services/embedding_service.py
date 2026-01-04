from sentence_transformers import SentenceTransformer
from app.core.config import EMBEDDING_MODEL_PATH

model = SentenceTransformer(EMBEDDING_MODEL_PATH)

def embed_text(text: str):
    return model.encode(text).tolist()
