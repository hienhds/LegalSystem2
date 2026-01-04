import os
import re
from bs4 import BeautifulSoup, Tag
from pymongo import MongoClient
from sentence_transformers import SentenceTransformer

# ================== CONFIG ==================
HTML_DIR = "Data/demuc1"

MONGO_URI = (
    "mongodb+srv://hienhoa20022003_db_user:231123"
    "@cluster0.wpmpzwz.mongodb.net/document_db"
    "?retryWrites=true&w=majority"
    "&tls=true"
)

MONGO_DB = "document_db"
COLLECTION_CHUONG = "chuong"
COLLECTION_DIEU = "dieu"

EMBED_MODEL_NAME = "sentence-transformers/all-MiniLM-L12-v2"
# ============================================


# ===== PARSE ĐIỀU (GIỮ NGUYÊN LOGIC GỐC) =====
def parse_dieu(p_dieu: Tag):
    doc = {
        "tieu_de": p_dieu.get_text(" ", strip=True),
        "ghi_chu": [],
        "noi_dung": [],
        "chi_dan": []
    }

    cur = p_dieu.find_next_sibling()

    while cur:
        # gặp Điều mới thì dừng
        if cur.name == "p" and "pDieu" in (cur.get("class") or []):
            break

        # ===== GHI CHÚ =====
        if cur.name == "p" and "pGhiChu" in (cur.get("class") or []):
            a_tag = cur.find("a", href=True)
            doc["ghi_chu"].append({
                "text": cur.get_text(" ", strip=True),
                "link": a_tag["href"] if a_tag else None
            })

        # ===== NỘI DUNG =====
        elif cur.name == "p" and (
            "pNoiDung" in (cur.get("class") or [])
            or cur.get("align") == "justify"
        ):
            text = cur.get_text(" ", strip=True)
            if text:
                doc["noi_dung"].append(text)

        # ===== CHỈ DẪN =====
        elif cur.name == "p" and "pChiDan" in (cur.get("class") or []):
            for a in cur.find_all("a", onclick=True):
                m = re.search(r"ViewNoiDungPhapDien\('([^']+)'\)", a["onclick"])
                if m:
                    doc["chi_dan"].append({
                        "mapc": m.group(1),
                        "text": a.get_text(" ", strip=True)
                    })

        cur = cur.find_next_sibling()

    return doc


# ===== GHÉP TEXT ĐỂ EMBEDDING =====
def build_dieu_text_for_embedding(doc: dict) -> str:
    parts = []
    if doc.get("tieu_de"):
        parts.append(doc["tieu_de"])
    for nd in doc.get("noi_dung", []):
        if nd:
            parts.append(nd)
    return "\n".join(parts).strip()


def main():
    # ===== CONNECT MONGODB =====
    client = MongoClient(
        MONGO_URI,
        tls=True,
        tlsAllowInvalidCertificates=True
    )

    db = client[MONGO_DB]
    col_chuong = db[COLLECTION_CHUONG]
    col_dieu = db[COLLECTION_DIEU]

    # ===== LOAD MODEL =====
    print("⏳ Loading embedding model...")
    model = SentenceTransformer(EMBED_MODEL_NAME)
    print("✅ Model loaded")

    total_file = total_chuong = total_dieu = 0

    # ===== DUYỆT FILE HTML =====
    for file_name in os.listdir(HTML_DIR):
        if not file_name.lower().endswith(".html"):
            continue

        total_file += 1
        de_muc_id = file_name[:-5]
        file_path = os.path.join(HTML_DIR, file_name)

        print(f"\n📄 Đang xử lý file: {file_name}")

        with open(file_path, encoding="utf-8") as f:
            soup = BeautifulSoup(f, "lxml")

        # ==================================================
        # 1️⃣ XỬ LÝ CHƯƠNG (NỐI pChuong – LOGIC GỐC)
        # ==================================================
        current_chuong = None

        for p in soup.find_all("p"):
            if "pChuong" not in (p.get("class") or []):
                continue

            a = p.find("a", attrs={"name": True})

            # chương mới
            if a and a.get("name"):
                if current_chuong:
                    col_chuong.update_one(
                        {
                            "text": current_chuong["text"],
                            "de_muc_id": current_chuong["de_muc_id"]
                        },
                        {"$set": current_chuong},
                        upsert=True
                    )
                    total_chuong += 1

                current_chuong = {
                    "text": p.get_text(" ", strip=True),
                    "de_muc_id": de_muc_id
                }

            # nối dòng chương
            else:
                if current_chuong:
                    extra = p.get_text(" ", strip=True)
                    if extra:
                        current_chuong["text"] += " " + extra

        if current_chuong:
            col_chuong.update_one(
                {
                    "text": current_chuong["text"],
                    "de_muc_id": current_chuong["de_muc_id"]
                },
                {"$set": current_chuong},
                upsert=True
            )
            total_chuong += 1

        # ==================================================
        # 2️⃣ XỬ LÝ ĐIỀU + EMBEDDING (LOGIC GỐC)
        # ==================================================
        current_chuong_id = None

        for p in soup.find_all("p"):
            classes = p.get("class") or []

            # ----- XÁC ĐỊNH CHƯƠNG (BẮT BUỘC NỐI pChuong) -----
            if "pChuong" in classes:
                chuong_text = p.get_text(" ", strip=True)

                next_p = p.find_next_sibling("p")
                while next_p and "pChuong" in (next_p.get("class") or []):
                    a = next_p.find("a", attrs={"name": True})
                    if a and a.get("name"):
                        break
                    extra = next_p.get_text(" ", strip=True)
                    if extra:
                        chuong_text += " " + extra
                    next_p = next_p.find_next_sibling("p")

                chuong_doc = col_chuong.find_one({
                    "text": chuong_text,
                    "de_muc_id": de_muc_id
                })

                if chuong_doc:
                    current_chuong_id = chuong_doc["_id"]
                    print(f"   📕 Chương: {chuong_text[:80]}...")

            # ----- XỬ LÝ ĐIỀU -----
            elif "pDieu" in classes and current_chuong_id:
                doc = parse_dieu(p)
                doc["chuong_id"] = current_chuong_id

                embed_text = build_dieu_text_for_embedding(doc)
                if embed_text:
                    vector = model.encode(embed_text, normalize_embeddings=True)
                    doc["embedding"] = vector.tolist()

                col_dieu.insert_one(doc)
                total_dieu += 1

                print(f"      📘 {doc['tieu_de']}")

        print(f"✓ Hoàn thành file: {file_name}")

    # ===== SUMMARY =====
    print("\n===================================")
    print("✅ IMPORT + EMBEDDING DONE")
    print(f"📄 File HTML: {total_file}")
    print(f"📕 Chương: {total_chuong}")
    print(f"📘 Điều: {total_dieu}")
    print("===================================")


if __name__ == "__main__":
    main()
