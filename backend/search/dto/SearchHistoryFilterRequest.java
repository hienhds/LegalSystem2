package com.example.backend.search.dto;

import com.example.backend.search.entity.SearchModule;
import com.example.backend.search.entity.SearchType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryFilterRequest {
    
    @JsonProperty("search_module")
    private SearchModule searchModule;
    
    @JsonProperty("search_type")
    private SearchType searchType;
    
    @JsonProperty("keyword")
    private String keyword;
    
    @JsonProperty("start_date")
    private LocalDateTime startDate;
    
    @JsonProperty("end_date")
    private LocalDateTime endDate;
    
    @JsonProperty("has_results")
    private Boolean hasResults;
    
    @JsonProperty("min_result_count")
    @Min(value = 0, message = "Min result count phải >= 0")
    private Integer minResultCount;
    
    @JsonProperty("max_result_count")
    @Min(value = 0, message = "Max result count phải >= 0")
    private Integer maxResultCount;
    
    @JsonProperty("page")
    @Min(value = 0, message = "Page phải >= 0")
    private Integer page = 0;
    
    @JsonProperty("size")
    @Min(value = 1, message = "Size phải >= 1")
    private Integer size = 20;
    
    @JsonProperty("sort_by")
    private String sortBy = "searchTime"; // searchTime, keyword, resultCount
    
    @JsonProperty("sort_direction")
    private String sortDirection = "desc"; // asc, desc
    
    // Validation methods
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }
    
    public boolean isResultCountRangeValid() {
        if (minResultCount == null || maxResultCount == null) {
            return true;
        }
        return minResultCount <= maxResultCount;
    }
    
    public boolean isSortValid() {
        if (sortBy == null) return true;
        return sortBy.matches("searchTime|keyword|resultCount|searchModule|searchType");
    }
    
    public boolean isSortDirectionValid() {
        if (sortDirection == null) return true;
        return sortDirection.matches("asc|desc");
    }
    
    // Helper methods
    public String getCleanKeyword() {
        return keyword != null ? keyword.trim() : null;
    }
    
    public boolean hasKeywordFilter() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasDateFilter() {
        return startDate != null || endDate != null;
    }
    
    public boolean hasResultCountFilter() {
        return hasResults != null || minResultCount != null || maxResultCount != null;
    }
}