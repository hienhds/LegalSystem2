-- Tạo database cho user-service
CREATE DATABASE IF NOT EXISTS legal_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Tạo database cho chat-service
CREATE DATABASE IF NOT EXISTS chat_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS document_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;