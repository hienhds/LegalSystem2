package com.example.scheduleservice.appointment.repository;

import com.example.scheduleservice.appointment.entity.BlockedTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockedTimeRepository extends JpaRepository<BlockedTime, Long> {
    
    List<BlockedTime> findByLawyerId(Long lawyerId);
    
    List<BlockedTime> findByLawyerIdAndIsActive(Long lawyerId, Boolean isActive);
    
    @Query("SELECT b FROM BlockedTime b WHERE b.lawyerId = :lawyerId " +
           "AND b.blockedDate = :date " +
           "AND b.isActive = true")
    List<BlockedTime> findByLawyerIdAndDate(@Param("lawyerId") Long lawyerId, @Param("date") LocalDate date);
}
