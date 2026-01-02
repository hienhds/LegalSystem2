package com.example.userservice.lawyer.service;

import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.common.service.EmailService;
import com.example.userservice.lawyer.dto.request.FilterLawyerRequest;
import com.example.userservice.lawyer.dto.request.LawyerRequest;
import com.example.userservice.lawyer.dto.response.LawyerDetailResponse;
import com.example.userservice.lawyer.dto.response.LawyerListResponse;
import com.example.userservice.lawyer.dto.response.LawyerResponse;
import com.example.userservice.lawyer.dto.response.LawyerReviewResponse;
import com.example.userservice.lawyer.dto.response.LawyerStatsResponse;
import com.example.userservice.lawyer.entity.*;
import com.example.userservice.lawyer.repository.BarAssociationRepository;
import com.example.userservice.lawyer.repository.LawyerRepository;
import com.example.userservice.lawyer.repository.LawyerSpecializationRepository;
import com.example.userservice.lawyer.repository.SpecializationRepository;
import com.example.userservice.lawyer.spec.LawyerSpec;
import com.example.userservice.user.entity.Role;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.entity.UserRole;
import com.example.userservice.user.repository.RoleRepository;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            request.getMaxYearsOfExp(),
            request.getMinRating()
        );

        Page<Lawyer> lawyerPage = lawyerRepository.findAll(spec, pageable);

        return lawyerPage.map(lawyer ->
                LawyerListResponse.builder()
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
                        .specializations(lawyer.getSpecializations().stream()
                                .map(ls -> ls.getSpecialization().getSpecName())
                                .toList())
                        .createdAt(lawyer.getCreatedAt())
                        .build()
        );

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
                .address(lawyer.getUser().getAddress())

                .barLicenseId(lawyer.getBarLicenseId())
                .bio(lawyer.getBio())
                .certificateImageUrl(lawyer.getCertificateImageUrl())
                .officeAddress(lawyer.getOfficeAddress())
                .yearsOfExp(lawyer.getYearsOfExp())

                .barAssociationId(lawyer.getBarAssociation().getBarAssociationId())
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

    public String updateStatus(Long lawyerId, VerificationStatus newStatus, Long adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Admin not found"));

        boolean isAdmin = admin.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null &&
                        "ADMIN".equalsIgnoreCase(ur.getRole().getRoleName()));
        if (!isAdmin) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không có quyền thực hiện hành động này");
        }

        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));

        User user = lawyer.getUser();
        if (user == null) {
            throw new AppException(ErrorType.NOT_FOUND, "Owner user of lawyer not found");
        }

        if (lawyer.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new AppException(ErrorType.BAD_REQUEST,
                    "Chỉ có thể cập nhật yêu cầu ở trạng thái PENDING. Hiện tại: " + lawyer.getVerificationStatus());
        }

        lawyer.setVerificationStatus(newStatus);

        String adminName = admin.getFullName() != null ? admin.getFullName() : "Admin";

        if (newStatus == VerificationStatus.APPROVED) {
            // set user flag + verifiedAt
            user.setLawyer(true);
            lawyer.setVerifiedAt(LocalDateTime.now());

            // ensure "LAWYER" role exists
            Role lawyerRole = roleRepository.findByRoleNameIgnoreCase("LAWYER")
                    .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Role 'LAWYER' not found in system"));

            // check duplicate role (use in-memory check first)
            boolean hasLawyerRole = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole() != null && "LAWYER".equalsIgnoreCase(ur.getRole().getRoleName()));

            if (!hasLawyerRole) {
                // create and persist UserRole
                UserRole newUserRole = new UserRole();
                newUserRole.setUser(user);
                newUserRole.setRole(lawyerRole);
                userRoleRepository.save(newUserRole);

                // maintain bi-directional relation in memory
                user.getUserRoles().add(newUserRole);
            }

            // persist user change (is_lawyer)
            userRepository.save(user);

        } else if (newStatus == VerificationStatus.REJECTED) {
            // make sure user is NOT marked as lawyer
            user.setLawyer(false);
            // optionally you may want to keep certificateImageUrl or set a rejection reason on Lawyer entity
            userRepository.save(user);
        } else {
            // if other statuses are defined, block them (keep logic strict)
            throw new AppException(ErrorType.BAD_REQUEST, "Trạng thái không hợp lệ để cập nhật: " + newStatus);
        }

        // 7. Persist lawyer after changes
        lawyerRepository.save(lawyer);

        // 8. Send email notification (both cases)
        String subject = "Thông báo cập nhật trạng thái yêu cầu nâng cấp luật sư";
        StringBuilder body = new StringBuilder();
        body.append("Xin chào ").append(user.getFullName()).append(",\n\n");
        body.append("Yêu cầu nâng cấp luật sư của bạn (ID: ").append(lawyer.getLawyerId()).append(") ");
        body.append("đã được xử lý bởi ").append(adminName).append(" với kết quả: ").append(newStatus).append(".\n\n");

        if (newStatus == VerificationStatus.APPROVED) {
            body.append("Chúc mừng — bạn bây giờ đã được cấp quyền LAWYER trong hệ thống. ");
            body.append("Nếu cần thêm thông tin vui lòng liên hệ quản trị viên.\n\n");
        } else { // REJECTED
            body.append("Rất tiếc, yêu cầu của bạn đã bị từ chối. ");
            body.append("Vui lòng kiểm tra lại hồ sơ, bổ sung thông tin và thử gửi lại nếu cần.\n\n");
        }

        body.append("Trân trọng,\nHệ thống quản trị");

        try {
            emailService.sendSimpleEmail(user.getEmail(), subject, body.toString());
        } catch (Exception ex) {

        }

        return "Updated lawyerId=" + lawyerId + " to status=" + newStatus;
    }
    
    // Thêm các method bị thiếu
    public LawyerListResponse getLawyerById(Long id) {
        Lawyer lawyer = lawyerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Luật sư không tồn tại"));
        
        return mapToLawyerListResponse(lawyer);
    }
    
    public LawyerDetailResponse getLawyerDetails(Long id) {
        return getDetail(id);
    }
    
    public Page<LawyerListResponse> getAllLawyers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Lawyer> lawyers = lawyerRepository.findAll(pageable);
        return lawyers.map(this::mapToLawyerListResponse);
    }
    
    public LawyerStatsResponse getStats() {
        long totalLawyers = lawyerRepository.count();
        long totalApproved = lawyerRepository.countByVerificationStatus(VerificationStatus.APPROVED);
        long totalPending = lawyerRepository.countByVerificationStatus(VerificationStatus.PENDING);
        
        return LawyerStatsResponse.builder()
                .totalLawyers(totalLawyers)
                .activeLawyers(totalApproved)
                .verifiedLawyers(totalApproved)
                .averageRating(0.0)
                .totalReviews(0L)
                .satisfactionRate(0.0)
                .totalAppointments(0L)
                .completedAppointments(0L)
                .build();
    }
    
    public Page<com.example.userservice.lawyer.dto.response.LawyerReviewResponse> getLawyerReviews(Long lawyerId, int page, int size) {
        // This requires AppointmentRepository which is in appointment package
        // For now, return empty page
        return Page.empty();
    }
    
    public LawyerDetailResponse updateLawyerProfile(Long userId, com.example.userservice.lawyer.dto.request.UpdateLawyerProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));
        
        Lawyer lawyer = user.getLawyer();
        if (lawyer == null) {
            throw new AppException(ErrorType.NOT_FOUND, "Bạn chưa đăng ký là luật sư");
        }
        
        // Update lawyer profile
        if (request.getBio() != null) {
            lawyer.setBio(request.getBio());
        }
        if (request.getOfficeAddress() != null) {
            lawyer.setOfficeAddress(request.getOfficeAddress());
        }
        if (request.getYearsOfExp() != null) {
            lawyer.setYearsOfExp(request.getYearsOfExp());
        }
        
        lawyerRepository.save(lawyer);
        
        return getDetail(lawyer.getLawyerId());
    }
    
    public void deleteLawyer(Long lawyerId) {
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Luật sư không tồn tại"));
        
        // Remove LAWYER role from user
        User user = lawyer.getUser();
        Role lawyerRole = roleRepository.findByRoleName("LAWYER")
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Role LAWYER not found"));
        
        // Delete user role manually
        user.getUserRoles().removeIf(ur -> ur.getRole().equals(lawyerRole));
        userRepository.save(user);
        
        // Delete lawyer record
        lawyerRepository.delete(lawyer);
    }
    
    private LawyerListResponse mapToLawyerListResponse(Lawyer lawyer) {
        return LawyerListResponse.builder()
                .lawyerId(lawyer.getLawyerId())
                .fullName(lawyer.getUser().getFullName())
                .email(lawyer.getUser().getEmail())
                .phoneNumber(lawyer.getUser().getPhoneNumber())
                .avatarUrl(lawyer.getUser().getAvatarUrl())
                .barAssociationName(lawyer.getBarAssociation().getAssociationName())
                .barLicenseId(lawyer.getBarLicenseId())
                .certificateUrl(lawyer.getCertificateImageUrl())
                .verificationStatus(lawyer.getVerificationStatus())
                .yearsOfExp(lawyer.getYearsOfExp())
                .bio(lawyer.getBio())
                .specializations(lawyer.getSpecializations().stream()
                        .map(ls -> ls.getSpecialization().getSpecName())
                        .toList())
                .createdAt(lawyer.getCreatedAt())
                .build();
    }

}
