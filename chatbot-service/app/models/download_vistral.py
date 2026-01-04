import os
from huggingface_hub import hf_hub_download

# Tạo thư mục models nếu chưa có
if not os.path.exists("models"):
    os.makedirs("models")

print("⬇️ Đang tải Qwen 2.5-7B-Instruct (GGUF)...")

try:
    hf_hub_download(
        repo_id="bartowski/Qwen2.5-7B-Instruct-GGUF",
        filename="Qwen2.5-7B-Instruct-Q4_K_M.gguf", # Tên file chuẩn (có viết hoa)
        local_dir="models"
    )
    print("✅ Tải xong! File đã nằm trong thư mục models.")
except Exception as e:
    print(f"❌ Lỗi: {e}")