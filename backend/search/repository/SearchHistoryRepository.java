package com.example.backend.search.repository;

import com.example.backend.search.entity.SearchHistory;
import com.example.backend.search.entity.SearchModule;
import com.example.backend.search.entity.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface cho SearchHistory entity
 * Cung cấp các methods để truy vấn và thống kê lịch sử tìm kiếm
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long>, SearchHistoryRepositoryCustom {

    // ==================== Basic Queries ====================

    /**
     * Lấy lịch sử tìm kiếm của user theo thời gian giảm dần
     */
    Page<SearchHistory> findByUserUserIdOrderBySearchTimestampDesc(Long userId, Pageable pageable);

    /**
     * Lấy lịch sử tìm kiếm của user theo module và thời gian giảm dần
     */
    Page<SearchHistory> findByUserUserIdAndSearchModuleOrderBySearchTimestampDesc(
            Long userId, SearchModule searchModule, Pageable pageable);

    /**
     * Lấy lịch sử tìm kiếm của user theo search type
     */
    Page<SearchHistory> findByUserUserIdAndSearchTypeOrderBySearchTimestampDesc(
            Long userId, SearchType searchType, Pageable pageable);

    /**
     * Tìm kiếm theo keyword chứa chuỗi con (case insensitive)
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user.userId = :userId " +
           "AND LOWER(sh.searchQuery) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY sh.searchTimestamp DESC")
    Page<SearchHistory> findByUserIdAndKeywordContaining(
            @Param("userId") Long userId, 
            @Param("keyword") String keyword, 
            Pageable pageable);

    /**
     * Lấy lịch sử tìm kiếm theo module và category
     */
    Page<SearchHistory> findBySearchModuleAndCategoryOrderBySearchTimestampDesc(
            SearchModule searchModule, String category, Pageable pageable);

    // ==================== Statistics Queries ====================

    /**
     * Lấy từ khóa phổ biến theo module trong khoảng thời gian
     */
    @Query("SELECT sh.searchQuery, COUNT(sh) as searchCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND sh.searchQuery IS NOT NULL " +
           "AND sh.searchQuery <> '' " +
           "AND sh.searchTimestamp >= :fromDate " +
           "GROUP BY sh.searchQuery " +
           "ORDER BY searchCount DESC")
    List<Object[]> findPopularKeywordsByModule(
            @Param("module") SearchModule module,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable);

    /**
     * Lấy từ khóa phổ biến tổng thể trong khoảng thời gian
     */
    @Query("SELECT sh.searchQuery, COUNT(sh) as searchCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchQuery IS NOT NULL " +
           "AND sh.searchQuery <> '' " +
           "AND sh.searchTimestamp >= :fromDate " +
           "GROUP BY sh.searchQuery " +
           "ORDER BY searchCount DESC")
    List<Object[]> findPopularKeywords(
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable);

    /**
     * Thống kê tìm kiếm theo category trong một module
     */
    @Query("SELECT sh.category, COUNT(sh) as categoryCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND sh.category IS NOT NULL " +
           "AND sh.category <> '' " +
           "AND sh.searchTimestamp >= :fromDate " +
           "GROUP BY sh.category " +
           "ORDER BY categoryCount DESC")
    List<Object[]> findCategoryStatsByModule(
            @Param("module") SearchModule module,
            @Param("fromDate") LocalDateTime fromDate);

    /**
     * Thống kê tìm kiếm theo module trong khoảng thời gian
     */
    @Query("SELECT sh.searchModule, COUNT(sh) as totalSearches, " +
           "COUNT(DISTINCT sh.user.userId) as uniqueUsers, " +
           "AVG(sh.resultsCount) as avgResults " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchTimestamp >= :fromDate " +
           "GROUP BY sh.searchModule " +
           "ORDER BY totalSearches DESC")
    List<Object[]> findModuleStatistics(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Thống kê tìm kiếm của user cụ thể
     */
    @Query("SELECT sh.searchModule, COUNT(sh) as searchCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.user.userId = :userId " +
           "AND sh.searchTimestamp >= :fromDate " +
           "GROUP BY sh.searchModule " +
           "ORDER BY searchCount DESC")
    List<Object[]> findUserSearchStatistics(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate);

    // ==================== Suggestions & Autocomplete ====================

    /**
     * Tìm từ khóa tương tự cho autocomplete
     */
    @Query("SELECT DISTINCT sh.searchQuery " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND LOWER(sh.searchQuery) LIKE LOWER(CONCAT(:keyword, '%')) " +
           "AND sh.searchQuery IS NOT NULL " +
           "AND sh.searchQuery <> '' " +
           "ORDER BY sh.searchQuery ASC")
    List<String> findKeywordSuggestions(
            @Param("module") SearchModule module,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Tìm categories được sử dụng trong module
     */
    @Query("SELECT DISTINCT sh.category " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND sh.category IS NOT NULL " +
           "AND sh.category <> '' " +
           "ORDER BY sh.category ASC")
    List<String> findCategoriesByModule(@Param("module") SearchModule module);

    // ==================== Count & Existence Queries ====================

    /**
     * Đếm số lượng tìm kiếm của user
     */
    long countByUserUserId(Long userId);

    /**
     * Đếm số lượng tìm kiếm của user trong khoảng thời gian
     */
    long countByUserUserIdAndSearchTimestampBetween(
            Long userId, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Đếm số lượng tìm kiếm theo module
     */
    long countBySearchModule(SearchModule module);

    /**
     * Kiểm tra user đã tìm kiếm keyword này chưa
     */
    boolean existsByUserUserIdAndSearchQueryIgnoreCase(Long userId, String searchQuery);

    // ==================== Date Range Queries ====================

    /**
     * Lấy lịch sử tìm kiếm trong khoảng thời gian
     */
    Page<SearchHistory> findByUserUserIdAndSearchTimestampBetweenOrderBySearchTimestampDesc(
            Long userId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    /**
     * Lấy lịch sử tìm kiếm theo module trong khoảng thời gian
     */
    Page<SearchHistory> findBySearchModuleAndSearchTimestampBetweenOrderBySearchTimestampDesc(
            SearchModule module, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // ==================== Trending & Analytics ====================

    /**
     * Lấy những từ khóa trending (tăng trưởng nhanh gần đây)
     */
    @Query("SELECT sh.searchQuery, COUNT(sh) as recentCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND sh.searchQuery IS NOT NULL " +
           "AND sh.searchQuery <> '' " +
           "AND sh.searchTimestamp >= :fromDate " +
           "GROUP BY sh.searchQuery " +
           "HAVING COUNT(sh) >= :minCount " +
           "ORDER BY recentCount DESC")
    List<Object[]> findTrendingKeywords(
            @Param("module") SearchModule module,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("minCount") long minCount,
            Pageable pageable);

    /**
     * Lấy searches có nhiều kết quả nhất
     */
    @Query("SELECT sh FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND sh.resultsCount > 0 " +
           "AND sh.searchTimestamp >= :fromDate " +
           "ORDER BY sh.resultsCount DESC, sh.searchTimestamp DESC")
    List<SearchHistory> findHighResultSearches(
            @Param("module") SearchModule module,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable);

    /**
     * Lấy searches không có kết quả để phân tích
     */
    @Query("SELECT sh FROM SearchHistory sh " +
           "WHERE sh.searchModule = :module " +
           "AND sh.resultsCount = 0 " +
           "AND sh.searchTimestamp >= :fromDate " +
           "ORDER BY sh.searchTimestamp DESC")
    List<SearchHistory> findZeroResultSearches(
            @Param("module") SearchModule module,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable);

    // ==================== Cleanup Operations ====================

    /**
     * Xóa lịch sử tìm kiếm cũ hơn số ngày chỉ định
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.searchTimestamp < :cutoffDate")
    int deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Xóa lịch sử tìm kiếm của user cụ thể
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.user.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Xóa lịch sử tìm kiếm của user trong khoảng thời gian
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh " +
           "WHERE sh.user.userId = :userId " +
           "AND sh.searchTimestamp BETWEEN :fromDate AND :toDate")
    int deleteByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    // ==================== Advanced Filters ====================

    /**
     * Tìm kiếm với filters JSON (sử dụng native query)
     */
    @Query(value = "SELECT * FROM search_histories sh " +
                  "WHERE sh.search_module = :module " +
                  "AND (:keyword IS NULL OR sh.search_query LIKE CONCAT('%', :keyword, '%')) " +
                  "AND (:category IS NULL OR sh.category = :category) " +
                  "AND sh.search_timestamp >= :fromDate " +
                  "ORDER BY sh.search_timestamp DESC " +
                  "LIMIT :limitCount", nativeQuery = true)
    List<SearchHistory> findByAdvancedFilters(
            @Param("module") String module,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("limitCount") int limitCount);

    // ==================== Additional Methods for Service ====================
    
    /**
     * Count distinct users
     */
    @Query("SELECT COUNT(DISTINCT sh.user.userId) FROM SearchHistory sh")
    Long countDistinctUsers();
    
    /**
     * Count searches since a date
     */
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.searchTimestamp >= :since")
    Long countSearchesSince(@Param("since") LocalDateTime since);
    
    /**
     * Count searches before a date (for cleanup)
     */
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.searchTimestamp < :before")
    Long countSearchesBefore(@Param("before") LocalDateTime before);
    
    /**
     * Find popular keywords with limit
     */
    @Query(value = "SELECT sh.search_query, COUNT(*) as searchCount " +
           "FROM search_histories sh " +
           "WHERE sh.search_query IS NOT NULL " +
           "AND sh.search_query <> '' " +
           "GROUP BY sh.search_query " +
           "ORDER BY searchCount DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findPopularKeywords(@Param("limit") Integer limit);
    
    /**
     * Find popular keywords since date with limit
     */
    @Query(value = "SELECT sh.search_query, COUNT(*) as searchCount " +
           "FROM search_histories sh " +
           "WHERE sh.search_query IS NOT NULL " +
           "AND sh.search_query <> '' " +
           "AND sh.search_timestamp >= :since " +
           "GROUP BY sh.search_query " +
           "ORDER BY searchCount DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findPopularKeywordsSince(@Param("since") LocalDateTime since, @Param("limit") Integer limit);
    
    /**
     * Get search count by module
     */
    @Query("SELECT sh.searchModule, COUNT(sh) FROM SearchHistory sh GROUP BY sh.searchModule")
    List<Object[]> getSearchCountByModule();
    
    /**
     * Get search count by type
     */
    @Query("SELECT sh.searchType, COUNT(sh) FROM SearchHistory sh GROUP BY sh.searchType")
    List<Object[]> getSearchCountByType();
    
    /**
     * Get average execution time
     */
    @Query("SELECT AVG(sh.resultsCount) FROM SearchHistory sh WHERE sh.resultsCount IS NOT NULL")
    Double getAverageExecutionTime();
    
    /**
     * Get total results found
     */
    @Query("SELECT SUM(sh.resultsCount) FROM SearchHistory sh WHERE sh.resultsCount IS NOT NULL")
    Long getTotalResultsFound();
    

    
    /**
     * Count by user ID and created at after
     */
    Long countByUserUserIdAndSearchTimestampAfter(Long userId, LocalDateTime after);
    
    /**
     * Find user popular keywords
     */
    @Query(value = "SELECT sh.search_query, COUNT(*) as searchCount " +
           "FROM search_histories sh " +
           "WHERE sh.user_id = :userId " +
           "AND sh.search_query IS NOT NULL " +
           "AND sh.search_query <> '' " +
           "GROUP BY sh.search_query " +
           "ORDER BY searchCount DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findUserPopularKeywords(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * Find keyword suggestions for user
     */
    @Query(value = "SELECT DISTINCT sh.search_query " +
           "FROM search_histories sh " +
           "WHERE sh.user_id = :userId " +
           "AND LOWER(sh.search_query) LIKE LOWER(CONCAT('%', :partialKeyword, '%')) " +
           "AND sh.search_query IS NOT NULL " +
           "AND sh.search_query <> '' " +
           "ORDER BY sh.search_query ASC " +
           "LIMIT :limit", nativeQuery = true)
    List<String> findKeywordSuggestions(@Param("userId") Long userId, 
                                       @Param("partialKeyword") String partialKeyword, 
                                       @Param("limit") Integer limit);
}