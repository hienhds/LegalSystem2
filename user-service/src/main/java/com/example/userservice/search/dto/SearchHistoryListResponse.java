package com.example.userservice.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryListResponse {
    
    @JsonProperty("histories")
    private List<SearchHistoryResponse> histories;
    
    @JsonProperty("total_count")
    private Long totalCount;
    
    @JsonProperty("page")
    private Integer page;
    
    @JsonProperty("page_size")
    private Integer pageSize;
    
    @JsonProperty("total_pages")
    private Integer totalPages;
    
    @JsonProperty("has_next")
    private Boolean hasNext;
    
    @JsonProperty("has_previous")
    private Boolean hasPrevious;
    
    // Constructor for paginated response
    public SearchHistoryListResponse(List<SearchHistoryResponse> histories, 
                                   Long totalCount, Integer page, Integer pageSize) {
        this.histories = histories;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(totalCount, pageSize);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
    
    // Constructor for simple list response
    public SearchHistoryListResponse(List<SearchHistoryResponse> histories) {
        this.histories = histories;
        this.totalCount = (long) histories.size();
        this.page = 0;
        this.pageSize = histories.size();
        this.totalPages = 1;
        this.hasNext = false;
        this.hasPrevious = false;
    }
    
    private Integer calculateTotalPages(Long totalCount, Integer pageSize) {
        if (totalCount == null || pageSize == null || pageSize == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }
    
    // Helper methods
    public boolean isEmpty() {
        return histories == null || histories.isEmpty();
    }
    
    public int getSize() {
        return histories != null ? histories.size() : 0;
    }
}
