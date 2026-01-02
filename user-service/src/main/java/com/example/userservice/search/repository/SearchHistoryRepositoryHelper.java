package com.example.userservice.search.repository;

import com.example.userservice.search.entity.SearchHistory;
import com.example.userservice.search.entity.SearchModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Helper class để test và demo các methods của SearchHistoryRepository
 */
@Component
public class SearchHistoryRepositoryHelper {

    private final SearchHistoryRepository searchHistoryRepository;

    public SearchHistoryRepositoryHelper(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    /**
     * Demo lấy lịch sử tìm kiếm của user
     */
    public Page<SearchHistory> getUserSearchHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return searchHistoryRepository.findByUserUserIdOrderBySearchTimestampDesc(userId, pageable);
    }

    /**
     * Demo lấy từ khóa phổ biến trong 7 ngày qua
     */
    public List<Object[]> getPopularKeywordsLast7Days(SearchModule module, int limit) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findPopularKeywordsByModule(module, fromDate, pageable);
    }

    /**
     * Demo lấy suggestions cho autocomplete
     */
    public List<String> getKeywordSuggestions(SearchModule module, String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findKeywordSuggestions(module, keyword, pageable);
    }

    /**
     * Demo thống kê tìm kiếm theo module
     */
    public List<Object[]> getModuleStatisticsLast30Days() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(30);
        return searchHistoryRepository.findModuleStatistics(fromDate);
    }

    /**
     * Demo cleanup old records (older than 90 days)
     */
    public int cleanupOldRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        return searchHistoryRepository.deleteOldRecords(cutoffDate);
    }

    /**
     * Demo trending analysis
     */
    public List<Object[]> getTrendingKeywords(SearchModule module, int days, int limit) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findTrendingKeywords(module, fromDate, 2, pageable);
    }

    /**
     * Demo user search patterns
     */
    public List<Object[]> getUserSearchPatterns(Long userId, int daysBack) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(daysBack);
        return searchHistoryRepository.findUserSearchStatistics(userId, fromDate);
    }

    /**
     * Demo advanced filters search
     */
    public List<SearchHistory> searchWithAdvancedFilters(
            SearchModule module,
            String keyword,
            String category,
            int daysBack,
            int limit) {
        
        LocalDateTime fromDate = daysBack > 0 ? LocalDateTime.now().minusDays(daysBack) : LocalDateTime.now().minusDays(30);
        
        return searchHistoryRepository.findByAdvancedFilters(
                module.name(), keyword, category, fromDate, limit);
    }

    /**
     * Demo count user searches
     */
    public long getUserSearchCount(Long userId) {
        return searchHistoryRepository.countByUserUserId(userId);
    }

    /**
     * Demo check if user searched for keyword
     */
    public boolean hasUserSearchedKeyword(Long userId, String keyword) {
        return searchHistoryRepository.existsByUserUserIdAndSearchQueryIgnoreCase(userId, keyword);
    }

    /**
     * Demo get zero result searches (để improve content)
     */
    public List<SearchHistory> getZeroResultSearches(SearchModule module, int daysBack, int limit) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(daysBack);
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findZeroResultSearches(module, fromDate, pageable);
    }

    /**
     * Demo get high result searches (successful patterns)
     */
    public List<SearchHistory> getHighResultSearches(SearchModule module, int daysBack, int limit) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(daysBack);
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findHighResultSearches(module, fromDate, pageable);
    }
}
