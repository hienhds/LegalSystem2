package com.example.caseservice.service;

import com.example.caseservice.client.UserClient;
import com.example.caseservice.client.FileClient;
import com.example.caseservice.dto.*;
import com.example.caseservice.entity.Case;
import com.example.caseservice.entity.CaseStatus;
import com.example.caseservice.entity.CaseDocument;
import com.example.caseservice.entity.CaseProgressUpdate;
import com.example.caseservice.repository.CaseRepository;
import com.example.caseservice.repository.CaseDocumentRepository;
import com.example.caseservice.repository.CaseProgressUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseRepository caseRepository;
    private final UserClient userClient;
    private final FileClient fileClient;
    private final CaseProgressUpdateRepository progressRepository;
    private final CaseDocumentRepository documentRepository;

    // 1. TẠO VỤ ÁN
    public CaseResponse createCase(CreateCaseRequest request, Long lawyerId) {
        Case legalCase = Case.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .clientId(request.getClientId())
                .lawyerId(lawyerId)
                .status(CaseStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Case savedCase = caseRepository.save(legalCase);
        return mapToResponse(savedCase);
    }

    // 2. LẤY CHI TIẾT
    public CaseResponse getCaseById(Long id) {
        Case c = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vụ án"));
        return mapToResponse(c);
    }

    // 3. TÌM KIẾM VÀ PHÂN TRANG (MỚI)
    public Page<CaseResponse> searchMyCases(Long userId, String keyword, Pageable pageable) {
        Page<Case> casesPage = caseRepository.searchMyCases(userId, keyword, pageable);
        return casesPage.map(this::mapToResponse);
    }

    // 4. CẬP NHẬT TIẾN ĐỘ
    @Transactional
    public CaseUpdateResponse updateProgress(Long caseId, UpdateProgressRequest request, Long lawyerId) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!legalCase.getLawyerId().equals(lawyerId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật vụ án này");
        }

        CaseProgressUpdate update = CaseProgressUpdate.builder()
                .legalCase(legalCase)
                .updateDescription(request.getDescription())
                .updateDate(LocalDateTime.now())
                .build();

        progressRepository.save(update);

        if (request.getStatus() != null) {
            legalCase.setStatus(request.getStatus());
        }
        legalCase.setUpdatedAt(LocalDateTime.now());
        caseRepository.save(legalCase);

        return CaseUpdateResponse.builder()
                .id(update.getId())
                .updateDescription(update.getUpdateDescription())
                .updateDate(update.getUpdateDate())
                .build();
    }

    // 5. UPLOAD TÀI LIỆU
    @Transactional
    public String uploadCaseDocument(Long caseId, Long userId, MultipartFile file) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Vụ án không tồn tại"));

        if (!legalCase.getLawyerId().equals(userId)) {
            throw new RuntimeException("Chỉ luật sư phụ trách mới được thêm tài liệu");
        }

        try {
            String fileUrl = fileClient.uploadFile(file);
            CaseDocument doc = CaseDocument.builder()
                    .legalCase(legalCase)
                    .fileName(file.getOriginalFilename())
                    .filePath(fileUrl)
                    .fileType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            documentRepository.save(doc);
            return fileUrl;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload: " + e.getMessage());
        }
    }

    // 6. XÓA TÀI LIỆU (MỚI)
    @Transactional
    public void deleteDocument(Long caseId, Long docId, Long userId) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Vụ án không tồn tại"));

        if (!legalCase.getLawyerId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa tài liệu của vụ án này");
        }

        CaseDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));

        documentRepository.delete(doc);
    }

    // 7. XÓA VỤ ÁN (MỚI)
    @Transactional
    public void deleteCase(Long caseId, Long userId) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Vụ án không tồn tại"));

        if (!legalCase.getLawyerId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa vụ án này");
        }

        // CascadeType.ALL trong Entity sẽ tự động xóa Documents và ProgressUpdates
        caseRepository.delete(legalCase);
    }

    // MAPPING
    private CaseResponse mapToResponse(Case c) {
        String lawyerName = "Unknown";
        String clientName = "Unknown";

        try {
            var lawyerRes = userClient.getUserById(c.getLawyerId());
            if (lawyerRes != null && lawyerRes.getResult() != null) lawyerName = lawyerRes.getResult().getFullName();

            var clientRes = userClient.getUserById(c.getClientId());
            if (clientRes != null && clientRes.getResult() != null) clientName = clientRes.getResult().getFullName();
        } catch (Exception e) {}

        List<CaseUpdateResponse> updates = (c.getProgressUpdates() == null) ? List.of() :
                c.getProgressUpdates().stream()
                        .map(u -> CaseUpdateResponse.builder()
                                .id(u.getId())
                                .updateDescription(u.getUpdateDescription())
                                .updateDate(u.getUpdateDate())
                                .build())
                        .collect(Collectors.toList());

        List<CaseDocumentResponse> docs = (c.getDocuments() == null) ? List.of() :
                c.getDocuments().stream()
                        .map(d -> CaseDocumentResponse.builder()
                                .id(d.getId())
                                .fileName(d.getFileName())
                                .filePath(d.getFilePath())
                                .fileType(d.getFileType())
                                .build())
                        .collect(Collectors.toList());

        return CaseResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus())
                .lawyerId(c.getLawyerId())
                .clientId(c.getClientId())
                .lawyerName(lawyerName)
                .clientName(clientName)
                .createdAt(c.getCreatedAt())
                .progressUpdates(updates)
                .documents(docs)
                .build();
    }
}