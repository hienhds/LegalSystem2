package com.example.backend.appointment.repository;

import com.example.backend.appointment.entity.Appointment;
import com.example.backend.appointment.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Tìm appointments theo citizen ID
    Page<Appointment> findByCitizen_UserId(Long citizenId, Pageable pageable);
    
    // Tìm appointments theo lawyer ID  
    Page<Appointment> findByLawyer_LawyerId(Long lawyerId, Pageable pageable);
    
    // Tìm appointments theo citizen ID và status
    Page<Appointment> findByCitizen_UserIdAndStatus(Long citizenId, AppointmentStatus status, Pageable pageable);
    
    // Tìm appointments theo lawyer ID và status
    Page<Appointment> findByLawyer_LawyerIdAndStatus(Long lawyerId, AppointmentStatus status, Pageable pageable);
    
    // Tìm appointment theo ID và citizen ID (để security check)
    Optional<Appointment> findByAppointmentIdAndCitizen_UserId(Long appointmentId, Long citizenId);
    
    // Tìm appointment theo ID và lawyer ID (để security check)  
    Optional<Appointment> findByAppointmentIdAndLawyer_LawyerId(Long appointmentId, Long lawyerId);
    
    // Kiểm tra xem lawyer có appointment trong khoảng thời gian này không
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.lawyer.lawyerId = :lawyerId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.appointmentTime = :appointmentTime " +
           "AND a.status IN ('CONFIRMED', 'PENDING')")
    boolean existsByLawyerAndDateTime(@Param("lawyerId") Long lawyerId, 
                                    @Param("appointmentDate") LocalDate appointmentDate,
                                    @Param("appointmentTime") java.time.LocalTime appointmentTime);
    
    // Lấy appointments theo ngày cho lawyer (để xem schedule)
    @Query("SELECT a FROM Appointment a WHERE a.lawyer.lawyerId = :lawyerId " +
           "AND a.appointmentDate = :date " +
           "AND a.status IN ('CONFIRMED', 'PENDING') " +
           "ORDER BY a.appointmentTime")
    List<Appointment> findByLawyerAndDate(@Param("lawyerId") Long lawyerId, @Param("date") LocalDate date);
    
    // Đếm appointments theo status cho statistics
    long countByStatus(AppointmentStatus status);
    
    // Lấy appointments sắp tới (trong vòng X ngày)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate " +
           "AND a.status = 'CONFIRMED' ORDER BY a.appointmentDate, a.appointmentTime")
    List<Appointment> findUpcomingAppointments(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    // Tìm appointments cần review (đã hoàn thành nhưng chưa rating)
    @Query("SELECT a FROM Appointment a WHERE a.citizen.userId = :citizenId " +
           "AND a.status = 'COMPLETED' AND a.rating IS NULL")
    List<Appointment> findPendingReviews(@Param("citizenId") Long citizenId);
    
    // Tìm appointments theo date range cho reporting
    @Query("SELECT a FROM Appointment a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Appointment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    // Statistics queries for lawyer ratings
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.lawyer.lawyerId = :lawyerId AND a.rating IS NOT NULL")
    Long countReviewsByLawyerId(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT AVG(a.rating) FROM Appointment a WHERE a.lawyer.lawyerId = :lawyerId AND a.rating IS NOT NULL")
    Double getAverageRatingByLawyerId(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'COMPLETED'")
    Long countCompletedAppointments();
    
    @Query("SELECT COUNT(a) FROM Appointment a")
    Long countTotalAppointments();
    
    @Query("SELECT AVG(a.rating) FROM Appointment a WHERE a.rating IS NOT NULL")
    Double getOverallAverageRating();
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.rating IS NOT NULL")
    Long countTotalReviews();
    
    // Get reviews for a lawyer with user details
    @Query("SELECT a FROM Appointment a JOIN FETCH a.citizen " +
           "WHERE a.lawyer.lawyerId = :lawyerId " +
           "AND a.rating IS NOT NULL " +
           "ORDER BY a.reviewedAt DESC")
    Page<Appointment> findReviewsByLawyerId(@Param("lawyerId") Long lawyerId, Pageable pageable);
}