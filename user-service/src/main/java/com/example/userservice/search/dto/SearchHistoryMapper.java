package com.example.userservice.search.dto;

import com.example.userservice.search.entity.SearchHistory;
import com.example.userservice.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchHistoryMapper {
    
    /**
     * Convert SearchHistoryRequest to SearchHistory entity
     */
    public SearchHistory toEntity(SearchHistoryRequest request, User user) {
        if (request == null) {
            return null;
        }
        
        SearchHistory entity = new SearchHistory();
        entity.setUser(user);
        // Set search keyword cho cả 2 fields
        entity.setSearchKeyword(request.getCleanKeyword());
        entity.setSearchModule(request.getSearchModule());
        entity.setSearchType(request.getSearchType());
        // Set result count cho cả 2 fields với default = 0
        entity.setResultsCount(request.getResultCount() != null ? request.getResultCount() : 0);
        // Set filters cho cả 2 fields
        entity.setFilters(request.getFilters());
        entity.setExecutionTimeMs(request.getExecutionTime());
        // Set search timestamp cho cả 2 fields
        entity.setSearchTimestamp(LocalDateTime.now());
        
        return entity;
    }
    
    /**
     * Convert SearchHistory entity to SearchHistoryResponse
     */
    public SearchHistoryResponse toResponse(SearchHistory entity) {
        if (entity == null) {
            return null;
        }
        
        SearchHistoryResponse response = new SearchHistoryResponse();
        response.setId(entity.getId()); // Entity có getter tên getId() cho search_id field
        response.setUserId(entity.getUser() != null ? entity.getUser().getUserId() : null);
        response.setSearchKeyword(entity.getSearchKeyword()); // Sử dụng method ưu tiên search_query
        response.setSearchModule(entity.getSearchModule());
        response.setSearchType(entity.getSearchType());
        response.setResultCount(entity.getResultsCount()); // Sử dụng method ưu tiên results_count
        response.setFilters(entity.getFilters()); // Sử dụng method ưu tiên search_filters
        response.setExecutionTime(entity.getExecutionTimeMs());
        response.setSearchedAt(entity.getSearchTimestamp()); // Sử dụng method ưu tiên search_timestamp
        
        return response;
    }
    
    /**
     * Convert list of SearchHistory entities to list of SearchHistoryResponse
     */
    public List<SearchHistoryResponse> toResponseList(List<SearchHistory> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert SearchHistoryRequest to SearchHistory entity with custom search time
     */
    public SearchHistory toEntity(SearchHistoryRequest request, User user, LocalDateTime searchTime) {
        SearchHistory entity = toEntity(request, user);
        if (entity != null && searchTime != null) {
            entity.setSearchTime(searchTime);
        }
        return entity;
    }
    
    /**
     * Update existing SearchHistory entity with data from request
     */
    public void updateEntity(SearchHistory entity, SearchHistoryRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        entity.setKeyword(request.getCleanKeyword());
        entity.setSearchModule(request.getSearchModule());
        entity.setSearchType(request.getSearchType());
        entity.setResultCount(request.getResultCount());
        entity.setFilters(request.getFilters());
        entity.setExecutionTimeMs(request.getExecutionTime());
    }
    
    /**
     * Create SearchHistoryListResponse with pagination info
     */
    public SearchHistoryListResponse toListResponse(List<SearchHistory> entities, 
                                                   Long totalCount, Integer page, Integer pageSize) {
        List<SearchHistoryResponse> responses = toResponseList(entities);
        return new SearchHistoryListResponse(responses, totalCount, page, pageSize);
    }
    
    /**
     * Create simple SearchHistoryListResponse without pagination
     */
    public SearchHistoryListResponse toListResponse(List<SearchHistory> entities) {
        List<SearchHistoryResponse> responses = toResponseList(entities);
        return new SearchHistoryListResponse(responses);
    }
    
    /**
     * Create SearchHistoryResponse from projection data (for custom queries)
     */
    public SearchHistoryResponse toResponse(Long id, Long userId, String keyword, 
                                          String searchModule, String searchType,
                                          Integer resultCount, LocalDateTime searchTime) {
        SearchHistoryResponse response = new SearchHistoryResponse();
        response.setId(id);
        response.setUserId(userId);
        response.setSearchKeyword(keyword);
        
        // Convert string values back to enums
        try {
            response.setSearchModule(searchModule != null ? 
                com.example.userservice.search.entity.SearchModule.valueOf(searchModule) : null);
            response.setSearchType(searchType != null ? 
                com.example.userservice.search.entity.SearchType.valueOf(searchType) : null);
        } catch (IllegalArgumentException e) {
            // Handle invalid enum values gracefully
            response.setSearchModule(null);
            response.setSearchType(null);
        }
        
        response.setResultCount(resultCount);
        response.setSearchedAt(searchTime);
        
        return response;
    }
}
