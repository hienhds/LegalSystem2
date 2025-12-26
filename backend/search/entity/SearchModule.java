package com.example.backend.search.entity;

/**
 * Enum định nghĩa các module tìm kiếm trong hệ thống
 */
public enum SearchModule {
    
    LEGAL_DOCUMENT("Legal Document", "Tìm kiếm văn bản pháp luật"),
    LAWYER("Lawyer", "Tìm kiếm luật sư"),
    FORUM("Forum", "Tìm kiếm forum/Q&A"),
    APPOINTMENT("Appointment", "Tìm kiếm lịch hẹn"),
    USER("User", "Tìm kiếm người dùng");
    
    private final String code;
    private final String description;
    
    SearchModule(String code, String description) {
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
     * Tìm SearchModule từ code
     */
    public static SearchModule fromCode(String code) {
        for (SearchModule module : SearchModule.values()) {
            if (module.getCode().equalsIgnoreCase(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid SearchModule code: " + code);
    }
}