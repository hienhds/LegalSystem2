package com.example.backend.document.repository;

import com.example.backend.document.entity.LegalDocument;
import com.example.backend.document.entity.LegalDocument.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    // Find by title (exact match)
    Optional<LegalDocument> findByTitle(String title);

    // Find by title containing keyword (case-insensitive)
    @Query("SELECT ld FROM LegalDocument ld WHERE LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND ld.status = :status")
    Page<LegalDocument> findByTitleContaining(@Param("keyword") String keyword, @Param("status") DocumentStatus status, Pageable pageable);

    // Find by category
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.category = :category AND ld.status = :status")
    Page<LegalDocument> findByCategory(@Param("category") String category, @Param("status") DocumentStatus status, Pageable pageable);

    // Find by status
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.status = :status")
    Page<LegalDocument> findByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    // Advanced search: title AND category
    @Query("SELECT ld FROM LegalDocument ld WHERE " +
           "LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND ld.category = :category " +
           "AND ld.status = :status")
    Page<LegalDocument> findByTitleContainingAndCategory(@Param("keyword") String keyword, 
                                                        @Param("category") String category, 
                                                        @Param("status") DocumentStatus status, 
                                                        Pageable pageable);

    // Get all unique categories
    @Query("SELECT DISTINCT ld.category FROM LegalDocument ld WHERE ld.status = :status ORDER BY ld.category")
    List<String> findAllCategories(@Param("status") DocumentStatus status);

    // Get trending/popular documents
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.status = :status ORDER BY ld.viewCount DESC")
    Page<LegalDocument> findTrendingDocuments(@Param("status") DocumentStatus status, Pageable pageable);

    // Increment view count
    @Modifying
    @Transactional
    @Query("UPDATE LegalDocument ld SET ld.viewCount = ld.viewCount + 1 WHERE ld.documentId = :documentId")
    void incrementViewCount(@Param("documentId") Long documentId);

    // Get documents by multiple categories
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.category IN :categories AND ld.status = :status ORDER BY ld.viewCount DESC")
    Page<LegalDocument> findByCategoryIn(@Param("categories") List<String> categories, @Param("status") DocumentStatus status, Pageable pageable);

    // Count documents by category
    @Query("SELECT ld.category, COUNT(ld) FROM LegalDocument ld WHERE ld.status = :status GROUP BY ld.category")
    List<Object[]> countDocumentsByCategory(@Param("status") DocumentStatus status);

    // Search in both title and category (general search)
    @Query("SELECT ld FROM LegalDocument ld WHERE " +
           "(LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(ld.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND ld.status = :status " +
           "ORDER BY ld.viewCount DESC")
    Page<LegalDocument> searchByKeyword(@Param("keyword") String keyword, 
                                       @Param("status") DocumentStatus status, 
                                       Pageable pageable);
    
    // Count documents created between dates
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Admin queries - without status filter
    @Query("SELECT ld FROM LegalDocument ld WHERE LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<LegalDocument> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.category = :category")
    Page<LegalDocument> findByCategory(@Param("category") String category, Pageable pageable);
    
    Page<LegalDocument> findByStatusEquals(DocumentStatus status, Pageable pageable);
    
    @Query("SELECT ld FROM LegalDocument ld WHERE LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND ld.category = :category")
    Page<LegalDocument> findByTitleContainingAndCategory(@Param("keyword") String keyword, 
                                                         @Param("category") String category, 
                                                         Pageable pageable);
    
    @Query("SELECT ld FROM LegalDocument ld WHERE LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND ld.status = :status")
    Page<LegalDocument> findByTitleContainingAndStatus(@Param("keyword") String keyword, 
                                                       @Param("status") DocumentStatus status, 
                                                       Pageable pageable);
    
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.category = :category AND ld.status = :status")
    Page<LegalDocument> findByCategoryAndStatus(@Param("category") String category, 
                                                @Param("status") DocumentStatus status, 
                                                Pageable pageable);
    
    @Query("SELECT ld FROM LegalDocument ld WHERE LOWER(ld.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND ld.category = :category AND ld.status = :status")
    Page<LegalDocument> findByTitleContainingAndCategoryAndStatus(@Param("keyword") String keyword, 
                                                                  @Param("category") String category, 
                                                                  @Param("status") DocumentStatus status, 
                                                                  Pageable pageable);
    
    // Count by status
    Long countByStatus(DocumentStatus status);
    
    // Get distinct categories
    @Query("SELECT DISTINCT ld.category FROM LegalDocument ld ORDER BY ld.category")
    List<String> findDistinctCategories();
}