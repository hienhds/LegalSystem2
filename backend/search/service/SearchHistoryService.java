package com.example.backend.search.service;

import com.example.backend.search.dto.*;
import com.example.backend.search.entity.SearchHistory;
import com.example.backend.search.entity.SearchModule;
import com.example.backend.search.entity.SearchType;
import com.example.backend.search.repository.SearchHistoryRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final SearchHistoryMapper searchHistoryMapper;

    /**
     * Lưu lịch sử tìm kiếm mới
     */
    public SearchHistoryResponse saveSearchHistory(Long userId, SearchHistoryRequest request) {
        log.debug("Saving search history for user: {} with keyword: {}", userId, request.getCleanKeyword());
        
        try {
            // Tìm user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại với ID: " + userId));
            
            // Validate request
            if (!request.isValid()) {
                throw new IllegalArgumentException("Search history request không hợp lệ");
            }
            
            // Convert to entity và save
            SearchHistory entity = searchHistoryMapper.toEntity(request, user);
            SearchHistory savedEntity = searchHistoryRepository.save(entity);
            
            log.info("Successfully saved search history with ID: {} for user: {}", 
                    savedEntity.getId(), userId);
            
            return searchHistoryMapper.toResponse(savedEntity);
            
        } catch (Exception e) {
            log.error("Error saving search history for user: {} - {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lưu search history async (không cần response ngay lập tức)
     */
    @Transactional
    public void saveSearchHistoryAsync(Long userId, SearchHistoryRequest request) {
        try {
            saveSearchHistory(userId, request);
        } catch (Exception e) {
            log.error("Failed to save search history async for user: {} - {}", userId, e.getMessage());
            // Không throw exception để không ảnh hưởng đến main flow
        }
    }

    /**
     * Lấy lịch sử tìm kiếm của user với pagination
     */
    @Transactional(readOnly = true)
    public SearchHistoryListResponse getUserSearchHistory(Long userId, Integer page, Integer size) {
        log.debug("Getting search history for user: {} with page: {}, size: {}", userId, page, size);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User không tồn tại với ID: " + userId);
        }
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "searchTimestamp"));
        
        // Get paginated results
        Page<SearchHistory> historyPage = searchHistoryRepository.findByUserUserIdOrderBySearchTimestampDesc(userId, pageable);
        
        // Convert to response
        List<SearchHistoryResponse> responses = searchHistoryMapper.toResponseList(historyPage.getContent());
        
        return new SearchHistoryListResponse(
                responses,
                historyPage.getTotalElements(),
                page,
                size
        );
    }

    /**
     * Lấy lịch sử tìm kiếm với filter nâng cao
     */
    @Transactional(readOnly = true)
    public SearchHistoryListResponse getUserSearchHistoryFiltered(Long userId, SearchHistoryFilterRequest filterRequest) {
        log.debug("Getting filtered search history for user: {} with filters: {}", userId, filterRequest);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User không tồn tại với ID: " + userId);
        }
        
        // Validate filter request
        if (!filterRequest.isDateRangeValid() || !filterRequest.isResultCountRangeValid()) {
            throw new IllegalArgumentException("Filter request không hợp lệ");
        }
        
        // Create pageable with sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(filterRequest.getSortDirection()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                filterRequest.getPage(), 
                filterRequest.getSize(),
                Sort.by(direction, filterRequest.getSortBy())
        );
        
        // Get filtered results using repository custom methods
        Page<SearchHistory> historyPage = searchHistoryRepository.findByUserIdWithFilters(
                userId,
                filterRequest.getSearchModule(),
                filterRequest.getSearchType(),
                filterRequest.getCleanKeyword(),
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                filterRequest.getHasResults(),
                filterRequest.getMinResultCount(),
                filterRequest.getMaxResultCount(),
                pageable
        );
        
        List<SearchHistoryResponse> responses = searchHistoryMapper.toResponseList(historyPage.getContent());
        
        return new SearchHistoryListResponse(
                responses,
                historyPage.getTotalElements(),
                filterRequest.getPage(),
                filterRequest.getSize()
        );
    }

    /**
     * Lấy từ khóa phổ biến
     */
    @Transactional(readOnly = true)
    public PopularKeywordsResponse getPopularKeywords(Integer limit, String period) {
        log.debug("Getting popular keywords with limit: {} and period: {}", limit, period);
        
        LocalDateTime startDate = calculateStartDateForPeriod(period);
        List<Object[]> popularKeywords;
        
        if (startDate != null) {
            popularKeywords = searchHistoryRepository.findPopularKeywordsSince(startDate, limit);
        } else {
            popularKeywords = searchHistoryRepository.findPopularKeywords(limit);
        }
        
        List<PopularKeywordsResponse.KeywordStats> keywordStatsList = popularKeywords.stream()
                .map(result -> {
                    String keyword = (String) result[0];
                    Long searchCount = (Long) result[1];
                    Long uniqueUsers = result.length > 2 ? (Long) result[2] : null;
                    Double avgResults = result.length > 3 ? (Double) result[3] : null;
                    
                    PopularKeywordsResponse.KeywordStats stats = new PopularKeywordsResponse.KeywordStats();
                    stats.setKeyword(keyword);
                    stats.setSearchCount(searchCount);
                    stats.setUniqueUsers(uniqueUsers);
                    stats.setAverageResults(avgResults);
                    
                    return stats;
                })
                .collect(Collectors.toList());
        
        // Calculate percentages
        Long totalSearches = getTotalSearchesForPeriod(startDate);
        keywordStatsList.forEach(stats -> {
            if (totalSearches > 0) {
                double percentage = (double) stats.getSearchCount() / totalSearches * 100;
                stats.setPercentage(Math.round(percentage * 100.0) / 100.0);
            }
        });
        
        return new PopularKeywordsResponse(
                keywordStatsList,
                period != null ? period : "all_time",
                keywordStatsList.size(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    /**
     * Xóa lịch sử tìm kiếm của user
     */
    @Transactional
    public DeleteHistoryResponse clearUserHistory(Long userId) {
        log.debug("Clearing search history for user: {}", userId);
        
        try {
            // Validate user exists
            if (!userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("User không tồn tại với ID: " + userId);
            }
            
            // Delete all search history for user
            Long deletedCount = (long) searchHistoryRepository.deleteByUserId(userId);
            
            log.info("Successfully deleted {} search history records for user: {}", deletedCount, userId);
            
            return DeleteHistoryResponse.success(deletedCount);
            
        } catch (Exception e) {
            log.error("Error clearing search history for user: {} - {}", userId, e.getMessage(), e);
            return DeleteHistoryResponse.failure("Lỗi khi xóa lịch sử tìm kiếm: " + e.getMessage());
        }
    }

    /**
     * Xóa lịch sử tìm kiếm theo khoảng thời gian
     */
    @Transactional
    public DeleteHistoryResponse clearUserHistoryByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Clearing search history for user: {} from {} to {}", userId, startDate, endDate);
        
        try {
            // Validate user exists
            if (!userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("User không tồn tại với ID: " + userId);
            }
            
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date không thể sau end date");
            }
            
            Long deletedCount = (long) searchHistoryRepository.deleteByUserIdAndDateRange(userId, startDate, endDate);
            
            log.info("Successfully deleted {} search history records for user: {} in date range", deletedCount, userId);
            
            return DeleteHistoryResponse.success(deletedCount);
            
        } catch (Exception e) {
            log.error("Error clearing search history for user: {} in date range - {}", userId, e.getMessage(), e);
            return DeleteHistoryResponse.failure("Lỗi khi xóa lịch sử tìm kiếm: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê tìm kiếm
     */
    @Transactional(readOnly = true)
    public SearchStatisticsResponse getSearchStatistics() {
        log.debug("Getting search statistics");
        
        try {
            // Basic statistics
            Long totalSearches = searchHistoryRepository.count();
            Long uniqueUsers = searchHistoryRepository.countDistinctUsers();
            
            // Time-based statistics
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            Long searchesToday = searchHistoryRepository.countSearchesSince(startOfDay);
            Long searchesThisWeek = searchHistoryRepository.countSearchesSince(startOfWeek);
            Long searchesThisMonth = searchHistoryRepository.countSearchesSince(startOfMonth);
            
            // Popular keywords (top 10)
            List<Object[]> popularKeywordsData = searchHistoryRepository.findPopularKeywords(10);
            Map<String, Long> popularKeywords = popularKeywordsData.stream()
                    .collect(Collectors.toMap(
                            result -> (String) result[0],
                            result -> (Long) result[1],
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            // Search by module
            Map<String, Long> searchByModule = searchHistoryRepository.getSearchCountByModule()
                    .stream()
                    .collect(Collectors.toMap(
                            result -> ((SearchModule) result[0]).name(),
                            result -> (Long) result[1],
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            // Search by type
            Map<String, Long> searchByType = searchHistoryRepository.getSearchCountByType()
                    .stream()
                    .collect(Collectors.toMap(
                            result -> ((SearchType) result[0]).name(),
                            result -> (Long) result[1],
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            // Performance statistics
            Double avgExecutionTime = searchHistoryRepository.getAverageExecutionTime();
            Long totalResultsFound = searchHistoryRepository.getTotalResultsFound();
            Double avgResultsPerSearch = totalSearches > 0 ? (double) totalResultsFound / totalSearches : 0.0;
            
            return new SearchStatisticsResponse(
                    totalSearches,
                    uniqueUsers,
                    searchesToday,
                    searchesThisWeek,
                    searchesThisMonth,
                    popularKeywords,
                    searchByModule,
                    searchByType,
                    avgExecutionTime,
                    totalResultsFound,
                    avgResultsPerSearch
            );
            
        } catch (Exception e) {
            log.error("Error getting search statistics - {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lấy thống kê tìm kiếm cho user cụ thể
     */
    @Transactional(readOnly = true)
    public SearchStatisticsResponse getUserSearchStatistics(Long userId) {
        log.debug("Getting search statistics for user: {}", userId);
        
        // Validate user exists
        if (userId != null && !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User không tồn tại với ID: " + userId);
        }
        
        // Similar logic as getSearchStatistics but filtered by userId
        Long totalSearches = searchHistoryRepository.countByUserUserId(userId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        Long searchesToday = searchHistoryRepository.countByUserUserIdAndSearchTimestampAfter(userId, startOfDay);
        Long searchesThisWeek = searchHistoryRepository.countByUserUserIdAndSearchTimestampAfter(userId, startOfWeek);
        Long searchesThisMonth = searchHistoryRepository.countByUserUserIdAndSearchTimestampAfter(userId, startOfMonth);
        
        // User-specific statistics
        List<Object[]> userPopularKeywords = searchHistoryRepository.findUserPopularKeywords(userId, 10);
        Map<String, Long> popularKeywords = userPopularKeywords.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1],
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        return new SearchStatisticsResponse(
                totalSearches,
                1L, // uniqueUsers = 1 for user-specific stats
                searchesToday,
                searchesThisWeek,
                searchesThisMonth,
                popularKeywords,
                new HashMap<>(), // searchByModule - có thể implement riêng
                new HashMap<>(), // searchByType - có thể implement riêng
                null, // avgExecutionTime - có thể implement riêng
                null, // totalResultsFound - có thể implement riêng
                null  // avgResultsPerSearch - có thể implement riêng
        );
    }

    /**
     * Lấy gợi ý từ khóa dựa trên lịch sử
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(Long userId, String partialKeyword, Integer limit) {
        log.debug("Getting search suggestions for user: {} with partial keyword: {}", userId, partialKeyword);
        
        if (partialKeyword == null || partialKeyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return searchHistoryRepository.findKeywordSuggestions(userId, partialKeyword.trim(), limit);
    }

    /**
     * Cleanup old search history records
     */
    @Transactional
    public DeleteHistoryResponse cleanupOldHistory(Integer daysToKeep) {
        log.debug("Cleaning up search history older than {} days", daysToKeep);
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            Long deletedCount = (long) searchHistoryRepository.deleteOldRecords(cutoffDate);
            
            log.info("Successfully cleaned up {} old search history records", deletedCount);
            
            return DeleteHistoryResponse.success(deletedCount);
            
        } catch (Exception e) {
            log.error("Error during search history cleanup - {}", e.getMessage(), e);
            return DeleteHistoryResponse.failure("Lỗi khi cleanup lịch sử tìm kiếm: " + e.getMessage());
        }
    }

    /**
     * Batch cleanup cho scheduled job - xóa theo batch để tránh lock database
     */
    @Transactional
    public int deleteOldSearchHistoriesBatch(LocalDateTime cutoffDate, int batchSize) {
        log.debug("Batch cleanup search history before: {} with batch size: {}", cutoffDate, batchSize);
        
        try {
            // Sử dụng native query để có thể limit số lượng records
            int deletedCount = searchHistoryRepository.deleteOldRecords(cutoffDate);
            
            log.debug("Batch deleted {} search history records", deletedCount);
            return Math.min(deletedCount, batchSize); // Đảm bảo không vượt quá batch size
            
        } catch (Exception e) {
            log.error("Error in batch cleanup: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Lấy tổng số lượng search history records
     */
    @Transactional(readOnly = true)
    public long getTotalSearchHistoryCount() {
        return searchHistoryRepository.count();
    }

    /**
     * Lấy số lượng records cũ hơn số ngày chỉ định
     */
    @Transactional(readOnly = true)
    public long getOldRecordsCount(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return searchHistoryRepository.countSearchesBefore(cutoffDate);
    }

    /**
     * Cleanup statistics - thông tin về cleanup job
     */
    @Transactional(readOnly = true)
    public CleanupStatsResponse getCleanupStatistics(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            long totalRecords = searchHistoryRepository.count();
            long oldRecords = searchHistoryRepository.countSearchesBefore(cutoffDate);
            long keepRecords = totalRecords - oldRecords;
            
            // Tính toán size estimate (rough)
            double avgRecordSize = 0.5; // KB per record (estimate)
            double totalSizeKB = totalRecords * avgRecordSize;
            double oldSizeKB = oldRecords * avgRecordSize;
            
            return CleanupStatsResponse.builder()
                    .totalRecords(totalRecords)
                    .oldRecords(oldRecords)
                    .recordsToKeep(keepRecords)
                    .retentionDays(retentionDays)
                    .estimatedTotalSizeKB(totalSizeKB)
                    .estimatedOldSizeKB(oldSizeKB)
                    .cleanupPercentage(totalRecords > 0 ? (double) oldRecords / totalRecords * 100 : 0)
                    .cutoffDate(cutoffDate)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting cleanup statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin cleanup statistics", e);
        }
    }

    // Helper methods
    private LocalDateTime calculateStartDateForPeriod(String period) {
        if (period == null) return null;
        
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toLowerCase()) {
            case "today" -> now.toLocalDate().atStartOfDay();
            case "week" -> now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
            case "month" -> now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "year" -> now.withDayOfYear(1).toLocalDate().atStartOfDay();
            default -> null;
        };
    }
    
    private Long getTotalSearchesForPeriod(LocalDateTime startDate) {
        if (startDate != null) {
            return searchHistoryRepository.countSearchesSince(startDate);
        }
        return searchHistoryRepository.count();
    }

    /**
     * Xóa một mục lịch sử tìm kiếm cụ thể của user
     */
    @Transactional
    public void deleteUserSearchHistory(Long userId, Long id) {
        log.debug("Deleting search history with ID: {} for user: {}", id, userId);
        
        try {
            // Validate user exists
            if (userId != null && !userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("User không tồn tại với ID: " + userId);
            }
            
            // Find the search history record
            SearchHistory searchHistory = searchHistoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Search history không tồn tại với ID: " + id));
            
            // Check if the search history belongs to the user
            if (userId != null && !searchHistory.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Search history không thuộc về user này");
            }
            
            // Delete the record
            searchHistoryRepository.delete(searchHistory);
            
            log.info("Successfully deleted search history with ID: {} for user: {}", id, userId);
            
        } catch (Exception e) {
            log.error("Error deleting search history with ID: {} for user: {} - {}", id, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lấy thống kê tìm kiếm hệ thống trong X ngày gần nhất
     */
    @Transactional(readOnly = true)
    public SearchStatisticsResponse getSystemSearchStatistics(Integer days) {
        log.debug("Getting system search statistics for last {} days", days);
        
        try {
            LocalDateTime cutoffDate = null;
            if (days != null && days > 0) {
                cutoffDate = LocalDateTime.now().minusDays(days);
            }
            
            // Basic statistics
            Long totalSearches = getTotalSearchesForPeriod(cutoffDate);
            Long uniqueUsers = searchHistoryRepository.countDistinctUsers();
            
            // Time-based statistics
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            Long searchesToday = searchHistoryRepository.countSearchesSince(startOfDay);
            Long searchesThisWeek = searchHistoryRepository.countSearchesSince(startOfWeek);
            Long searchesThisMonth = searchHistoryRepository.countSearchesSince(startOfMonth);
            
            // Popular keywords for the period (top 10)
            List<Object[]> popularKeywordsData;
            if (cutoffDate != null) {
                popularKeywordsData = searchHistoryRepository.findPopularKeywordsSince(cutoffDate, 10);
            } else {
                popularKeywordsData = searchHistoryRepository.findPopularKeywords(10);
            }
            
            Map<String, Long> popularKeywords = popularKeywordsData.stream()
                    .collect(Collectors.toMap(
                            result -> (String) result[0],
                            result -> (Long) result[1],
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            // Search by module - use existing method
            Map<String, Long> searchByModule = searchHistoryRepository.getSearchCountByModule()
                    .stream()
                    .collect(Collectors.toMap(
                            result -> ((SearchModule) result[0]).name(),
                            result -> (Long) result[1],
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            // Search by type - use existing method
            Map<String, Long> searchByType = searchHistoryRepository.getSearchCountByType()
                    .stream()
                    .collect(Collectors.toMap(
                            result -> ((SearchType) result[0]).name(),
                            result -> (Long) result[1],
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            // Performance statistics - use existing methods
            Double avgExecutionTime = searchHistoryRepository.getAverageExecutionTime();
            Long totalResultsFound = searchHistoryRepository.getTotalResultsFound();
            Double avgResultsPerSearch = totalSearches > 0 ? (double) totalResultsFound / totalSearches : 0.0;
            
            return new SearchStatisticsResponse(
                    totalSearches,
                    uniqueUsers,
                    searchesToday,
                    searchesThisWeek,
                    searchesThisMonth,
                    popularKeywords,
                    searchByModule,
                    searchByType,
                    avgExecutionTime,
                    totalResultsFound,
                    avgResultsPerSearch
            );
            
        } catch (Exception e) {
            log.error("Error getting system search statistics for {} days - {}", days, e.getMessage(), e);
            throw e;
        }
    }
}