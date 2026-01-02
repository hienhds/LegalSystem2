# HƯỚNG DẪN IMPORT DỮ LIỆU DOCUMENT SERVICE

## Yêu cầu
- Python 3.x
- pymongo library: `pip install pymongo`
- beautifulsoup4 (cho import_chuong_dieu_to_mongo.py): `pip install beautifulsoup4`
- MongoDB đang chạy trên localhost:27017

## Cấu trúc dữ liệu
```
document_db/
├── chu_de (Chủ đề)
├── de_muc (Đề mục)
├── chuong (Chương)
└── dieu (Điều)
```

## Cách chạy

### Bước 1: Cài đặt thư viện Python
```bash
pip install pymongo beautifulsoup4
```

### Bước 2: Import theo thứ tự
Chạy các script theo thứ tự phân cấp từ trên xuống dưới:

```bash
# 1. Import Chủ đề
cd preData
python import_chude.py

# 2. Import Đề mục
python import_demuc.py

# 3. Import Chương và Điều
python import_chuong_dieu_to_mongo.py
```

## Kiểm tra dữ liệu

### Sử dụng MongoDB Shell
```bash
# Kết nối MongoDB
docker exec -it mongodb mongosh -u admin -p 231123

# Chuyển sang database
use document_db

# Kiểm tra số lượng documents
db.chu_de.countDocuments()
db.de_muc.countDocuments()
db.chuong.countDocuments()
db.dieu.countDocuments()

# Xem dữ liệu mẫu
db.chu_de.findOne()
db.de_muc.findOne()
db.chuong.findOne()
db.dieu.findOne()
```

### Sử dụng API
```bash
# Lấy danh sách chủ đề
curl http://localhost:8087/api/documents/chu-de

# Lấy đề mục theo chủ đề
curl http://localhost:8087/api/documents/chu-de/{chuDeId}
```

## Tạo dữ liệu mẫu đơn giản (nếu không có file Data/)

Nếu chưa có dữ liệu trong folder `Data/`, bạn có thể tạo dữ liệu mẫu bằng MongoDB shell:

```javascript
// Kết nối MongoDB
docker exec -it mongodb mongosh -u admin -p 231123

use document_db

// Insert Chủ đề
db.chu_de.insertMany([
  {
    chu_de_id: "CDT001",
    text: "Luật Dân sự"
  },
  {
    chu_de_id: "CDT002", 
    text: "Luật Hình sự"
  },
  {
    chu_de_id: "CDT003",
    text: "Luật Lao động"
  }
])

// Insert Đề mục
db.de_muc.insertMany([
  {
    de_muc_id: "DM001",
    text: "Quy định chung",
    chu_de_id: "CDT001"
  },
  {
    de_muc_id: "DM002",
    text: "Quyền và nghĩa vụ của công dân",
    chu_de_id: "CDT001"
  },
  {
    de_muc_id: "DM003",
    text: "Tội phạm và hình phạt",
    chu_de_id: "CDT002"
  }
])

// Insert Chương
db.chuong.insertMany([
  {
    chuong_id: "C001",
    text: "Chương 1: Những quy định chung",
    de_muc_id: "DM001",
    order: 1
  },
  {
    chuong_id: "C002",
    text: "Chương 2: Thể nhân",
    de_muc_id: "DM002",
    order: 2
  }
])

// Insert Điều
db.dieu.insertMany([
  {
    dieu_id: "D001",
    chuong_id: "C001",
    tieu_de: "Điều 1. Phạm vi điều chỉnh",
    noi_dung: [
      "Bộ luật này quy định về các quan hệ dân sự phát sinh giữa các cá nhân, pháp nhân và các chủ thể khác.",
      "Các quan hệ dân sự bao gồm quan hệ về nhân thân và quan hệ về tài sản."
    ],
    ghi_chu: [],
    chi_dan: []
  },
  {
    dieu_id: "D002",
    chuong_id: "C001",
    tieu_de: "Điều 2. Áp dụng Bộ luật Dân sự",
    noi_dung: [
      "Bộ luật này áp dụng đối với mọi quan hệ dân sự phát sinh trên lãnh thổ Việt Nam.",
      "Trường hợp điều ước quốc tế mà Việt Nam là thành viên có quy định khác với quy định của Bộ luật này thì áp dụng quy định của điều ước quốc tế đó."
    ],
    ghi_chu: [],
    chi_dan: []
  }
])

// Verify data
db.chu_de.countDocuments()
db.de_muc.countDocuments()
db.chuong.countDocuments()
db.dieu.countDocuments()
```

## Troubleshooting

### Lỗi kết nối MongoDB
- Kiểm tra MongoDB đang chạy: `docker ps | grep mongodb`
- Kiểm tra port: MongoDB phải chạy trên port 27017
- Kiểm tra credentials: username=admin, password=231123

### Lỗi import script
- Kiểm tra file Data/ có tồn tại không
- Kiểm tra encoding của file JSON (phải là UTF-8)
- Kiểm tra pymongo đã cài đặt: `pip list | grep pymongo`

## Notes
- MongoDB ObjectId sẽ được tự động tạo cho mỗi document
- Các script sử dụng `upsert=True` nên có thể chạy lại nhiều lần
- Thứ tự import quan trọng: chu_de → de_muc → chuong → dieu
