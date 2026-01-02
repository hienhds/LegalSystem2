package com.example.userservice.search.dto;

import com.example.userservice.search.entity.SearchModule;
import com.example.userservice.search.entity.SearchType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("searchKeyword")
    private String searchKeyword;
    
    @JsonProperty("searchModule")
    private SearchModule searchModule;
    
    @JsonProperty("searchType")
    private SearchType searchType;
    
    @JsonProperty("resultCount")
    private Integer resultCount;
    
    @JsonProperty("filters")
    private Map<String, Object> filters;
    
    @JsonProperty("executionTime")
    private Long executionTime;
    
    @JsonProperty("searchedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime searchedAt;
    
    // Constructor for basic response
    public SearchHistoryResponse(Long id, String searchKeyword, SearchModule searchModule, 
                                SearchType searchType, LocalDateTime searchedAt) {
        this.id = id;
        this.searchKeyword = searchKeyword;
        this.searchModule = searchModule;
        this.searchType = searchType;
        this.searchedAt = searchedAt;
    }
    
    // Constructor with result count
    public SearchHistoryResponse(Long id, String searchKeyword, SearchModule searchModule, 
                                SearchType searchType, Integer resultCount, LocalDateTime searchedAt) {
        this.id = id;
        this.searchKeyword = searchKeyword;
        this.searchModule = searchModule;
        this.searchType = searchType;
        this.resultCount = resultCount;
        this.searchedAt = searchedAt;
    }
    
    // Helper methods for display
    public String getFormattedSearchTime() {
        if (searchedAt != null) {
            return searchedAt.toString();
        }
        return null;
    }
    
    public String getSearchModuleDisplayName() {
        return searchModule != null ? searchModule.getDisplayName() : null;
    }
    
    public String getSearchTypeDisplayName() {
        return searchType != null ? searchType.getDisplayName() : null;
    }
    
    // Check if this search had results
    public boolean hasResults() {
        return resultCount != null && resultCount > 0;
    }
    
    // Check if filters were applied
    public boolean hasFilters() {
        return filters != null && !filters.isEmpty();
    }
}
