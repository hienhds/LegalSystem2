package com.example.backend.search.entity;

/**
 * Enum định nghĩa các loại tìm kiếm
 */
public enum SearchType {
    
    GENERAL("General", "Tìm kiếm chung theo keyword"),
    ADVANCED("Advanced", "Tìm kiếm nâng cao với nhiều filters"),
    FILTER("Filter", "Tìm kiếm theo bộ lọc cụ thể"),
    CATEGORY("Category", "Tìm kiếm theo danh mục"),
    TRENDING("Trending", "Xem nội dung phổ biến"),
    BY_ID("By ID", "Truy cập trực tiếp theo ID"),
    AUTOCOMPLETE("Autocomplete", "Auto-suggest/completion");
    
    private final String code;
    private final String description;
    
    SearchType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDisplayName() {
        return description;
    }
    
    /**
     * Tìm SearchType từ code
     */
    public static SearchType fromCode(String code) {
        for (SearchType type : SearchType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid SearchType code: " + code);
    }
}