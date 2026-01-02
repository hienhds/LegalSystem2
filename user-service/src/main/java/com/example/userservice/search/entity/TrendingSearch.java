package com.example.userservice.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trending_searches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trendingId;
    
    @Column(nullable = false, unique = true)
    private String searchKeyword;
    
    @Column(nullable = false)
    @Builder.Default
    private Long searchCount = 1L;
    
    @Column(nullable = false)
    @Builder.Default
    private Double trendingScore = 0.0;
    
    @CreationTimestamp
    private LocalDateTime firstSearched;
    
    @UpdateTimestamp
    private LocalDateTime lastUpdated;
    
    // Để tính trending score
    private LocalDateTime lastWeekCount;
    private LocalDateTime lastMonthCount;
}
