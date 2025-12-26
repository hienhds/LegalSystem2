package com.example.backend.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCategoryResponse {
    
    private String categoryName;
    private Long documentCount;
    
    // Static factory method for category statistics
    public static DocumentCategoryResponse fromCategoryStats(String categoryName, Long count) {
        return DocumentCategoryResponse.builder()
            .categoryName(categoryName)
            .documentCount(count)
            .build();
    }
    
    // Container for multiple categories
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentCategoriesResponse {
        private List<String> categories;
        private List<DocumentCategoryResponse> categoryStats;
        private Integer totalCategories;
        
        public static DocumentCategoriesResponse fromCategories(List<String> categories) {
            return DocumentCategoriesResponse.builder()
                .categories(categories)
                .totalCategories(categories.size())
                .build();
        }
        
        public static DocumentCategoriesResponse fromCategoryStats(List<DocumentCategoryResponse> stats) {
            return DocumentCategoriesResponse.builder()
                .categoryStats(stats)
                .totalCategories(stats.size())
                .build();
        }
    }
}