package com.example.backend.search.dto;

import com.example.backend.search.entity.SearchModule;
import com.example.backend.search.entity.SearchType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryRequest {
    
    @NotBlank(message = "Search keyword không được để trống")
    @Size(max = 500, message = "Search keyword không được vượt quá 500 ký tự")
    @JsonProperty("searchKeyword")
    private String searchKeyword;
    
    @NotNull(message = "Search module không được null")
    @JsonProperty("searchModule")
    private SearchModule searchModule;
    
    @NotNull(message = "Search type không được null")
    @JsonProperty("searchType")
    private SearchType searchType;
    
    @JsonProperty("resultCount")
    private Integer resultCount;
    
    @JsonProperty("filters")
    private Map<String, Object> filters;
    
    @JsonProperty("executionTime")
    private Long executionTime;
    
    // Constructor for basic search history
    public SearchHistoryRequest(String searchKeyword, SearchModule searchModule, SearchType searchType) {
        this.searchKeyword = searchKeyword;
        this.searchModule = searchModule;
        this.searchType = searchType;
    }
    
    // Constructor for search with results
    public SearchHistoryRequest(String searchKeyword, SearchModule searchModule, SearchType searchType, Integer resultCount) {
        this.searchKeyword = searchKeyword;
        this.searchModule = searchModule;
        this.searchType = searchType;
        this.resultCount = resultCount;
    }
    
    // Validation method
    public boolean isValid() {
        return searchKeyword != null && !searchKeyword.trim().isEmpty() 
            && searchModule != null 
            && searchType != null;
    }
    
    // Helper method to clean keyword
    public String getCleanKeyword() {
        return searchKeyword != null ? searchKeyword.trim() : null;
    }
}