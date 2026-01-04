import json
from pymongo import MongoClient

# ================== CONFIG ==================
JSON_FILE = "Data/json_table/demuc.js"

MONGO_URI = (
    "mongodb+srv://hienhoa20022003_db_user:231123"
    "@cluster0.wpmpzwz.mongodb.net/document_db"
    "?retryWrites=true&w=majority"
    "&tls=true"
    "&serverSelectionTimeoutMS=5000"
)
MONGO_DB = "document_db"
MONGO_COLLECTION = "de_muc"
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
        de_muc_id = item.get("Value")
        text = item.get("Text")
        chu_de_id = item.get("ChuDe")

        if not de_muc_id or not text or not chu_de_id:
            continue

        doc = {
            "de_muc_id": de_muc_id,
            "text": text,
            "chu_de_id": chu_de_id
        }

        col.update_one(
            {"de_muc_id": de_muc_id},
            {"$set": doc},
            upsert=True
        )

        total += 1

    print("===================================")
    print("✅ IMPORT ĐỀ MỤC DONE")
    print(f"📘 Tổng số đề mục: {total}")
    print("===================================")


if __name__ == "__main__":
    main()
