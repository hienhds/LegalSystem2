package com.example.backend.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSearchRequest {
    
    @Size(max = 200, message = "Search keyword must not exceed 200 characters")
    private String keyword;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @Min(value = 0, message = "Page number must be non-negative")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer size = 10;
    
    @Size(max = 20, message = "Sort field must not exceed 20 characters")
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Size(max = 4, message = "Sort direction must be 'asc' or 'desc'")
    @Builder.Default
    private String sortDirection = "desc";
    
    // Helper methods for validation
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }
    
    public String getCleanKeyword() {
        return hasKeyword() ? keyword.trim() : null;
    }
    
    public String getCleanCategory() {
        return hasCategory() ? category.trim() : null;
    }
}