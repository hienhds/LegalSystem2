package com.example.userservice.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteHistoryResponse {
    
    @JsonProperty("deleted_count")
    private Long deletedCount;
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;
    
    // Constructor for successful deletion
    public DeleteHistoryResponse(Long deletedCount) {
        this.deletedCount = deletedCount;
        this.success = true;
        this.message = "Successfully deleted " + deletedCount + " search history records";
        this.deletedAt = LocalDateTime.now();
    }
    
    // Constructor for failure
    public DeleteHistoryResponse(String errorMessage) {
        this.deletedCount = 0L;
        this.success = false;
        this.message = errorMessage;
        this.deletedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static DeleteHistoryResponse success(Long deletedCount) {
        return new DeleteHistoryResponse(deletedCount);
    }
    
    public static DeleteHistoryResponse failure(String errorMessage) {
        return new DeleteHistoryResponse(errorMessage);
    }
    
    public static DeleteHistoryResponse noRecordsFound() {
        return new DeleteHistoryResponse(0L, true, "No search history records found to delete", LocalDateTime.now());
    }
}
