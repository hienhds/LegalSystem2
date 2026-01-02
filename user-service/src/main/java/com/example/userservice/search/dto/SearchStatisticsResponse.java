package com.example.userservice.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchStatisticsResponse {
    
    @JsonProperty("total_searches")
    private Long totalSearches;
    
    @JsonProperty("unique_users")
    private Long uniqueUsers;
    
    @JsonProperty("searches_today")
    private Long searchesToday;
    
    @JsonProperty("searches_this_week")
    private Long searchesThisWeek;
    
    @JsonProperty("searches_this_month")
    private Long searchesThisMonth;
    
    @JsonProperty("popular_keywords")
    private Map<String, Long> popularKeywords;
    
    @JsonProperty("search_by_module")
    private Map<String, Long> searchByModule;
    
    @JsonProperty("search_by_type")
    private Map<String, Long> searchByType;
    
    @JsonProperty("average_execution_time_ms")
    private Double averageExecutionTime;
    
    @JsonProperty("total_results_found")
    private Long totalResultsFound;
    
    @JsonProperty("average_results_per_search")
    private Double averageResultsPerSearch;
    
    // Helper methods for percentage calculations
    public double getSuccessRate() {
        if (totalSearches == null || totalSearches == 0) return 0.0;
        return totalResultsFound != null && totalResultsFound > 0 ? 
               (double) totalResultsFound / totalSearches * 100 : 0.0;
    }
    
    public double getGrowthRate() {
        // This could be enhanced to calculate growth compared to previous period
        return 0.0;
    }
    
    // Check if statistics are available
    public boolean hasData() {
        return totalSearches != null && totalSearches > 0;
    }
}
