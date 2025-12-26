package com.example.userservice.appointment.service;

import com.example.userservice.appointment.dto.AvailableSlotResponse;
import com.example.userservice.appointment.dto.BulkAvailabilityRequest;
import com.example.userservice.appointment.dto.LawyerAvailabilityRequest;
import com.example.userservice.appointment.dto.LawyerAvailabilityResponse;
import com.example.userservice.appointment.entity.Appointment;
import com.example.userservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.userservice.appointment.entity.LawyerAvailability;
import com.example.userservice.appointment.repository.AppointmentRepository;
import com.example.userservice.appointment.repository.LawyerAvailabilityRepository;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.lawyer.entity.Lawyer;
import com.example.userservice.lawyer.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LawyerScheduleService {
    
    private final LawyerAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final LawyerRepository lawyerRepository;
    
    /**
     * Tạo khung giờ làm việc mới cho luật sư
     */
    public LawyerAvailabilityResponse createAvailability(LawyerAvailabilityRequest request, Long lawyerId) {
        log.info("Creating availability for lawyer {} on day {}", lawyerId, request.getDayOfWeek());
        
        // Validate lawyer exists
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy luật sư với ID: " + lawyerId));
        
        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Check for overlapping availability
        validateNoOverlap(lawyerId, request.getDayOfWeek(), 
                         request.getStartTime(), request.getEndTime(), null);
        
        // Create availability
        LawyerAvailability availability = LawyerAvailability.builder()
            .lawyer(lawyer)
            .dayOfWeek(request.getDayOfWeek())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .timeZone(request.getTimeZone() != null ? request.getTimeZone() : "Asia/Ho_Chi_Minh")
            .build();
        
        availability = availabilityRepository.save(availability);
        
        log.info("Created availability with ID: {}", availability.getAvailabilityId());
        return mapToResponse(availability);
    }
    
    /**
     * Tạo nhiều khung giờ làm việc cùng lúc (bulk create)
     */
    public List<LawyerAvailabilityResponse> createBulkAvailability(BulkAvailabilityRequest request, Long lawyerId) {
        log.info("Creating bulk availability for lawyer {} on days {}", lawyerId, request.getDayOfWeeks());
        
        // Validate lawyer exists
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy luật sư với ID: " + lawyerId));
        
        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Check for duplicates in request
        long distinctDays = request.getDayOfWeeks().stream().distinct().count();
        if (distinctDays != request.getDayOfWeeks().size()) {
            throw new AppException(ErrorType.VALIDATION_ERROR, "Danh sách ngày trong tuần có trùng lặp");
        }
        
        List<LawyerAvailability> createdAvailabilities = new ArrayList<>();
        
        // Validate and create for each day
        for (Integer dayOfWeek : request.getDayOfWeeks()) {
            // Check for overlapping availability
            validateNoOverlap(lawyerId, dayOfWeek, request.getStartTime(), request.getEndTime(), null);
            
            // Create availability
            LawyerAvailability availability = LawyerAvailability.builder()
                .lawyer(lawyer)
                .dayOfWeek(dayOfWeek)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .timeZone(request.getTimeZone() != null ? request.getTimeZone() : "Asia/Ho_Chi_Minh")
                .build();
            
            createdAvailabilities.add(availability);
        }
        
        // Save all at once
        createdAvailabilities = availabilityRepository.saveAll(createdAvailabilities);
        
        log.info("Created {} availabilities for lawyer {}", createdAvailabilities.size(), lawyerId);
        return createdAvailabilities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Cập nhật khung giờ làm việc
     */
    public LawyerAvailabilityResponse updateAvailability(Long availabilityId, LawyerAvailabilityRequest request, Long lawyerId) {
        log.info("Updating availability {}", availabilityId);
        
        LawyerAvailability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy khung giờ làm việc với ID: " + availabilityId));
        
        // Validate lawyer ownership
        if (!availability.getLawyer().getLawyerId().equals(lawyerId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không có quyền sửa khung giờ làm việc này");
        }
        
        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Check for overlapping availability (excluding current)
        validateNoOverlap(lawyerId, request.getDayOfWeek(), 
                         request.getStartTime(), request.getEndTime(), availabilityId);
        
        // Update fields
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setIsActive(request.getIsActive() != null ? request.getIsActive() : availability.getIsActive());
        if (request.getTimeZone() != null) {
            availability.setTimeZone(request.getTimeZone());
        }
        availability.setUpdatedAt(LocalDateTime.now());
        
        availability = availabilityRepository.save(availability);
        
        log.info("Updated availability {}", availabilityId);
        return mapToResponse(availability);
    }
    
    /**
     * Xóa khung giờ làm việc
     */
    public void deleteAvailability(Long availabilityId) {
        log.info("Deleting availability {}", availabilityId);
        
        LawyerAvailability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy khung giờ làm việc với ID: " + availabilityId));
        
        // Check if there are any confirmed appointments using this availability
        validateNoExistingAppointments(availability);
        
        availabilityRepository.delete(availability);
        
        log.info("Deleted availability {}", availabilityId);
    }
    
    /**
     * Lấy tất cả khung giờ làm việc của luật sư
     */
    @Transactional(readOnly = true)
    public List<LawyerAvailabilityResponse> getLawyerAvailabilities(Long lawyerId, Boolean activeOnly) {
        log.info("Getting availabilities for lawyer {}", lawyerId);
        
        // Validate lawyer exists
        if (!lawyerRepository.existsById(lawyerId)) {
            throw new AppException(ErrorType.NOT_FOUND, "Không tìm thấy luật sư với ID: " + lawyerId);
        }
        
        List<LawyerAvailability> availabilities;
        if (activeOnly != null && activeOnly) {
            availabilities = availabilityRepository.findByLawyer_LawyerIdAndIsActiveTrue(lawyerId);
        } else {
            availabilities = availabilityRepository.findByLawyer_LawyerId(lawyerId);
        }
        
        return availabilities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy khung giờ trống của luật sư trong ngày cụ thể
     */
    @Transactional(readOnly = true)
    public AvailableSlotResponse getAvailableSlots(Long lawyerId, LocalDate date, Integer durationMinutes) {
        log.info("Getting available slots for lawyer {} on {}", lawyerId, date);
        
        // Validate lawyer exists
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy luật sư với ID: " + lawyerId));
        
        // Default duration is 60 minutes
        int duration = durationMinutes != null ? durationMinutes : 60;
        
        // Get day of week (1 = Monday, 7 = Sunday)
        int dayOfWeek = date.getDayOfWeek().getValue();
        
        // Get lawyer's working schedule for this day
        List<LawyerAvailability> workingSchedule = availabilityRepository
            .findByLawyer_LawyerIdAndDayOfWeekAndIsActiveTrue(lawyerId, dayOfWeek);
        
        if (workingSchedule.isEmpty()) {
            return AvailableSlotResponse.builder()
                .date(date)
                .lawyerId(lawyerId)
                .lawyerName(lawyer.getUser().getFullName())
                .hasWorkingSchedule(false)
                .totalAvailableSlots(0)
                .availableSlots(new ArrayList<>())
                .bookedSlots(new ArrayList<>())
                .message("Luật sư hiện chưa cập nhật lịch làm việc cho ngày này, vui lòng quay lại sau")
                .build();
        }
        
        // Get existing appointments for this day
        List<Appointment> appointments = appointmentRepository.findByLawyerAndDate(lawyerId, date);
        
        // Build booked slots
        List<AvailableSlotResponse.BookedSlot> bookedSlots = appointments.stream()
            .map(apt -> AvailableSlotResponse.BookedSlot.builder()
                .appointmentId(apt.getAppointmentId())
                .startTime(apt.getAppointmentTime())
                .endTime(apt.getAppointmentTime().plusMinutes(apt.getDurationMinutes()))
                .status(apt.getStatus())
                .durationMinutes(apt.getDurationMinutes())
                .build())
            .collect(Collectors.toList());
        
        // Generate available time slots
        List<AvailableSlotResponse.TimeSlot> availableSlots = new ArrayList<>();
        
        for (LawyerAvailability schedule : workingSchedule) {
            LocalTime currentTime = schedule.getStartTime();
            LocalTime endTime = schedule.getEndTime();
            
            while (currentTime.plusMinutes(duration).isBefore(endTime) || 
                   currentTime.plusMinutes(duration).equals(endTime)) {
                
                final LocalTime slotStart = currentTime;
                final LocalTime slotEnd = currentTime.plusMinutes(duration);
                
                // Check if this slot conflicts with any appointment
                boolean isBooked = appointments.stream()
                    .anyMatch(apt -> {
                        LocalTime aptStart = apt.getAppointmentTime();
                        LocalTime aptEnd = aptStart.plusMinutes(apt.getDurationMinutes());
                        // Check overlap
                        return (slotStart.isBefore(aptEnd) && slotEnd.isAfter(aptStart));
                    });
                
                if (!isBooked) {
                    availableSlots.add(AvailableSlotResponse.TimeSlot.builder()
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .durationMinutes(duration)
                        .build());
                }
                
                currentTime = currentTime.plusMinutes(duration);
            }
        }
        
        String message = availableSlots.isEmpty() 
            ? "Luật sư đã kín lịch trong ngày này. Vui lòng chọn ngày khác"
            : null;
        
        return AvailableSlotResponse.builder()
            .date(date)
            .lawyerId(lawyerId)
            .lawyerName(lawyer.getUser().getFullName())
            .hasWorkingSchedule(true)
            .totalAvailableSlots(availableSlots.size())
            .availableSlots(availableSlots)
            .bookedSlots(bookedSlots)
            .message(message)
            .build();
    }
    
    // ===== VALIDATION METHODS =====
    
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new AppException(ErrorType.VALIDATION_ERROR, 
                "Thời gian bắt đầu phải trước thời gian kết thúc");
        }
    }
    
    private void validateNoOverlap(Long lawyerId, Integer dayOfWeek, 
                                   LocalTime startTime, LocalTime endTime, Long excludeId) {
        boolean hasOverlap = availabilityRepository.existsOverlappingAvailability(
            lawyerId, dayOfWeek, startTime, endTime, excludeId);
        
        if (hasOverlap) {
            throw new AppException(ErrorType.CONFLICT, 
                "Khung giờ này đã tồn tại hoặc bị trùng lặp, vui lòng kiểm tra lại");
        }
    }
    
    private void validateNoExistingAppointments(LawyerAvailability availability) {
        // Get all future dates that match this day of week
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(3); // Check next 3 months
        
        // Check if there are any confirmed appointments during this time slot
        List<Appointment> futureAppointments = appointmentRepository.findUpcomingAppointments(today, endDate);
        
        boolean hasConflict = futureAppointments.stream()
            .anyMatch(apt -> {
                // Check if appointment is on the same day of week
                if (apt.getAppointmentDate().getDayOfWeek().getValue() != availability.getDayOfWeek()) {
                    return false;
                }
                // Check if appointment is for this lawyer
                if (!apt.getLawyer().getLawyerId().equals(availability.getLawyer().getLawyerId())) {
                    return false;
                }
                // Check if appointment time falls within this availability slot
                LocalTime aptTime = apt.getAppointmentTime();
                return !aptTime.isBefore(availability.getStartTime()) && 
                       aptTime.isBefore(availability.getEndTime());
            });
        
        if (hasConflict) {
            throw new AppException(ErrorType.CONFLICT, 
                "Không thể xóa khung giờ này vì đã có lịch hẹn. Vui lòng hủy lịch hẹn của khách trước khi thay đổi");
        }
    }
    
    // ===== MAPPING METHODS =====
    
    private LawyerAvailabilityResponse mapToResponse(LawyerAvailability availability) {
        String dayName = getDayOfWeekName(availability.getDayOfWeek());
        
        return LawyerAvailabilityResponse.builder()
            .availabilityId(availability.getAvailabilityId())
            .lawyerId(availability.getLawyer().getLawyerId())
            .lawyerName(availability.getLawyer().getUser().getFullName())
            .dayOfWeek(availability.getDayOfWeek())
            .dayOfWeekName(dayName)
            .startTime(availability.getStartTime())
            .endTime(availability.getEndTime())
            .isActive(availability.getIsActive())
            .timeZone(availability.getTimeZone())
            .createdAt(availability.getCreatedAt())
            .updatedAt(availability.getUpdatedAt())
            .build();
    }
    
    private String getDayOfWeekName(Integer dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Thứ 2";
            case 2 -> "Thứ 3";
            case 3 -> "Thứ 4";
            case 4 -> "Thứ 5";
            case 5 -> "Thứ 6";
            case 6 -> "Thứ 7";
            case 7 -> "Chủ nhật";
            default -> "Unknown";
        };
    }
}
