package com.example.scheduleservice.appointment.repository;

import com.example.scheduleservice.appointment.entity.LawyerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LawyerAvailabilityRepository extends JpaRepository<LawyerAvailability, Long> {
    
    List<LawyerAvailability> findByLawyerIdAndIsActiveTrue(Long lawyerId);
    
    List<LawyerAvailability> findByLawyerId(Long lawyerId);
    
    Optional<LawyerAvailability> findByLawyerIdAndDayOfWeek(Long lawyerId, Integer dayOfWeek);
    
    @Query("SELECT la FROM LawyerAvailability la WHERE la.lawyerId = :lawyerId AND la.dayOfWeek = :dayOfWeek AND la.isActive = true")
    Optional<LawyerAvailability> findActiveLawyerAvailability(
            @Param("lawyerId") Long lawyerId, 
            @Param("dayOfWeek") Integer dayOfWeek);
    
    boolean existsByLawyerIdAndDayOfWeek(Long lawyerId, Integer dayOfWeek);
}
