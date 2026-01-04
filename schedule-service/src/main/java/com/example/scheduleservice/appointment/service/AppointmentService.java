package com.example.scheduleservice.appointment.service;

import com.example.scheduleservice.appointment.dto.AppointmentRequest;
import com.example.scheduleservice.appointment.dto.AppointmentResponse;
import com.example.scheduleservice.appointment.entity.Appointment;
import com.example.scheduleservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.scheduleservice.appointment.repository.AppointmentRepository;
import com.example.scheduleservice.feign.UserServiceClient;
import com.example.scheduleservice.feign.UserInfoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserServiceClient userServiceClient;

    public AppointmentResponse createAppointment(AppointmentRequest request, Long citizenId) {
        log.info("Creating appointment for citizen {} with lawyer {}", citizenId, request.getLawyerId());
        
        // TODO: Validate citizen and lawyer exist via Feign client
        // For now, skip validation to allow service to start
            
        // Check if lawyer is available at this time
        boolean hasConflict = appointmentRepository.existsByLawyerAndDateTime(
            request.getLawyerId(), 
            request.getAppointmentDate(), 
            request.getAppointmentTime()
        );
        
        if (hasConflict) {
            log.warn("Luật sư không có thời gian rảnh vào lúc này: {} {}", request.getAppointmentDate(), request.getAppointmentTime());
            return null;
        }
        
        // Create appointment
        Appointment appointment = Appointment.builder()
            .citizenId(citizenId)
            .lawyerId(request.getLawyerId())
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
            appointments = appointmentRepository.findByCitizenIdAndStatus(citizenId, status, pageable);
        } else {
            appointments = appointmentRepository.findByCitizenId(citizenId, pageable);
        }
        
        return appointments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getLawyerAppointments(Long lawyerId, int page, int size, AppointmentStatus status) {
        log.info("Getting appointments for lawyer: {}", lawyerId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Appointment> appointments;
        
        if (status != null) {
            appointments = appointmentRepository.findByLawyerIdAndStatus(lawyerId, status, pageable);
        } else {
            appointments = appointmentRepository.findByLawyerId(lawyerId, pageable);
        }
        
        return appointments.map(this::mapToResponse);
    }

    public AppointmentResponse getAppointmentById(Long appointmentId, Long userId, boolean isLawyer) {
        log.info("Getting appointment {} for user {}", appointmentId, userId);
        
        Appointment appointment;
        if (isLawyer) {
            appointment = appointmentRepository.findByAppointmentIdAndLawyerId(appointmentId, userId).orElse(null);
        } else {
            appointment = appointmentRepository.findByAppointmentIdAndCitizenId(appointmentId, userId).orElse(null);
        }
        
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {} cho người dùng: {}", appointmentId, userId);
            return null;
        }
        
        return mapToResponse(appointment);
    }

    public AppointmentResponse confirmAppointment(Long appointmentId, Long lawyerId, String message) {
        log.info("Lawyer {} confirming appointment {}", lawyerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndLawyerId(appointmentId, lawyerId).orElse(null);
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {} cho luật sư: {}", appointmentId, lawyerId);
            return null;
        }
            
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            log.warn("Không thể xác nhận lịch hẹn với trạng thái hiện tại: {}", appointment.getStatus());
            return null;
        }
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} confirmed successfully", appointmentId);
        return mapToResponse(appointment);
    }

    public AppointmentResponse rejectAppointment(Long appointmentId, Long lawyerId, String reason) {
        log.info("Lawyer {} rejecting appointment {}", lawyerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndLawyerId(appointmentId, lawyerId).orElse(null);
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {} cho luật sư: {}", appointmentId, lawyerId);
            return null;
        }
            
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            log.warn("Không thể từ chối lịch hẹn với trạng thái hiện tại: {}", appointment.getStatus());
            return null;
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
            appointment = appointmentRepository.findByAppointmentIdAndLawyerId(appointmentId, userId).orElse(null);
        } else {
            appointment = appointmentRepository.findByAppointmentIdAndCitizenId(appointmentId, userId).orElse(null);
        }
        
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {} cho người dùng: {}", appointmentId, userId);
            return null;
        }
        
        if (appointment.getStatus() == AppointmentStatus.CANCELLED || 
            appointment.getStatus() == AppointmentStatus.COMPLETED) {
            log.warn("Không thể hủy lịch hẹn với trạng thái hiện tại: {}", appointment.getStatus());
            return null;
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
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndCitizenId(appointmentId, citizenId).orElse(null);
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {} cho công dân: {}", appointmentId, citizenId);
            return null;
        }
            
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            log.warn("Chỉ có thể đánh giá lịch hẹn đã hoàn thành");
            return null;
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
        
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {}", appointmentId);
            return null;
        }
            
        // Security: Only the assigned lawyer can complete the appointment
        if (!appointment.getLawyerId().equals(lawyerId)) {
            log.warn("Luật sư chỉ có thể hoàn thành lịch hẹn của mình");
            return null;
        }
        
        // Business rule: Can only complete CONFIRMED appointments
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            log.warn("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
            return null;
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} completed successfully", appointmentId);
        return mapToResponse(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        // TODO: Fetch user details from user-service via Feign client
        // For now, return basic info with IDs only
        return AppointmentResponse.builder()
            .appointmentId(appointment.getAppointmentId())
            .citizenId(appointment.getCitizenId())
            .citizenName("User-" + appointment.getCitizenId()) // TODO: Fetch from user-service
            .citizenPhone("N/A") // TODO: Fetch from user-service
            .lawyerId(appointment.getLawyerId())
            .lawyerName("Lawyer-" + appointment.getLawyerId()) // TODO: Fetch from user-service
            .lawyerPhone("N/A") // TODO: Fetch from user-service
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