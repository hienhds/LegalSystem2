package com.example.backend.case_management.dto;

import com.example.backend.case_management.entity.CaseStatus;
import lombok.Data;

@Data
public class UpdateProgressRequest {
    private String title;       // Ví dụ: "Đã nộp đơn lên tòa"
    private String description; // Chi tiết: "Tòa án hẹn ngày..."
    private CaseStatus status;  // (Tùy chọn) Cập nhật luôn trạng thái vụ án nếu cần
}