package com.example.backend.search.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO chứa thông tin thống kê cleanup search history
 */
@Data
@Builder
public class CleanupStatsResponse {
    
    /**
     * Tổng số records hiện tại
     */
    private long totalRecords;
    
    /**
     * Số records cũ sẽ bị xóa
     */
    private long oldRecords;
    
    /**
     * Số records sẽ giữ lại
     */
    private long recordsToKeep;
    
    /**
     * Số ngày retention
     */
    private int retentionDays;
    
    /**
     * Đường cắt thời gian
     */
    private LocalDateTime cutoffDate;
    
    /**
     * Phần trăm sẽ bị xóa
     */
    private double cleanupPercentage;
    
    /**
     * Ước tính size tổng cộng (KB)
     */
    private double estimatedTotalSizeKB;
    
    /**
     * Ước tính size cũ sẽ xóa (KB)
     */
    private double estimatedOldSizeKB;
    
    /**
     * Thời gian thực hiện cleanup
     */
    private LocalDateTime lastCleanupTime;
    
    /**
     * Số records đã xóa trong lần cleanup cuối
     */
    private Long lastCleanupDeletedCount;
    
    /**
     * Trạng thái cleanup
     */
    private String status;
    
    public static CleanupStatsResponse success(String message) {
        return CleanupStatsResponse.builder()
                .status("SUCCESS")
                .build();
    }
    
    public static CleanupStatsResponse failure(String message) {
        return CleanupStatsResponse.builder()
                .status("ERROR")
                .build();
    }
}