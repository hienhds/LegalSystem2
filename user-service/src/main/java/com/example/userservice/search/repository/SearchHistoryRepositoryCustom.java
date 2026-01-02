package com.example.userservice.search.repository;

import com.example.userservice.search.entity.SearchHistory;
import com.example.userservice.search.entity.SearchModule;
import com.example.userservice.search.entity.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Custom repository interface cho các queries phức tạp của SearchHistory
 */
public interface SearchHistoryRepositoryCustom {

    /**
     * Tìm kiếm với filters động
     */
    Page<SearchHistory> findByDynamicFilters(
            Long userId,
            SearchModule searchModule,
            String keyword,
            String category,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String filtersJson,
            Pageable pageable
    );
    
    /**
     * Tìm kiếm theo user với filters chi tiết
     */
    Page<SearchHistory> findByUserIdWithFilters(
            Long userId,
            SearchModule searchModule,
            SearchType searchType,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean hasResults,
            Integer minResultCount,
            Integer maxResultCount,
            Pageable pageable
    );

    /**
     * Thống kê nâng cao theo khoảng thời gian
     */
    Map<String, Object> getAdvancedStatistics(
            SearchModule searchModule,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    /**
     * Lấy patterns tìm kiếm của user
     */
    List<Map<String, Object>> getUserSearchPatterns(
            Long userId,
            LocalDateTime fromDate,
            int limit
    );

    /**
     * Search suggestion dựa trên popularity và relevance
     */
    List<String> getSmartSuggestions(
            SearchModule searchModule,
            String partialKeyword,
            Long userId,
            int limit
    );

    /**
     * Trending analysis với growth rate
     */
    List<Map<String, Object>> getTrendingAnalysis(
            SearchModule searchModule,
            int daysPeriod,
            int limit
    );
}
