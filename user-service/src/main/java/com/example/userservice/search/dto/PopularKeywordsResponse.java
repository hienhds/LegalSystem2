package com.example.userservice.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularKeywordsResponse {
    
    @JsonProperty("keywords")
    private List<KeywordStats> keywords;
    
    @JsonProperty("period")
    private String period; // "today", "week", "month", "all_time"
    
    @JsonProperty("total_count")
    private Integer totalCount;
    
    @JsonProperty("generated_at")
    private String generatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordStats {
        @JsonProperty("keyword")
        private String keyword;
        
        @JsonProperty("search_count")
        private Long searchCount;
        
        @JsonProperty("unique_users")
        private Long uniqueUsers;
        
        @JsonProperty("average_results")
        private Double averageResults;
        
        @JsonProperty("percentage")
        private Double percentage;
        
        // Constructor for basic keyword stats
        public KeywordStats(String keyword, Long searchCount) {
            this.keyword = keyword;
            this.searchCount = searchCount;
        }
    }
    
    // Helper methods
    public boolean isEmpty() {
        return keywords == null || keywords.isEmpty();
    }
    
    public int getSize() {
        return keywords != null ? keywords.size() : 0;
    }
}
