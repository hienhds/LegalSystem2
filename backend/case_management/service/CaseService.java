package com.example.backend.case_management.service;

import com.example.backend.case_management.dto.CaseResponse;
import com.example.backend.case_management.dto.CaseUpdateResponse;
import com.example.backend.case_management.dto.CreateCaseRequest;
import com.example.backend.case_management.dto.UpdateProgressRequest;
import com.example.backend.case_management.entity.Case;
import com.example.backend.case_management.entity.CaseDocument;
import com.example.backend.case_management.entity.CaseProgressUpdate;
import com.example.backend.case_management.entity.CaseStatus;
import com.example.backend.case_management.repository.CaseRepository;
import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.common.service.UploadImageService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final UploadImageService uploadService; // Inject service upload

    // --- CODE CŨ: TẠO VÀ LẤY CHI TIẾT ---
    public CaseResponse createCase(Long clientId, CreateCaseRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Client not found"));

        User lawyer = userRepository.findById(request.getLawyerId())
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Lawyer not found"));

        if (lawyer.getLawyer() == null) {
            throw new AppException(ErrorType.BAD_REQUEST, "User is not a lawyer");
        }

        Case newCase = Case.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .client(client)
                .lawyer(lawyer)
                .status(CaseStatus.IN_PROGRESS)
                .build();

        Case savedCase = caseRepository.save(newCase);
        return CaseResponse.from(savedCase);
    }

    public CaseResponse getCaseDetail(Long caseId) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Case not found"));
        return CaseResponse.from(c);
    }

    // --- CODE MỚI: CẬP NHẬT TIẾN ĐỘ ---
    public CaseUpdateResponse addCaseUpdate(Long caseId, Long userId, UpdateProgressRequest request) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy vụ án"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));

        // Chỉ luật sư phụ trách mới được update
        if (!c.getLawyer().getUserId().equals(userId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không phải luật sư phụ trách vụ án này");
        }

        CaseProgressUpdate update = CaseProgressUpdate.builder()
                .legalCase(c)
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        c.getUpdates().add(update);

        if (request.getStatus() != null) {
            c.setStatus(request.getStatus());
        }

        caseRepository.save(c);

        return CaseUpdateResponse.from(update);
    }

    // --- CODE MỚI: UPLOAD TÀI LIỆU ---
    public String uploadCaseDocument(Long caseId, Long userId, MultipartFile file) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Case not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));

        // Check quyền: Client hoặc Lawyer của vụ án mới được up
        boolean isClient = c.getClient().getUserId().equals(userId);
        boolean isLawyer = c.getLawyer().getUserId().equals(userId);

        if (!isClient && !isLawyer) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không có quyền upload tài liệu cho vụ án này");
        }

        // Gọi hàm uploadFile (đã sửa ở UploadImageService)
        String fileUrl = uploadService.uploadFile(userId, file, "case_docs");

        CaseDocument doc = CaseDocument.builder()
                .legalCase(c)
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .uploadedBy(user)
                .build();

        c.getDocuments().add(doc);
        caseRepository.save(c);

        return fileUrl;
    }
}