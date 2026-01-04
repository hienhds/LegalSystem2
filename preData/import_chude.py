import json
from pymongo import MongoClient

# ================== CONFIG ==================
JSON_FILE = "Data/json_table/chude.js"

MONGO_URI = (
    "mongodb+srv://hienhoa20022003_db_user:231123"
    "@cluster0.wpmpzwz.mongodb.net/document_db"
    "?retryWrites=true&w=majority"
    "&tls=true"
    "&serverSelectionTimeoutMS=5000"
)
MONGO_DB = "document_db"
MONGO_COLLECTION = "chu_de"
# ============================================


def main():
    # Kết nối MongoDB
    client = MongoClient(MONGO_URI)
    db = client[MONGO_DB]
    col = db[MONGO_COLLECTION]

    # Đọc file JSON
    with open(JSON_FILE, encoding="utf-8") as f:
        data = json.load(f)

    total = 0

    for item in data:
        chu_de_id = item.get("Value")
        text = item.get("Text")

        if not chu_de_id or not text:
            continue

        doc = {
            "chu_de_id": chu_de_id,
            "text": text
        }

        col.update_one(
            {"chu_de_id": chu_de_id},
            {"$set": doc},
            upsert=True
        )

        total += 1

    print("===================================")
    print("✅ IMPORT CHỦ ĐỀ DONE")
    print(f"📕 Tổng số chủ đề: {total}")
    print("===================================")


if __name__ == "__main__":
    main()
