from fastapi import FastAPI
from app.api.chat import router as chat_router

app = FastAPI(
    title="Chatbot Service",
    version="1.0.0"
)

app.include_router(chat_router)

@app.get("/health")
def health():
    return {"status": "UP"}
