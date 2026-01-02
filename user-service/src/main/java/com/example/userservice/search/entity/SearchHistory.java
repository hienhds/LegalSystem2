package com.example.userservice.search.entity;

import com.example.userservice.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity lưu trữ lịch sử tìm kiếm của người dùng
 */
@Entity
@Table(name = "search_histories",
        indexes = {
                @Index(name = "idx_search_histories_user_id", columnList = "user_id"),
                @Index(name = "idx_search_histories_created_at", columnList = "created_at"),
                @Index(name = "idx_search_histories_search_query", columnList = "search_query"),
                @Index(name = "idx_search_histories_keyword", columnList = "keyword"),
                @Index(name = "idx_search_histories_category", columnList = "category"),
                @Index(name = "idx_search_histories_module", columnList = "search_module"),
                @Index(name = "idx_search_histories_user_time", columnList = "user_id, created_at"),
                @Index(name = "idx_search_histories_module_time", columnList = "search_module, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long id;

    /**
     * Người dùng thực hiện tìm kiếm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User không được null")
    private User user;

    /**
     * Module được tìm kiếm (LEGAL_DOCUMENT, LAWYER, FORUM, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "search_module", nullable = false, length = 50)
    @NotNull(message = "Search module không được null")
    private SearchModule searchModule;

    /**
     * Từ khóa tìm kiếm chính (search_query field)
     */
    @Column(name = "search_query", length = 255)
    private String searchQuery;

    /**
     * Từ khóa tìm kiếm (keyword field - backward compatibility)
     */
    @Column(name = "keyword", length = 500)
    @Size(max = 500, message = "Keyword không được vượt quá 500 ký tự")
    private String keyword;

    /**
     * Danh mục tìm kiếm (theo từng module)
     */
    @Column(name = "category", length = 100)
    @Size(max = 100, message = "Category không được vượt quá 100 ký tự")
    private String category;

    /**
     * Loại tìm kiếm (GENERAL, ADVANCED, FILTER, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "search_type", nullable = false, length = 50)
    @NotNull(message = "Search type không được null")
    @Builder.Default
    private SearchType searchType = SearchType.GENERAL;

    /**
     * Bộ lọc chi tiết dưới dạng JSON (search_filters field)
     */
    @Column(name = "search_filters", columnDefinition = "JSON")
    private String searchFilters;

    /**
     * Bộ lọc chi tiết dưới dạng JSON (filters_json field - backward compatibility)
     */
    @Column(name = "filters_json", columnDefinition = "JSON")
    private String filtersJson;
    
    /**
     * Thời gian thực hiện tìm kiếm (search_timestamp field)
     */
    @Column(name = "search_timestamp")
    private LocalDateTime searchTimestamp;
    
    /**
     * Thời gian thực hiện tìm kiếm (search_time field - backward compatibility)
     */
    @Column(name = "search_time")
    private LocalDateTime searchTime;
    
    /**
     * Thời gian thực thi search (milliseconds)
     */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * Số kết quả trả về từ tìm kiếm (results_count field)
     */
    @Column(name = "results_count")
    private Integer resultsCount;

    /**
     * Số kết quả trả về từ tìm kiếm (result_count field - backward compatibility)
     */
    @Column(name = "result_count")
    @Builder.Default
    private Integer resultCount = 0;

    /**
     * Địa chỉ IP của người dùng
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * User Agent của trình duyệt
     */
    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Thời gian click vào kết quả
     */
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    /**
     * ID của luật sư được click (nếu có)
     */
    @Column(name = "clicked_lawyer_id")
    private Long clickedLawyerId;

    /**
     * Thời gian tạo record
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== Utility Methods ====================

    /**
     * Lấy từ khóa tìm kiếm (priority: searchQuery -> keyword)
     */
    public String getSearchKeyword() {
        return searchQuery != null ? searchQuery : keyword;
    }

    /**
     * Set từ khóa tìm kiếm cho cả 2 fields
     */
    public void setSearchKeyword(String searchKeyword) {
        this.searchQuery = searchKeyword;
        this.keyword = searchKeyword; // backward compatibility
    }

    /**
     * Lấy số kết quả (priority: resultsCount -> resultCount)
     */
    public Integer getResultsCount() {
        return resultsCount != null ? resultsCount : resultCount;
    }

    /**
     * Set số kết quả cho cả 2 fields
     */
    public void setResultsCount(Integer count) {
        this.resultsCount = count;
        this.resultCount = count != null ? count : 0; // backward compatibility
    }

    /**
     * Lấy filters JSON (priority: searchFilters -> filtersJson)
     */
    public String getFiltersJsonString() {
        return searchFilters != null ? searchFilters : filtersJson;
    }

    /**
     * Set filters JSON cho cả 2 fields
     */
    public void setFiltersJsonString(String filters) {
        this.searchFilters = filters;
        this.filtersJson = filters; // backward compatibility
    }

    /**
     * Lấy thời gian tìm kiếm (priority: searchTimestamp -> searchTime -> createdAt)
     */
    public LocalDateTime getSearchTimestamp() {
        if (searchTimestamp != null) {
            return searchTimestamp;
        }
        if (searchTime != null) {
            return searchTime;
        }
        return createdAt;
    }

    /**
     * Set thời gian tìm kiếm cho cả 2 fields
     */
    public void setSearchTimestamp(LocalDateTime timestamp) {
        this.searchTimestamp = timestamp;
        this.searchTime = timestamp; // backward compatibility
    }

    /**
     * Kiểm tra xem tìm kiếm này có từ khóa không
     */
    public boolean hasKeyword() {
        String keyword = getSearchKeyword();
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * Kiểm tra xem tìm kiếm này có category không
     */
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }

    /**
     * Kiểm tra xem tìm kiếm này có filters không
     */
    public boolean hasFilters() {
        String filters = getFiltersJsonString();
        return filters != null && !filters.trim().isEmpty();
    }

    /**
     * Kiểm tra xem tìm kiếm này có trả về kết quả không
     */
    public boolean hasResults() {
        Integer count = getResultsCount();
        return count != null && count > 0;
    }

    /**
     * Lấy từ khóa đã clean (trim và lowercase)
     */
    public String getCleanKeyword() {
        String keyword = getSearchKeyword();
        if (keyword == null) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }

    /**
     * Lấy category đã clean (trim)
     */
    public String getCleanCategory() {
        if (category == null) {
            return null;
        }
        return category.trim();
    }
    
    /**
     * Get filters as Map from JSON string (priority: searchFilters -> filtersJson)
     */
    public Map<String, Object> getFilters() {
        String filtersStr = getFiltersJsonString();
        if (filtersStr == null || filtersStr.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(filtersStr, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Set filters from Map to JSON string (update both fields)
     */
    public void setFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            setFiltersJsonString(null);
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String filtersJson = mapper.writeValueAsString(filters);
            setFiltersJsonString(filtersJson);
        } catch (JsonProcessingException e) {
            setFiltersJsonString(null);
        }
    }

    // ==================== ToString Method ====================

    @Override
    public String toString() {
        return "SearchHistory{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", searchModule=" + searchModule +
                ", searchQuery='" + getSearchKeyword() + '\'' +
                ", category='" + category + '\'' +
                ", searchType=" + searchType +
                ", resultsCount=" + getResultsCount() +
                ", createdAt=" + createdAt +
                '}';
    }

    // ==================== Equals & HashCode ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchHistory that = (SearchHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
