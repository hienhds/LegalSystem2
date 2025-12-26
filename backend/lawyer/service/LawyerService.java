package com.example.backend.lawyer.service;

import com.example.backend.appointment.repository.AppointmentRepository;
import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.common.service.EmailService;
import com.example.backend.lawyer.dto.request.FilterLawyerRequest;
import com.example.backend.lawyer.dto.request.LawyerRequest;
import com.example.backend.lawyer.dto.request.UpdateLawyerProfileRequest;
import com.example.backend.lawyer.dto.response.LawyerDetailResponse;
import com.example.backend.lawyer.dto.response.LawyerListResponse;
import com.example.backend.lawyer.dto.response.LawyerResponse;
import com.example.backend.lawyer.dto.response.LawyerReviewResponse;
import com.example.backend.lawyer.dto.response.LawyerStatsResponse;
import com.example.backend.lawyer.entity.*;
import com.example.backend.lawyer.repository.BarAssociationRepository;
import com.example.backend.lawyer.repository.LawyerRepository;
import com.example.backend.lawyer.repository.LawyerSpecializationRepository;
import com.example.backend.lawyer.repository.SpecializationRepository;
import com.example.backend.lawyer.spec.LawyerSpec;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.repository.RoleRepository;
import com.example.backend.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LawyerService {

    private final LawyerRepository lawyerRepository;
    private final BarAssociationRepository barAssociationRepository;
    private final SpecializationRepository specializationRepository;
    private final UserRepository userRepository;
    private final LawyerSpecializationRepository lawyerSpecializationRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AppointmentRepository appointmentRepository;




    public LawyerResponse requestUpgrade(LawyerRequest request, Long userId, String certificateUrl){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));

        if(user.getLawyer() != null){
            throw new AppException(ErrorType.CONFLICT, "ban da gui yeu cau hoac da la luat su");

        }

        BarAssociation barAssociation = barAssociationRepository.findById(request.getBarAssociationId())
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "lien doan luat su khong ton tai"));


        if(user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()){
            throw new AppException(ErrorType.BAD_REQUEST, "update avatar before give request");

        }

        if(user.getAddress() == null || user.getAddress().isBlank()){
            throw  new AppException(ErrorType.BAD_REQUEST, "update address before give request");
        }

        Lawyer lawyer = Lawyer.builder()
                .user(user)
                .barLicenseId(request.getBarLicenseId())
                .bio(request.getBio())
                .certificateImageUrl(certificateUrl)
                .officeAddress(request.getOfficeAddress())
                .yearsOfExp(request.getYearsOfExp())
                .barAssociation(barAssociation)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        Lawyer saved = lawyerRepository.save(lawyer);

        // Lưu Specializations
        request.getSpecializationIds().forEach(id -> {
            Specialization sp = specializationRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Specialization không tồn tại: " + id));

            LawyerSpecialization ls = new LawyerSpecialization();
            ls.setLawyer(saved);
            ls.setSpecialization(sp);


            lawyerSpecializationRepository.save(ls);
        });

        return LawyerResponse.builder()
                .lawyerId(saved.getLawyerId())
                .fullName(user.getFullName())
                .barLicenseId(saved.getBarLicenseId())
                .bio(saved.getBio())
                .certificateImageUrl(saved.getCertificateImageUrl())
                .officeAddress(saved.getOfficeAddress())
                .yearsOfExp(saved.getYearsOfExp())
                .barAssociationName(saved.getBarAssociation().getAssociationName())
                .verificationStatus(saved.getVerificationStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public Page<LawyerListResponse> getAllLawyers(FilterLawyerRequest request, int page, int size){
        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        var spec = LawyerSpec.filter(
            request.getStatus(), 
            request.getBarAssociationId(), 
            request.getKeyword(),
            request.getSpecializationIds(),
            request.getMinYearsOfExp(),
            request.getMaxYearsOfExp()
        );

        Page<Lawyer> lawyerPage = lawyerRepository.findAll(spec, pageable);

        Page<LawyerListResponse> responsePage = lawyerPage.map(lawyer -> {
            Double avgRating = appointmentRepository.getAverageRatingByLawyerId(lawyer.getLawyerId());
            Long reviewCount = appointmentRepository.countReviewsByLawyerId(lawyer.getLawyerId());
            
            return LawyerListResponse.builder()
                    .lawyerId(lawyer.getLawyerId())
                    .fullName(lawyer.getUser().getFullName())
                    .email(lawyer.getUser().getEmail())
                    .phoneNumber(lawyer.getUser().getPhoneNumber())
                    .avatarUrl(lawyer.getUser().getAvatarUrl())
                    .barLicenseId(lawyer.getBarLicenseId())
                    .barAssociationName(lawyer.getBarAssociation().getAssociationName())
                    .verificationStatus(lawyer.getVerificationStatus())
                    .certificateUrl(lawyer.getCertificateImageUrl())
                    .yearsOfExp(lawyer.getYearsOfExp())
                    .bio(lawyer.getBio())
                    .officeAddress(lawyer.getOfficeAddress())
                    .specializations(
                            lawyer.getSpecializations().stream()
                                    .map(ls -> ls.getSpecialization().getSpecName())
                                    .toList()
                    )
                    .createdAt(lawyer.getCreatedAt())
                    .averageRating(avgRating != null ? avgRating : 0.0)
                    .reviewCount(reviewCount != null ? reviewCount : 0L)
                    .build();
        });

        // Filter by minRating if specified (post-processing since rating is in appointments table)
        if (request.getMinRating() != null && request.getMinRating() > 0) {
            List<LawyerListResponse> filteredList = responsePage.getContent().stream()
                    .filter(lawyer -> lawyer.getAverageRating() >= request.getMinRating())
                    .toList();
            
            return new org.springframework.data.domain.PageImpl<>(
                    filteredList,
                    pageable,
                    filteredList.size()
            );
        }

        return responsePage;
    }

    // ============= GET DETAIL =============
    public LawyerDetailResponse getDetail(Long id) {
        Lawyer lawyer = lawyerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));

        return LawyerDetailResponse.builder()
                .lawyerId(lawyer.getLawyerId())
                .fullName(lawyer.getUser().getFullName())
                .email(lawyer.getUser().getEmail())
                .phoneNumber(lawyer.getUser().getPhoneNumber())
                .avatarUrl(lawyer.getUser().getAvatarUrl())
                .role(lawyer.getUser().getRoleName())

                .barLicenseId(lawyer.getBarLicenseId())
                .bio(lawyer.getBio())
                .certificateImageUrl(lawyer.getCertificateImageUrl())
                .officeAddress(lawyer.getOfficeAddress())
                .yearsOfExp(lawyer.getYearsOfExp())

                .barAssociationName(lawyer.getBarAssociation().getAssociationName())
                .verificationStatus(lawyer.getVerificationStatus())

                .specializationNames(
                        lawyer.getSpecializations().stream()
                                .map(ls -> ls.getSpecialization().getSpecName())
                                .toList()
                )

                .createdAt(lawyer.getCreatedAt())
                .updatedAt(lawyer.getUpdatedAt())
                .build();
    }

    public String updateStatus(Long id, VerificationStatus newStatus) {

        Lawyer lawyer = lawyerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));

        lawyer.setVerificationStatus(newStatus);
        if (newStatus == VerificationStatus.APPROVED) {
            lawyer.setVerifiedAt(LocalDateTime.now());
            
            // Add LAWYER role to user when verified
            User user = lawyer.getUser();
            Role lawyerRole = roleRepository.findByRoleName("LAWYER")
                    .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "LAWYER role not found"));
            
            // Check if user already has LAWYER role
            boolean hasLawyerRole = user.getUserRoles().stream()
                    .anyMatch(userRole -> "LAWYER".equals(userRole.getRole().getRoleName()));
            
            if (!hasLawyerRole) {
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(lawyerRole);
                userRoleRepository.save(userRole);
            }
        }

        lawyerRepository.save(lawyer);

        // gửi email
        String email = lawyer.getUser().getEmail();
        String subject = "Trạng thái yêu cầu nâng cấp luật sư";
        String body = "Xin chào " + lawyer.getUser().getFullName() + "\n\n"
                + "Yêu cầu nâng cấp luật sư của bạn đã được cập nhật thành: "
                + newStatus + ".\n\nCảm ơn bạn.";

        emailService.sendSimpleEmail(email, subject, body);

        return "Status updated to " + newStatus;
    }

    @Transactional
    public void deleteLawyer(Long id) {
        Lawyer lawyer = lawyerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));
        
        // Get user from lawyer
        User user = lawyer.getUser();
        if (user != null) {
            // Find and remove LAWYER role from user_roles
            Role lawyerRole = roleRepository.findByRoleName("LAWYER")
                    .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "LAWYER role not found"));
            
            UserRole userRole = userRoleRepository.findByUserAndRole(user, lawyerRole)
                    .orElse(null);
            
            if (userRole != null) {
                userRoleRepository.delete(userRole);
            }
        }
        
        // Delete lawyer profile
        lawyerRepository.delete(lawyer);
    }

    @Transactional
    public LawyerDetailResponse updateLawyerProfile(Long userId, UpdateLawyerProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));

        Lawyer lawyer = user.getLawyer();
        if (lawyer == null) {
            throw new AppException(ErrorType.NOT_FOUND, "Lawyer profile not found for this user");
        }

        // Update User fields
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            user.setAddress(request.getAddress());
        }
        userRepository.save(user);

        // Update Lawyer fields
        if (request.getBarLicenseId() != null && !request.getBarLicenseId().isBlank()) {
            lawyer.setBarLicenseId(request.getBarLicenseId());
        }
        if (request.getBio() != null) {
            lawyer.setBio(request.getBio());
        }
        if (request.getOfficeAddress() != null) {
            lawyer.setOfficeAddress(request.getOfficeAddress());
        }
        if (request.getYearsOfExp() != null) {
            lawyer.setYearsOfExp(request.getYearsOfExp());
        }
        if (request.getBarAssociationId() != null) {
            BarAssociation barAssociation = barAssociationRepository.findById(request.getBarAssociationId())
                    .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Bar Association not found"));
            lawyer.setBarAssociation(barAssociation);
        }

        lawyerRepository.save(lawyer);

        // Return updated detail
        return getDetail(lawyer.getLawyerId());
    }

    // ============= GET LAWYER BY ID WITH RATING =============
    public LawyerListResponse getLawyerById(Long lawyerId) {
        Lawyer lawyer = lawyerRepository.findByIdWithDetails(lawyerId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));

        Double avgRating = appointmentRepository.getAverageRatingByLawyerId(lawyerId);
        Long reviewCount = appointmentRepository.countReviewsByLawyerId(lawyerId);

        return LawyerListResponse.builder()
                .lawyerId(lawyer.getLawyerId())
                .fullName(lawyer.getUser().getFullName())
                .email(lawyer.getUser().getEmail())
                .phoneNumber(lawyer.getUser().getPhoneNumber())
                .avatarUrl(lawyer.getUser().getAvatarUrl())
                .barLicenseId(lawyer.getBarLicenseId())
                .barAssociationName(lawyer.getBarAssociation().getAssociationName())
                .verificationStatus(lawyer.getVerificationStatus())
                .certificateUrl(lawyer.getCertificateImageUrl())
                .yearsOfExp(lawyer.getYearsOfExp())
                .bio(lawyer.getBio())
                .officeAddress(lawyer.getOfficeAddress())
                .specializations(
                        lawyer.getSpecializations().stream()
                                .map(ls -> ls.getSpecialization().getSpecName())
                                .toList()
                )
                .createdAt(lawyer.getCreatedAt())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .build();
    }

    // ============= GET STATS =============
    public LawyerStatsResponse getStats() {
        Long totalLawyers = lawyerRepository.count();
        Long verifiedLawyers = lawyerRepository.countByVerificationStatus(VerificationStatus.APPROVED);
        
        Double overallRating = appointmentRepository.getOverallAverageRating();
        Long totalReviews = appointmentRepository.countTotalReviews();
        Long totalAppointments = appointmentRepository.countTotalAppointments();
        Long completedAppointments = appointmentRepository.countCompletedAppointments();
        
        Double satisfactionRate = 0.0;
        if (totalAppointments != null && totalAppointments > 0 && completedAppointments != null) {
            satisfactionRate = (completedAppointments.doubleValue() / totalAppointments.doubleValue()) * 100;
        }

        return LawyerStatsResponse.builder()
                .totalLawyers(totalLawyers != null ? totalLawyers : 0L)
                .activeLawyers(verifiedLawyers != null ? verifiedLawyers : 0L)
                .verifiedLawyers(verifiedLawyers != null ? verifiedLawyers : 0L)
                .averageRating(overallRating != null ? overallRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .satisfactionRate(satisfactionRate)
                .totalAppointments(totalAppointments != null ? totalAppointments : 0L)
                .completedAppointments(completedAppointments != null ? completedAppointments : 0L)
                .build();
    }

    // ============= GET LAWYER REVIEWS =============
    public Page<LawyerReviewResponse> getLawyerReviews(Long lawyerId, int page, int size) {
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("reviewedAt").descending());
        Page<com.example.backend.appointment.entity.Appointment> appointments = 
                appointmentRepository.findReviewsByLawyerId(lawyerId, pageable);
        
        return appointments.map(appointment -> LawyerReviewResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .citizenName(appointment.getCitizen().getFullName())
                .citizenAvatar(appointment.getCitizen().getAvatarUrl())
                .rating(appointment.getRating())
                .reviewComment(appointment.getReviewComment())
                .reviewedAt(appointment.getReviewedAt())
                .build());
    }

}
