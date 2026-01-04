from sentence_transformers import SentenceTransformer
import os

MODEL_NAME = "sentence-transformers/all-MiniLM-L12-v2"
SAVE_DIR = "models/all-MiniLM-L12-v2"

os.makedirs(SAVE_DIR, exist_ok=True)

print("Downloading model...")
model = SentenceTransformer(MODEL_NAME)
model.save(SAVE_DIR)
print("Model saved to:", SAVE_DIR)
