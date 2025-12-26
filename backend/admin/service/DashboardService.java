package com.example.backend.admin.service;

import com.example.backend.admin.dto.*;
import com.example.backend.document.repository.LegalDocumentRepository;
import com.example.backend.lawyer.entity.VerificationStatus;
import com.example.backend.lawyer.repository.LawyerRepository;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final LawyerRepository lawyerRepository;
    private final LegalDocumentRepository legalDocumentRepository;

    public DashboardStatsResponse getDashboardStats() {
        // Đếm tổng số users
        Long totalUsers = userRepository.count();
        
        // Đếm tổng số lawyers
        Long totalLawyers = lawyerRepository.count();
        
        // Đếm tổng số legal documents
        Long totalLegalDocs = legalDocumentRepository.count();
        
        // Tính growth rate (so sánh 30 ngày gần nhất với 30 ngày trước đó)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);
        
        // Users growth
        Long usersLast30Days = userRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        Long usersPrevious30Days = userRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        Double userGrowth = calculateGrowthRate(usersLast30Days, usersPrevious30Days);
        
        // Lawyers growth
        Long lawyersLast30Days = lawyerRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        Long lawyersPrevious30Days = lawyerRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        Double lawyerGrowth = calculateGrowthRate(lawyersLast30Days, lawyersPrevious30Days);
        
        // Legal docs growth
        Long docsLast30Days = legalDocumentRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        Long docsPrevious30Days = legalDocumentRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        Double legalDocGrowth = calculateGrowthRate(docsLast30Days, docsPrevious30Days);
        
        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .userGrowth(userGrowth)
                .totalLawyers(totalLawyers)
                .lawyerGrowth(lawyerGrowth)
                .totalQuestions(null) // Chưa có bảng questions/forum_post
                .questionGrowth(null)
                .totalLegalDocs(totalLegalDocs)
                .legalDocGrowth(legalDocGrowth)
                .build();
    }
    
    private Double calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return 0.0;
        }
        double growth = ((current - previous) * 100.0) / previous;
        return Math.round(growth * 10) / 10.0; // Làm tròn 1 chữ số thập phân
    }

    public RegistrationChartResponse getRegistrationChart(int days) {
        List<String> labels = new ArrayList<>();
        List<Long> lawyerCounts = new ArrayList<>();
        List<Long> userCounts = new ArrayList<>();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days - 1);

        // Tạo labels cho các ngày
        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.toLocalDate().plusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue();
            labels.add(dayNames[dayOfWeek % 7]);
            
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            // Đếm số lawyers đăng ký trong ngày
            Long lawyerCount = lawyerRepository.countByCreatedAtBetween(dayStart, dayEnd);
            lawyerCounts.add(lawyerCount);
            
            // Đếm số users đăng ký trong ngày (tạm thời tính tất cả users)
            Long userCount = userRepository.countByCreatedAtBetween(dayStart, dayEnd);
            userCounts.add(userCount);
        }

        return RegistrationChartResponse.builder()
                .labels(labels)
                .lawyers(lawyerCounts)
                .users(userCounts)
                .build();
    }

    public LawyerVerificationStatusResponse getLawyerVerificationStatus() {
        Long total = lawyerRepository.count();
        Long verified = lawyerRepository.countByVerificationStatus(VerificationStatus.APPROVED);
        Long pending = lawyerRepository.countByVerificationStatus(VerificationStatus.PENDING);
        Long rejected = lawyerRepository.countByVerificationStatus(VerificationStatus.REJECTED);

        Double verifiedPercent = total > 0 ? (verified * 100.0 / total) : 0.0;
        Double pendingPercent = total > 0 ? (pending * 100.0 / total) : 0.0;
        Double rejectedPercent = total > 0 ? (rejected * 100.0 / total) : 0.0;

        return LawyerVerificationStatusResponse.builder()
                .total(total)
                .verified(verified)
                .pending(pending)
                .rejected(rejected)
                .verifiedPercent(Math.round(verifiedPercent * 10) / 10.0)
                .pendingPercent(Math.round(pendingPercent * 10) / 10.0)
                .rejectedPercent(Math.round(rejectedPercent * 10) / 10.0)
                .build();
    }
}
