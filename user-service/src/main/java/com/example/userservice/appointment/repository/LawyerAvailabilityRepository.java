package com.example.userservice.appointment.repository;

import com.example.userservice.appointment.entity.LawyerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface LawyerAvailabilityRepository extends JpaRepository<LawyerAvailability, Long> {
    
    // Lấy tất cả lịch làm việc của luật sư (chỉ active)
    List<LawyerAvailability> findByLawyer_LawyerIdAndIsActiveTrue(Long lawyerId);
    
    // Lấy tất cả lịch làm việc của luật sư (bao gồm cả inactive)
    List<LawyerAvailability> findByLawyer_LawyerId(Long lawyerId);
    
    // Lấy lịch làm việc theo ngày trong tuần
    List<LawyerAvailability> findByLawyer_LawyerIdAndDayOfWeekAndIsActiveTrue(
        Long lawyerId, 
        Integer dayOfWeek
    );
    
    // Kiểm tra trùng lặp khung giờ (overlap detection)
    @Query("SELECT COUNT(la) > 0 FROM LawyerAvailability la " +
           "WHERE la.lawyer.lawyerId = :lawyerId " +
           "AND la.dayOfWeek = :dayOfWeek " +
           "AND la.isActive = true " +
           "AND (:excludeId IS NULL OR la.availabilityId != :excludeId) " +
           "AND ((la.startTime < :endTime AND la.endTime > :startTime))")
    boolean existsOverlappingAvailability(
        @Param("lawyerId") Long lawyerId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId
    );
    
    // Kiểm tra xem thời gian có nằm trong khung giờ làm việc không
    @Query("SELECT COUNT(la) > 0 FROM LawyerAvailability la " +
           "WHERE la.lawyer.lawyerId = :lawyerId " +
           "AND la.dayOfWeek = :dayOfWeek " +
           "AND la.isActive = true " +
           "AND la.startTime <= :time AND la.endTime > :time")
    boolean existsByLawyerAndDayAndTimeRange(
        @Param("lawyerId") Long lawyerId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("time") LocalTime time
    );
    
    // Đếm số lượng availability của luật sư
    long countByLawyer_LawyerId(Long lawyerId);
}
