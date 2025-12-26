package com.example.backend.case_management.entity;

public enum CaseStatus {
    PENDING_APPROVAL, // Chờ duyệt (nếu cần)
    IN_PROGRESS,      // Đang thực hiện
    COMPLETED,        // Hoàn thành
    FAILED,           // Thất bại
    CANCELLED         // Đã hủy
}