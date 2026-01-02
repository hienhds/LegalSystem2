package com.example.scheduleservice.appointment.repository;

import com.example.scheduleservice.appointment.entity.LawyerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LawyerAvailabilityRepository extends JpaRepository<LawyerAvailability, Long> {
    
    List<LawyerAvailability> findByLawyerId(Long lawyerId);
    
    List<LawyerAvailability> findByLawyerIdAndIsActive(Long lawyerId, Boolean isActive);
    
    boolean existsByLawyerIdAndDayOfWeek(Long lawyerId, Integer dayOfWeek);
}
