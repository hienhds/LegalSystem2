package com.example.scheduleservice.appointment.service;

import com.example.scheduleservice.appointment.dto.LawyerAvailabilityRequest;
import com.example.scheduleservice.appointment.dto.LawyerAvailabilityUpdateRequest;
import com.example.scheduleservice.appointment.dto.LawyerAvailabilityResponse;
import com.example.scheduleservice.appointment.entity.LawyerAvailability;
import com.example.scheduleservice.appointment.repository.LawyerAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LawyerScheduleService {
    
    private final LawyerAvailabilityRepository availabilityRepository;
    
    @Transactional
    public List<LawyerAvailabilityResponse> createAvailability(LawyerAvailabilityRequest request) {
        log.info("Creating availability for lawyer {} on days {}", request.getLawyerId(), request.getDaysOfWeek());
        
        // Validate start time < end time
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc");
        }
        
        List<LawyerAvailability> createdAvailabilities = new ArrayList<>();
        List<Integer> skippedDays = new ArrayList<>();
        
        // Tạo availability cho từng ngày được chọn
        for (Integer dayOfWeek : request.getDaysOfWeek()) {
            // Kiểm tra xem đã tồn tại chưa
            if (availabilityRepository.existsByLawyerIdAndDayOfWeek(request.getLawyerId(), dayOfWeek)) {
                log.warn("Availability already exists for lawyer {} on day {}", request.getLawyerId(), dayOfWeek);
                skippedDays.add(dayOfWeek);
                continue;
            }
            
            LawyerAvailability availability = LawyerAvailability.builder()
                    .lawyerId(request.getLawyerId())
                    .dayOfWeek(dayOfWeek)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .workAddress(request.getWorkAddress())
                    .workAddressDetails(request.getWorkAddressDetails())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .timeZone(request.getTimeZone() != null ? request.getTimeZone() : "Asia/Ho_Chi_Minh")
                    .build();
            
            createdAvailabilities.add(availabilityRepository.save(availability));
        }
        
        if (!skippedDays.isEmpty()) {
            log.info("Skipped days (already exist): {}", skippedDays);
        }
        
        return createdAvailabilities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public LawyerAvailabilityResponse updateAvailability(Long availabilityId, LawyerAvailabilityUpdateRequest request) {
        log.info("Updating availability {}", availabilityId);
        
        LawyerAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ làm việc với ID: " + availabilityId));
        
        // Validate start time < end time
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc");
        }
        
        Integer newDayOfWeek = request.getDayOfWeek();
        
        // Kiểm tra nếu thay đổi dayOfWeek và đã tồn tại
        if (!availability.getDayOfWeek().equals(newDayOfWeek)) {
            if (availabilityRepository.existsByLawyerIdAndDayOfWeek(availability.getLawyerId(), newDayOfWeek)) {
                throw new IllegalArgumentException("Đã có khung giờ làm việc cho ngày này");
            }
            availability.setDayOfWeek(newDayOfWeek);
        }
        
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setWorkAddress(request.getWorkAddress());
        availability.setWorkAddressDetails(request.getWorkAddressDetails());
        availability.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        availability.setUpdatedAt(LocalDateTime.now());
        
        return mapToResponse(availabilityRepository.save(availability));
    }
    
    @Transactional
    public void deleteAvailability(Long availabilityId, Long lawyerId) {
        log.info("Deleting availability {} for lawyer {}", availabilityId, lawyerId);
        
        LawyerAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ làm việc với ID: " + availabilityId));
        
        // Verify ownership
        if (!availability.getLawyerId().equals(lawyerId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa khung giờ làm việc này");
        }
        
        availabilityRepository.delete(availability);
    }
    
    public List<LawyerAvailabilityResponse> getLawyerAvailabilities(Long lawyerId) {
        log.info("Getting availabilities for lawyer {}", lawyerId);
        
        List<LawyerAvailability> availabilities = availabilityRepository.findByLawyerId(lawyerId);
        
        return availabilities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<LawyerAvailabilityResponse> getActiveLawyerAvailabilities(Long lawyerId) {
        log.info("Getting active availabilities for lawyer {}", lawyerId);
        
        List<LawyerAvailability> availabilities = availabilityRepository.findByLawyerIdAndIsActiveTrue(lawyerId);
        
        return availabilities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private LawyerAvailabilityResponse mapToResponse(LawyerAvailability availability) {
        LawyerAvailabilityResponse response = LawyerAvailabilityResponse.builder()
                .availabilityId(availability.getAvailabilityId())
                .lawyerId(availability.getLawyerId())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .workAddress(availability.getWorkAddress())
                .workAddressDetails(availability.getWorkAddressDetails())
                .isActive(availability.getIsActive())
                .timeZone(availability.getTimeZone())
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .build();
        
        // Set day of week name
        response.setDayOfWeekName(response.getDayOfWeekName());
        
        return response;
    }
}
