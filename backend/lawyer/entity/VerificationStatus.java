package com.example.backend.lawyer.entity;

public enum VerificationStatus {
    PENDING,    // Chờ duyệt
    APPROVED,   // Đã được xác thực
    REJECTED    // Từ chối (chứng chỉ sai, thiếu thông tin...)
}