package com.example.scheduleservice.appointment.service;

import com.example.scheduleservice.appointment.dto.AppointmentRequest;
import com.example.scheduleservice.appointment.dto.AppointmentResponse;
import com.example.scheduleservice.appointment.entity.Appointment;
import com.example.scheduleservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.scheduleservice.appointment.repository.AppointmentRepository;
import com.example.scheduleservice.common.dto.ApiResponse;
import com.example.scheduleservice.feign.UserInfoResponse;
import com.example.scheduleservice.feign.UserServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserServiceClient userServiceClient;
    private final RedisTemplate<String, String> redisTemplate;

    public AppointmentResponse createAppointment(AppointmentRequest request, Long citizenId) {
        log.info("Creating appointment for citizen {} with lawyer {}", citizenId, request.getLawyerId());
        
        // Distributed lock để tránh double booking
        String lockKey = "appointment:lock:" + request.getLawyerId() + ":" + 
                        request.getAppointmentDate() + ":" + request.getAppointmentTime();
        
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("Không thể acquire lock cho appointment slot");
            return null;
        }
        
        try {
            // Validate citizen exists via Feign
            ApiResponse<UserInfoResponse> citizenResponse = userServiceClient.getUserById(citizenId);
            if (citizenResponse == null || !citizenResponse.getSuccess() || citizenResponse.getData() == null) {
                log.warn("Không tìm thấy công dân với ID: {}", citizenId);
                return null;
            }
            UserInfoResponse citizenInfo = citizenResponse.getData();
            
            // Validate lawyer exists via Feign
            ApiResponse<UserInfoResponse> lawyerResponse = userServiceClient.getUserById(request.getLawyerId());
            if (lawyerResponse == null || !lawyerResponse.getSuccess() || lawyerResponse.getData() == null) {
                log.warn("Không tìm thấy luật sư với ID: {}", request.getLawyerId());
                return null;
            }
            UserInfoResponse lawyerInfo = lawyerResponse.getData();
            
            if (!Boolean.TRUE.equals(lawyerInfo.getIsLawyer())) {
                log.warn("User {} không phải luật sư", request.getLawyerId());
                return null;
            }
            
            // Check if lawyer is available at this time (Concurrency control)
            boolean hasConflict = appointmentRepository.existsByLawyerAndDateTime(
                request.getLawyerId(), 
                request.getAppointmentDate(), 
                request.getAppointmentTime()
            );
            
            if (hasConflict) {
                log.warn("Luật sư không có thời gian rảnh vào lúc này: {} {}", 
                    request.getAppointmentDate(), request.getAppointmentTime());
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
            return mapToResponse(appointment, citizenInfo, lawyerInfo);
            
        } finally {
            // Release lock
            redisTemplate.delete(lockKey);
        }
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
        
        return appointments.map(this::mapToResponseWithUserInfo);
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
        
        return appointments.map(this::mapToResponseWithUserInfo);
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
        
        return mapToResponseWithUserInfo(appointment);
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
        return mapToResponseWithUserInfo(appointment);
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
        return mapToResponseWithUserInfo(appointment);
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
        return mapToResponseWithUserInfo(appointment);
    }

    public AppointmentResponse completeAppointment(Long appointmentId, Long lawyerId) {
        log.info("Lawyer {} completing appointment {}", lawyerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentIdAndLawyerId(appointmentId, lawyerId).orElse(null);
        if (appointment == null) {
            log.warn("Không tìm thấy lịch hẹn với ID: {}", appointmentId);
            return null;
        }
        
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            log.warn("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
            return null;
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);
        
        log.info("Appointment {} completed successfully", appointmentId);
        return mapToResponseWithUserInfo(appointment);
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
        return mapToResponseWithUserInfo(appointment);
    }

    private AppointmentResponse mapToResponseWithUserInfo(Appointment appointment) {
        try {
            ApiResponse<UserInfoResponse> citizenResponse = userServiceClient.getUserById(appointment.getCitizenId());
            ApiResponse<UserInfoResponse> lawyerResponse = userServiceClient.getUserById(appointment.getLawyerId());
            
            UserInfoResponse citizenInfo = (citizenResponse != null && citizenResponse.getData() != null) 
                ? citizenResponse.getData() : new UserInfoResponse();
            UserInfoResponse lawyerInfo = (lawyerResponse != null && lawyerResponse.getData() != null) 
                ? lawyerResponse.getData() : new UserInfoResponse();
            
            return mapToResponse(appointment, citizenInfo, lawyerInfo);
        } catch (Exception e) {
            log.error("Error fetching user info: {}", e.getMessage());
            return mapToResponse(appointment, new UserInfoResponse(), new UserInfoResponse());
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment, UserInfoResponse citizen, UserInfoResponse lawyer) {
        return AppointmentResponse.builder()
            .appointmentId(appointment.getAppointmentId())
            .citizenId(appointment.getCitizenId())
            .citizenName(citizen.getFullName())
            .citizenPhone(citizen.getPhoneNumber())
            .lawyerId(appointment.getLawyerId())
            .lawyerName(lawyer.getFullName())
            .lawyerPhone(lawyer.getPhoneNumber())
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
