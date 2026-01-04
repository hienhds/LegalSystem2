package com.example.scheduleservice.appointment.repository;

import com.example.scheduleservice.appointment.entity.Appointment;
import com.example.scheduleservice.appointment.entity.Appointment.AppointmentStatus;
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
    Page<Appointment> findByCitizenId(Long citizenId, Pageable pageable);
    
    // Tìm appointments theo lawyer ID  
    Page<Appointment> findByLawyerId(Long lawyerId, Pageable pageable);
    
    // Tìm appointments theo citizen ID và status
    Page<Appointment> findByCitizenIdAndStatus(Long citizenId, AppointmentStatus status, Pageable pageable);
    
    // Tìm appointments theo lawyer ID và status
    Page<Appointment> findByLawyerIdAndStatus(Long lawyerId, AppointmentStatus status, Pageable pageable);
    
    // Tìm appointment theo ID và citizen ID (để security check)
    Optional<Appointment> findByAppointmentIdAndCitizenId(Long appointmentId, Long citizenId);
    
    // Tìm appointment theo ID và lawyer ID (để security check)  
    Optional<Appointment> findByAppointmentIdAndLawyerId(Long appointmentId, Long lawyerId);
    
    // Kiểm tra xem lawyer có appointment trong khoảng thời gian này không
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.lawyerId = :lawyerId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.appointmentTime = :appointmentTime " +
           "AND a.status IN ('CONFIRMED', 'PENDING')")
    boolean existsByLawyerAndDateTime(@Param("lawyerId") Long lawyerId, 
                                    @Param("appointmentDate") LocalDate appointmentDate,
                                    @Param("appointmentTime") java.time.LocalTime appointmentTime);
    
    // Lấy appointments theo ngày cho lawyer (để xem schedule)
    @Query("SELECT a FROM Appointment a WHERE a.lawyerId = :lawyerId " +
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
}