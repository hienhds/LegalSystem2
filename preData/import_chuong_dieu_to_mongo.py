import os
import re
from bs4 import BeautifulSoup, Tag
from pymongo import MongoClient
from bson import ObjectId

# ================== CONFIG ==================
HTML_DIR = "Data/demuc"

MONGO_URI = "mongodb://admin:231123@localhost:27017/?authSource=admin"
MONGO_DB = "document_db"
COLLECTION_CHUONG = "chuong"
COLLECTION_DIEU = "dieu"
# ============================================


def parse_dieu(p_dieu: Tag):
    """
    Parse 1 <p class="pDieu"> thành 1 document MongoDB
    """
    doc = {
        "tieu_de": p_dieu.get_text(" ", strip=True),
        "ghi_chu": [],
        "noi_dung": [],
        "chi_dan": []
    }

    cur = p_dieu.find_next_sibling()

    while cur:
        # STOP khi gặp Điều mới
        if cur.name == "p" and "pDieu" in (cur.get("class") or []):
            break

        # ===== GHI CHÚ (0..n) =====
        if cur.name == "p" and "pGhiChu" in (cur.get("class") or []):
            a_tag = cur.find("a", href=True)
            doc["ghi_chu"].append({
                "text": cur.get_text(" ", strip=True),
                "link": a_tag["href"] if a_tag else None
            })

        # ===== NỘI DUNG pNoiDung =====
        elif cur.name == "p" and "pNoiDung" in (cur.get("class") or []):
            text = cur.get_text(" ", strip=True)
            if text:
                doc["noi_dung"].append(text)

        # ===== NỘI DUNG p align="justify" =====
        elif cur.name == "p" and cur.get("align") == "justify":
            text = cur.get_text(" ", strip=True)
            if text:
                doc["noi_dung"].append(text)

        # ===== CHỈ DẪN (0..n) =====
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


def main():
    client = MongoClient(MONGO_URI)
    db = client[MONGO_DB]
    col_chuong = db[COLLECTION_CHUONG]
    col_dieu = db[COLLECTION_DIEU]

    total_file = 0
    total_chuong = 0
    total_dieu = 0

    for file_name in os.listdir(HTML_DIR):
        if not file_name.lower().endswith(".html"):
            continue

        total_file += 1
        de_muc_id = file_name[:-5]  # bỏ .html
        file_path = os.path.join(HTML_DIR, file_name)

        with open(file_path, encoding="utf-8") as f:
            soup = BeautifulSoup(f, "lxml")

        # ===== XỬ LÝ CHƯƠNG (logic cũ 100%) =====
        current_chuong = None  # chương đang ghép
        current_chuong_id = None  # _id của chương hiện tại

        for p in soup.find_all("p"):
            # ===== CHỈ QUAN TÂM pChuong =====
            if "pChuong" not in (p.get("class") or []):
                continue

            a = p.find("a", attrs={"name": True})

            # ===== GẶP CHƯƠNG MỚI =====
            if a and a.get("name"):
                # lưu chương cũ trước
                if current_chuong:
                    result = col_chuong.update_one(
                        {
                            "text": current_chuong["text"],
                            "de_muc_id": current_chuong["de_muc_id"]
                        },
                        {"$set": current_chuong},
                        upsert=True
                    )
                    
                    # Lấy _id của chương vừa lưu
                    if result.upserted_id:
                        current_chuong_id = result.upserted_id
                    else:
                        chuong_doc = col_chuong.find_one({
                            "text": current_chuong["text"],
                            "de_muc_id": current_chuong["de_muc_id"]
                        })
                        current_chuong_id = chuong_doc["_id"]
                    
                    total_chuong += 1

                # mở chương mới
                current_chuong = {
                    "text": p.get_text(" ", strip=True),
                    "de_muc_id": de_muc_id
                }
                current_chuong_id = None  # reset

            # ===== pChuong LIỀN KỀ (GHÉP TEXT) =====
            else:
                if current_chuong:
                    extra_text = p.get_text(" ", strip=True)
                    if extra_text:
                        current_chuong["text"] += " " + extra_text

        # ===== KẾT FILE: LƯU CHƯƠNG CUỐI =====
        if current_chuong:
            result = col_chuong.update_one(
                {
                    "text": current_chuong["text"],
                    "de_muc_id": current_chuong["de_muc_id"]
                },
                {"$set": current_chuong},
                upsert=True
            )
            
            if result.upserted_id:
                current_chuong_id = result.upserted_id
            else:
                chuong_doc = col_chuong.find_one({
                    "text": current_chuong["text"],
                    "de_muc_id": current_chuong["de_muc_id"]
                })
                current_chuong_id = chuong_doc["_id"]
            
            total_chuong += 1

        # ===== XỬ LÝ ĐIỀU (logic cũ + thêm chuong_id) =====
        # Duyệt lại từ đầu để xử lý điều và gán chuong_id
        current_chuong_id_for_dieu = None
        
        for p in soup.find_all("p"):
            classes = p.get("class") or []
            
            # Cập nhật current_chuong_id khi gặp chương mới
            if "pChuong" in classes:
                a = p.find("a", attrs={"name": True})
                if a and a.get("name"):
                    # Tìm _id của chương này
                    # Ghép text giống logic trên
                    chuong_text = p.get_text(" ", strip=True)
                    
                    # Tìm các pChuong liền kề sau nó
                    next_p = p.find_next_sibling("p")
                    while next_p and "pChuong" in (next_p.get("class") or []):
                        next_a = next_p.find("a", attrs={"name": True})
                        if next_a and next_a.get("name"):
                            break  # Gặp chương mới
                        # Ghép text
                        extra = next_p.get_text(" ", strip=True)
                        if extra:
                            chuong_text += " " + extra
                        next_p = next_p.find_next_sibling("p")
                    
                    # Tìm _id của chương này
                    chuong_doc = col_chuong.find_one({
                        "text": chuong_text,
                        "de_muc_id": de_muc_id
                    })
                    if chuong_doc:
                        current_chuong_id_for_dieu = chuong_doc["_id"]
            
            # Xử lý điều
            elif "pDieu" in classes and current_chuong_id_for_dieu:
                doc = parse_dieu(p)
                if doc:
                    # Thêm chuong_id vào điều
                    doc["chuong_id"] = current_chuong_id_for_dieu
                    
                    col_dieu.insert_one(doc)
                    total_dieu += 1

        print(f"✓ Đã xử lý: {file_name}")

    print("===================================")
    print("✅ IMPORT DONE")
    print(f"📄 Số file HTML: {total_file}")
    print(f"📕 Số Chương: {total_chuong}")
    print(f"📘 Số Điều: {total_dieu}")
    print("===================================")


if __name__ == "__main__":
    main()