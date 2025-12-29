package com.example.caseservice.service;

import com.example.caseservice.client.UserClient;
import com.example.caseservice.client.FileClient;
import com.example.caseservice.dto.*;
import com.example.caseservice.entity.*;
import com.example.caseservice.exception.AppException;
import com.example.caseservice.exception.ErrorType;
import com.example.caseservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseRepository caseRepository;
    private final UserClient userClient;
    private final FileClient fileClient;
    private final CaseProgressUpdateRepository progressRepository;
    private final CaseDocumentRepository documentRepository;

    public CaseResponse createCase(CreateCaseRequest request, Long lawyerId) {
        Case legalCase = Case.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .clientId(request.getClientId())
                .lawyerId(lawyerId)
                .status(CaseStatus.IN_PROGRESS)
                .build();
        return mapToResponse(caseRepository.save(legalCase));
    }

    public CaseResponse getCaseById(Long id) {
        Case c = caseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy vụ án #" + id));
        return mapToResponse(c);
    }

    public Page<CaseResponse> searchMyCases(Long userId, String role, String keyword, Pageable pageable) {
        Page<Case> casesPage;
        boolean isLawyer = "LAWYER".equalsIgnoreCase(role);
        String searchKeyword = (keyword == null) ? "" : keyword.trim();

        if (isLawyer) {
            casesPage = caseRepository.searchCasesForLawyer(userId, searchKeyword, pageable);
        } else {
            casesPage = caseRepository.searchCasesForClient(userId, searchKeyword, pageable);
        }

        if (casesPage.isEmpty()) return Page.empty();

        Set<Long> userIds = casesPage.getContent().stream()
                .flatMap(c -> Stream.of(c.getLawyerId(), c.getClientId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> userNameMap = new HashMap<>();
        try {
            var userRes = userClient.getUsersByIds(new ArrayList<>(userIds));
            if (userRes != null && userRes.getResult() != null) {
                userRes.getResult().forEach(u -> userNameMap.put(u.getId(), u.getFullName()));
            }
        } catch (Exception e) {}

        List<CaseResponse> responses = casesPage.getContent().stream()
                .map(c -> mapToResponseWithNames(c, userNameMap))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, casesPage.getTotalElements());
    }

    @Transactional
    public String uploadCaseDocument(Long caseId, Long userId, MultipartFile file) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Vụ án không tồn tại"));

        if (!Objects.equals(c.getLawyerId(), userId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ luật sư phụ trách mới được thêm tài liệu");
        }

        // Gọi quy trình tự động: Xin link -> Tự Upload -> Tự Confirm status
        String fileId = fileClient.uploadFileDirectly(file, userId, caseId.toString());

        CaseDocument doc = CaseDocument.builder()
                .legalCase(c)
                .fileName(file.getOriginalFilename())
                .filePath(fileId)
                .fileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .build();

        documentRepository.save(doc);
        return "Tải lên thành công. FileId: " + fileId;
    }

    public String getDownloadUrl(Long caseId, Long docId, Long userId) {
        CaseDocument doc = validateAndGetDocument(caseId, docId, userId);
        return fileClient.getPresignedDownloadUrl(doc.getFilePath(), doc.getFileName());
    }

    public String getViewUrl(Long caseId, Long docId, Long userId) {
        CaseDocument doc = validateAndGetDocument(caseId, docId, userId);
        return fileClient.getPresignedPreviewUrl(doc.getFilePath());
    }

    private CaseDocument validateAndGetDocument(Long caseId, Long docId, Long userId) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Vụ án không tồn tại"));

        if (!Objects.equals(c.getLawyerId(), userId) && !Objects.equals(c.getClientId(), userId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không có quyền truy cập tài liệu này");
        }

        return documentRepository.findById(docId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Tài liệu không tồn tại"));
    }

    @Transactional
    public void deleteDocument(Long caseId, Long docId, Long userId) {
        CaseDocument doc = validateAndGetDocument(caseId, docId, userId);
        if (!Objects.equals(doc.getLegalCase().getLawyerId(), userId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ luật sư phụ trách mới được xóa tài liệu");
        }
        documentRepository.delete(doc);
    }

    @Transactional
    public void deleteCase(Long caseId, Long userId) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Vụ án không tồn tại"));

        if (!Objects.equals(c.getLawyerId(), userId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không có quyền xóa vụ án này");
        }
        caseRepository.delete(c);
    }

    @Transactional
    public CaseUpdateResponse updateProgress(Long caseId, UpdateProgressRequest request, Long lawyerId) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Vụ án không tồn tại"));

        if (!Objects.equals(c.getLawyerId(), lawyerId)) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không có quyền cập nhật vụ án này");
        }

        CaseProgressUpdate update = CaseProgressUpdate.builder()
                .legalCase(c)
                .updateDescription(request.getDescription())
                .updateDate(LocalDateTime.now())
                .build();

        progressRepository.save(update);
        if (request.getStatus() != null) c.setStatus(request.getStatus());
        caseRepository.save(c);

        return CaseUpdateResponse.builder()
                .id(update.getId())
                .updateDescription(update.getUpdateDescription())
                .updateDate(update.getUpdateDate())
                .build();
    }

    private CaseResponse mapToResponseWithNames(Case c, Map<Long, String> nameMap) {
        CaseResponse res = mapToResponse(c);
        res.setLawyerName(nameMap.getOrDefault(c.getLawyerId(), "Unknown"));
        res.setClientName(nameMap.getOrDefault(c.getClientId(), "Unknown"));
        return res;
    }

    private CaseResponse mapToResponse(Case c) {
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
                .createdAt(c.getCreatedAt())
                .progressUpdates(updates)
                .documents(docs)
                .build();
    }
}