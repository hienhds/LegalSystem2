from datetime import datetime


def chat_history_doc(user_id, question, answer):
    return {
        "user_id": user_id,
        "question": question,
        "answer": answer,
        "created_at": datetime.utcnow()
    }
