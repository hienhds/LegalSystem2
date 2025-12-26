package com.example.backend.search.repository;

import com.example.backend.search.entity.SearchHistory;
import com.example.backend.search.entity.SearchModule;
import com.example.backend.search.entity.SearchType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation của SearchHistoryRepositoryCustom
 * Chứa các custom queries phức tạp cho SearchHistory
 */
@Repository
public class SearchHistoryRepositoryImpl implements SearchHistoryRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<SearchHistory> findByDynamicFilters(
            Long userId,
            SearchModule searchModule,
            String keyword,
            String category,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String filtersJson,
            Pageable pageable) {

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT sh FROM SearchHistory sh WHERE 1=1 ");
        
        Map<String, Object> parameters = new HashMap<>();
        
        if (userId != null) {
            jpql.append("AND sh.user.userId = :userId ");
            parameters.put("userId", userId);
        }
        
        if (searchModule != null) {
            jpql.append("AND sh.searchModule = :searchModule ");
            parameters.put("searchModule", searchModule);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append("AND LOWER(sh.keyword) LIKE LOWER(CONCAT('%', :keyword, '%')) ");
            parameters.put("keyword", keyword.trim());
        }
        
        if (category != null && !category.trim().isEmpty()) {
            jpql.append("AND sh.category = :category ");
            parameters.put("category", category.trim());
        }
        
        if (fromDate != null) {
            jpql.append("AND sh.createdAt >= :fromDate ");
            parameters.put("fromDate", fromDate);
        }
        
        if (toDate != null) {
            jpql.append("AND sh.createdAt <= :toDate ");
            parameters.put("toDate", toDate);
        }
        
        jpql.append("ORDER BY sh.createdAt DESC");
        
        Query query = entityManager.createQuery(jpql.toString(), SearchHistory.class);
        Query countQuery = entityManager.createQuery(
            jpql.toString().replace("SELECT sh", "SELECT COUNT(sh)").replace("ORDER BY sh.createdAt DESC", "")
        );
        
        // Set parameters
        parameters.forEach(query::setParameter);
        parameters.forEach(countQuery::setParameter);
        
        // Pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        @SuppressWarnings("unchecked")
        List<SearchHistory> results = query.getResultList();
        Long total = (Long) countQuery.getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<SearchHistory> findByUserIdWithFilters(
            Long userId,
            SearchModule searchModule,
            SearchType searchType,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean hasResults,
            Integer minResultCount,
            Integer maxResultCount,
            Pageable pageable) {

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT sh FROM SearchHistory sh WHERE sh.user.userId = :userId ");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", userId);
        
        if (searchModule != null) {
            jpql.append("AND sh.searchModule = :searchModule ");
            parameters.put("searchModule", searchModule);
        }
        
        if (searchType != null) {
            jpql.append("AND sh.searchType = :searchType ");
            parameters.put("searchType", searchType);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append("AND LOWER(sh.keyword) LIKE LOWER(CONCAT('%', :keyword, '%')) ");
            parameters.put("keyword", keyword.trim());
        }
        
        if (startDate != null) {
            jpql.append("AND sh.createdAt >= :startDate ");
            parameters.put("startDate", startDate);
        }
        
        if (endDate != null) {
            jpql.append("AND sh.createdAt <= :endDate ");
            parameters.put("endDate", endDate);
        }
        
        if (hasResults != null) {
            if (hasResults) {
                jpql.append("AND sh.resultCount > 0 ");
            } else {
                jpql.append("AND sh.resultCount = 0 ");
            }
        }
        
        if (minResultCount != null) {
            jpql.append("AND sh.resultCount >= :minResultCount ");
            parameters.put("minResultCount", minResultCount);
        }
        
        if (maxResultCount != null) {
            jpql.append("AND sh.resultCount <= :maxResultCount ");
            parameters.put("maxResultCount", maxResultCount);
        }
        
        jpql.append("ORDER BY sh.createdAt DESC");
        
        Query query = entityManager.createQuery(jpql.toString(), SearchHistory.class);
        Query countQuery = entityManager.createQuery(
            jpql.toString().replace("SELECT sh", "SELECT COUNT(sh)").replace("ORDER BY sh.createdAt DESC", "")
        );
        
        // Set parameters
        parameters.forEach(query::setParameter);
        parameters.forEach(countQuery::setParameter);
        
        // Pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        @SuppressWarnings("unchecked")
        List<SearchHistory> results = query.getResultList();
        Long total = (Long) countQuery.getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Map<String, Object> getAdvancedStatistics(
            SearchModule searchModule,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        Map<String, Object> stats = new HashMap<>();
        
        // Total searches
        String totalQuery = "SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.searchModule = :module " +
                           "AND sh.createdAt BETWEEN :fromDate AND :toDate";
        Long totalSearches = entityManager.createQuery(totalQuery, Long.class)
                .setParameter("module", searchModule)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getSingleResult();
        stats.put("totalSearches", totalSearches);
        
        // Unique users
        String usersQuery = "SELECT COUNT(DISTINCT sh.user.userId) FROM SearchHistory sh " +
                           "WHERE sh.searchModule = :module AND sh.createdAt BETWEEN :fromDate AND :toDate";
        Long uniqueUsers = entityManager.createQuery(usersQuery, Long.class)
                .setParameter("module", searchModule)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getSingleResult();
        stats.put("uniqueUsers", uniqueUsers);
        
        // Average results
        String avgQuery = "SELECT AVG(sh.resultCount) FROM SearchHistory sh " +
                         "WHERE sh.searchModule = :module AND sh.createdAt BETWEEN :fromDate AND :toDate";
        Double avgResults = entityManager.createQuery(avgQuery, Double.class)
                .setParameter("module", searchModule)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getSingleResult();
        stats.put("averageResults", avgResults != null ? avgResults : 0.0);
        
        // Zero result searches percentage
        String zeroResultQuery = "SELECT COUNT(sh) FROM SearchHistory sh " +
                               "WHERE sh.searchModule = :module AND sh.resultCount = 0 " +
                               "AND sh.createdAt BETWEEN :fromDate AND :toDate";
        Long zeroResults = entityManager.createQuery(zeroResultQuery, Long.class)
                .setParameter("module", searchModule)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getSingleResult();
        
        double zeroResultPercentage = totalSearches > 0 ? 
            (zeroResults.doubleValue() / totalSearches.doubleValue()) * 100 : 0;
        stats.put("zeroResultPercentage", zeroResultPercentage);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserSearchPatterns(
            Long userId,
            LocalDateTime fromDate,
            int limit) {

        String query = "SELECT sh.searchModule, sh.searchType, COUNT(sh) as searchCount, " +
                      "AVG(sh.resultCount) as avgResults " +
                      "FROM SearchHistory sh " +
                      "WHERE sh.user.userId = :userId AND sh.createdAt >= :fromDate " +
                      "GROUP BY sh.searchModule, sh.searchType " +
                      "ORDER BY searchCount DESC";
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(query)
                .setParameter("userId", userId)
                .setParameter("fromDate", fromDate)
                .setMaxResults(limit)
                .getResultList();
        
        List<Map<String, Object>> patterns = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> pattern = new HashMap<>();
            pattern.put("searchModule", result[0]);
            pattern.put("searchType", result[1]);
            pattern.put("searchCount", result[2]);
            pattern.put("avgResults", result[3]);
            patterns.add(pattern);
        }
        
        return patterns;
    }

    @Override
    public List<String> getSmartSuggestions(
            SearchModule searchModule,
            String partialKeyword,
            Long userId,
            int limit) {

        // Priority: User's own searches > Popular searches > Recent searches
        StringBuilder query = new StringBuilder();
        query.append("(SELECT DISTINCT sh.keyword, 3 as priority FROM SearchHistory sh ");
        query.append("WHERE sh.user.userId = :userId AND sh.searchModule = :module ");
        query.append("AND LOWER(sh.keyword) LIKE LOWER(CONCAT(:keyword, '%')) ");
        query.append("AND sh.keyword IS NOT NULL AND sh.keyword <> '' ) ");
        
        query.append("UNION ");
        
        query.append("(SELECT sh.keyword, 2 as priority FROM SearchHistory sh ");
        query.append("WHERE sh.searchModule = :module ");
        query.append("AND LOWER(sh.keyword) LIKE LOWER(CONCAT(:keyword, '%')) ");
        query.append("AND sh.keyword IS NOT NULL AND sh.keyword <> '' ");
        query.append("GROUP BY sh.keyword HAVING COUNT(sh) > 1) ");
        
        query.append("UNION ");
        
        query.append("(SELECT DISTINCT sh.keyword, 1 as priority FROM SearchHistory sh ");
        query.append("WHERE sh.searchModule = :module ");
        query.append("AND LOWER(sh.keyword) LIKE LOWER(CONCAT(:keyword, '%')) ");
        query.append("AND sh.keyword IS NOT NULL AND sh.keyword <> '' ");
        query.append("AND sh.createdAt >= :recentDate) ");
        
        query.append("ORDER BY priority DESC, keyword ASC");
        
        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        nativeQuery.setParameter("userId", userId);
        nativeQuery.setParameter("module", searchModule.name());
        nativeQuery.setParameter("keyword", partialKeyword);
        nativeQuery.setParameter("recentDate", LocalDateTime.now().minusDays(7));
        nativeQuery.setMaxResults(limit);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = nativeQuery.getResultList();
        
        return results.stream()
                .map(result -> (String) result[0])
                .distinct()
                .limit(limit)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getTrendingAnalysis(
            SearchModule searchModule,
            int daysPeriod,
            int limit) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffDate = now.minusDays(daysPeriod);
        LocalDateTime previousPeriodStart = cutoffDate.minusDays(daysPeriod);
        
        String query = 
            "SELECT " +
            "    recent.keyword, " +
            "    recent.recent_count, " +
            "    COALESCE(previous.previous_count, 0) as previous_count, " +
            "    CASE " +
            "        WHEN COALESCE(previous.previous_count, 0) = 0 THEN 100.0 " +
            "        ELSE ((recent.recent_count - COALESCE(previous.previous_count, 0)) * 100.0 / COALESCE(previous.previous_count, 1)) " +
            "    END as growth_rate " +
            "FROM " +
            "    (SELECT keyword, COUNT(*) as recent_count " +
            "     FROM search_histories " +
            "     WHERE search_module = :module " +
            "     AND keyword IS NOT NULL AND keyword <> '' " +
            "     AND created_at >= :recentStart " +
            "     GROUP BY keyword " +
            "     HAVING COUNT(*) >= 2) recent " +
            "LEFT JOIN " +
            "    (SELECT keyword, COUNT(*) as previous_count " +
            "     FROM search_histories " +
            "     WHERE search_module = :module " +
            "     AND keyword IS NOT NULL AND keyword <> '' " +
            "     AND created_at >= :previousStart AND created_at < :recentStart " +
            "     GROUP BY keyword) previous " +
            "ON recent.keyword = previous.keyword " +
            "ORDER BY growth_rate DESC, recent.recent_count DESC";
        
        Query nativeQuery = entityManager.createNativeQuery(query);
        nativeQuery.setParameter("module", searchModule.name());
        nativeQuery.setParameter("recentStart", cutoffDate);
        nativeQuery.setParameter("previousStart", previousPeriodStart);
        nativeQuery.setMaxResults(limit);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = nativeQuery.getResultList();
        
        List<Map<String, Object>> trendingList = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> trending = new HashMap<>();
            trending.put("keyword", result[0]);
            trending.put("recentCount", result[1]);
            trending.put("previousCount", result[2]);
            trending.put("growthRate", result[3]);
            trendingList.add(trending);
        }
        
        return trendingList;
    }
}