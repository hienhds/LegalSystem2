package com.example.userservice.appointment.service;

import com.example.userservice.appointment.dto.AppointmentRequest;
import com.example.userservice.appointment.dto.AppointmentResponse;
import com.example.userservice.appointment.entity.Appointment;
import com.example.userservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.userservice.appointment.repository.AppointmentRepository;
import com.example.userservice.appointment.repository.LawyerAvailabilityRepository;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.lawyer.entity.Lawyer;
import com.example.userservice.lawyer.repository.LawyerRepository;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final LawyerRepository lawyerRepository;
    private final LawyerAvailabilityRepository availabilityRepository;

    public AppointmentResponse createAppointment(AppointmentRequest request, Long citizenId) {
        log.info("Creating appointment for citizen {} with lawyer {}", citizenId, request.getLawyerId());
        
        // Validate citizen exists
        User citizen = userRepository.findById(citizenId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy công dân với ID: " + citizenId));
            
        // Validate lawyer exists
        Lawyer lawyer = lawyerRepository.findById(request.getLawyerId())
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy luật sư với ID: " + request.getLawyerId()));
        
        // Validate appointment time is within lawyer's working hours
        int dayOfWeek = request.getAppointmentDate().getDayOfWeek().getValue();
        boolean isWithinWorkingHours = availabilityRepository.existsByLawyerAndDayAndTimeRange(
            request.getLawyerId(),
            dayOfWeek,
            request.getAppointmentTime()
        );
        
        if (!isWithinWorkingHours) {
            throw new AppException(ErrorType.BUSINESS_RULE_VIOLATION, 
                "Thời gian đặt lịch không nằm trong khung giờ làm việc của luật sư. Vui lòng chọn thời gian khác.");
        }
            
        // Check if lawyer is available at this time (check for overlapping appointments)
        List<Appointment> existingAppointments = appointmentRepository.findConfirmedAppointmentsByLawyerAndDate(
            request.getLawyerId(), 
            request.getAppointmentDate()
        );
        
        // Calculate new appointment time range
        LocalTime newStart = request.getAppointmentTime();
        LocalTime newEnd = newStart.plusMinutes(request.getDurationMinutes());
        
        // Check for overlap with existing appointments
        boolean hasConflict = existingAppointments.stream().anyMatch(existing -> {
            LocalTime existingStart = existing.getAppointmentTime();
            LocalTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
            // Overlap if: newStart < existingEnd AND newEnd > existingStart
            return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
        });
        
        if (hasConflict) {
            throw new AppException(ErrorType.CONFLICT, 
                "Khung giờ này bị trùng với lịch hẹn khác. Vui lòng chọn thời gian khác.");
        }
        
        // Create appointment
        Appointment appointment = Appointment.builder()
            .citizen(citizen)
            .lawyer(lawyer)
            .appointmentDate(request.getAppointmentDate())
            .appointmentTime(request.getAppointmentTime())
            .description(request.getDescription())
            .consultationType(request.getConsultationType())
            .meetingLocation(request.getMeetingLocation())
            .durationMinutes(request.getDurationMinutes())
            .status(AppointmentStatus.PENDING)
            .build();
            
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment created with ID: {}", appointment.getAppointmentId());
        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getCitizenAppointments(Long citizenId, int page, int size, AppointmentStatus status) {
        log.info("Getting appointments for citizen: {}", citizenId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Appointment> appointments;
        
        if (status != null) {
            appointments = appointmentRepository.findByCitizen_UserIdAndStatus(citizenId, status, pageable);
        } else {
            appointments = appointmentRepository.findByCitizen_UserId(citizenId, pageable);
        }
        
        return appointments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getLawyerAppointments(Long lawyerId, int page, int size, AppointmentStatus status) {
        log.info("Getting appointments for lawyer: {}", lawyerId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Appointment> appointments;
        
        if (status != null) {
            appointments = appointmentRepository.findByLawyer_LawyerIdAndStatus(lawyerId, status, pageable);
        } else {
            appointments = appointmentRepository.findByLawyer_LawyerId(lawyerId, pageable);
        }
        
        return appointments.map(this::mapToResponse);
    }

    public AppointmentResponse getAppointmentById(Long appointmentId, Long userId, boolean isLawyer) {
        log.info("Getting appointment {} for user {}", appointmentId, userId);
        
        Appointment appointment;
        if (isLawyer) {
            appointment = appointmentRepository.findByAppointmentIdAndLawyer_LawyerId(appointmentId, userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
        } else {
            appointment = appointmentRepository.findByAppointmentIdAndCitizen_UserId(appointmentId, userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
        }
        
        return mapToResponse(appointment);
    }

    public AppointmentResponse confirmAppointment(Long appointmentId, Long lawyerId, String message) {
        log.info("Lawyer {} confirming appointment {}", lawyerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndLawyer_LawyerId(appointmentId, lawyerId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
            
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppException(ErrorType.BAD_REQUEST, "Không thể xác nhận lịch hẹn với trạng thái hiện tại");
        }
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} confirmed successfully", appointmentId);
        return mapToResponse(appointment);
    }

    public AppointmentResponse rejectAppointment(Long appointmentId, Long lawyerId, String reason) {
        log.info("Lawyer {} rejecting appointment {}", lawyerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndLawyer_LawyerId(appointmentId, lawyerId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
            
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppException(ErrorType.BAD_REQUEST, "Không thể từ chối lịch hẹn với trạng thái hiện tại");
        }
        
        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.setRejectionReason(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} rejected successfully", appointmentId);
        return mapToResponse(appointment);
    }

    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId, boolean isLawyer, String reason) {
        log.info("User {} cancelling appointment {}", userId, appointmentId);
        
        Appointment appointment;
        if (isLawyer) {
            appointment = appointmentRepository.findByAppointmentIdAndLawyer_LawyerId(appointmentId, userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
        } else {
            appointment = appointmentRepository.findByAppointmentIdAndCitizen_UserId(appointmentId, userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
        }
        
        if (appointment.getStatus() == AppointmentStatus.CANCELLED || 
            appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException(ErrorType.BAD_REQUEST, "Không thể hủy lịch hẹn với trạng thái hiện tại");
        }
        
        // Validate 2-hour cancellation policy (only for citizens, lawyers can cancel anytime)
        if (!isLawyer) {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), 
                appointment.getAppointmentTime()
            );
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointmentDateTime);
            
            if (hoursUntilAppointment < 2 && hoursUntilAppointment >= 0) {
                throw new AppException(
                    ErrorType.BUSINESS_RULE_VIOLATION,
                    "Không thể tự hủy lịch hẹn vì đã quá thời hạn quy định (phải hủy trước 2 giờ). " +
                    "Vui lòng liên hệ trực tiếp để giải quyết."
                );
            }
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} cancelled successfully", appointmentId);
        return mapToResponse(appointment);
    }

    public AppointmentResponse rateAppointment(Long appointmentId, Long citizenId, Integer rating, String comment) {
        log.info("Citizen {} rating appointment {}", citizenId, appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndCitizen_UserId(appointmentId, citizenId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
            
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new AppException(ErrorType.BAD_REQUEST, "Chỉ có thể đánh giá lịch hẹn đã hoàn thành");
        }
        
        appointment.setRating(rating);
        appointment.setReviewComment(comment);
        appointment.setReviewedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} rated successfully", appointmentId);
        return mapToResponse(appointment);
    }

    public AppointmentResponse completeAppointment(Long appointmentId, Long lawyerId) {
        log.info("Lawyer {} completing appointment {}", lawyerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lịch hẹn"));
            
        // Security: Only the assigned lawyer can complete the appointment
        if (!appointment.getLawyer().getLawyerId().equals(lawyerId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Luật sư chỉ có thể hoàn thành lịch hẹn của mình");
        }
        
        // Business rule: Can only complete CONFIRMED appointments
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorType.BAD_REQUEST, "Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} completed successfully", appointmentId);
        return mapToResponse(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
            .appointmentId(appointment.getAppointmentId())
            .citizenId(appointment.getCitizen().getUserId())
            .citizenName(appointment.getCitizen().getFullName())
            .citizenPhone(appointment.getCitizen().getPhoneNumber())
            .lawyerId(appointment.getLawyer().getLawyerId())
            .lawyerName(appointment.getLawyer().getUser().getFullName())
            .lawyerPhone(appointment.getLawyer().getUser().getPhoneNumber())
            .appointmentDate(appointment.getAppointmentDate())
            .appointmentTime(appointment.getAppointmentTime())
            .durationMinutes(appointment.getDurationMinutes())
            .description(appointment.getDescription())
            .status(appointment.getStatus())
            .consultationType(appointment.getConsultationType())
            .meetingLocation(appointment.getMeetingLocation())
            .rejectionReason(appointment.getRejectionReason())
            .cancellationReason(appointment.getCancellationReason())
            .rating(appointment.getRating())
            .reviewComment(appointment.getReviewComment())
            .reviewedAt(appointment.getReviewedAt())
            .createdAt(appointment.getCreatedAt())
            .updatedAt(appointment.getUpdatedAt())
            .build();
    }
}
