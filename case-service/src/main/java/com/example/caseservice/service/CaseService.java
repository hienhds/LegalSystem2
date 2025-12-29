package com.example.caseservice.service;

import com.example.caseservice.client.UserClient;
import com.example.caseservice.client.FileClient;
import com.example.caseservice.dto.*;
import com.example.caseservice.entity.*;
import com.example.caseservice.exception.AppException;
import com.example.caseservice.exception.ErrorType;
import com.example.caseservice.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CaseService {
    private final CaseRepository caseRepository;
    private final UserClient userClient;
    private final FileClient fileClient;
    private final CaseProgressUpdateRepository progressRepository;
    private final CaseDocumentRepository documentRepository;
    private final CaseProducer caseProducer;

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request, Long lawyerId) {
        Case legalCase = Case.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .clientId(request.getClientId())
                .lawyerId(lawyerId)
                .status(CaseStatus.IN_PROGRESS)
                .build();
        
        Case savedCase = caseRepository.save(legalCase);
        Map<Long, String> nameMap = fetchUserNames(Arrays.asList(lawyerId, request.getClientId()));
        CaseResponse response = mapToResponseWithNames(savedCase, nameMap);

        // Gửi event đồng bộ (đã fix lỗi serialize ở CaseProducer)
        syncToSearch(savedCase, nameMap, "CREATE");

        return response;
    }

    public Page<CaseResponse> searchMyCases(Long userId, String role, String keyword, Pageable pageable) {
        String kw = (keyword == null) ? "" : keyword.toLowerCase().trim();
        
        // 1. Nếu không có keyword, lấy từ DB và gắn tên người dùng
        if (kw.isEmpty()) {
            Page<Case> casesPage = "LAWYER".equalsIgnoreCase(role) 
                ? caseRepository.searchCasesForLawyer(userId, "", pageable)
                : caseRepository.searchCasesForClient(userId, "", pageable);
            
            if (casesPage.isEmpty()) return new PageImpl<>(new ArrayList<>(), pageable, 0);
            Map<Long, String> nameMap = fetchUserNames(extractIdsFromList(casesPage.getContent()));
            return casesPage.map(c -> mapToResponseWithNames(c, nameMap));
        }

        // 2. TÌM KIẾM NÂNG CAO (In-memory filtering)
        List<Case> allCases = "LAWYER".equalsIgnoreCase(role)
                ? caseRepository.findAllByLawyerId(userId)
                : caseRepository.findAllByClientId(userId);

        if (allCases.isEmpty()) return new PageImpl<>(new ArrayList<>(), pageable, 0);

        // Fetch thông tin người dùng từ user-service
        List<Long> userIds = extractIdsFromList(allCases);
        Map<Long, UserResponse> userDetailMap = fetchFullUserDetails(userIds);

        // Lọc trong bộ nhớ theo Tên/SĐT/Email/Tiêu đề
        List<CaseResponse> filtered = allCases.stream()
            .map(c -> {
                UserResponse lawyer = userDetailMap.get(c.getLawyerId());
                UserResponse client = userDetailMap.get(c.getClientId());
                
                boolean matchTitle = c.getTitle().toLowerCase().contains(kw);
                boolean matchLawyer = lawyer != null && matchesKeyword(lawyer, kw);
                boolean matchClient = client != null && matchesKeyword(client, kw);

                if (matchTitle || matchLawyer || matchClient) {
                    CaseResponse res = mapToResponse(c);
                    res.setLawyerName(lawyer != null ? lawyer.getFullName() : "Không xác định");
                    res.setClientName(client != null ? client.getFullName() : "Không xác định");
                    return res;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(CaseResponse::getCreatedAt).reversed())
            .collect(Collectors.toList());

        // Phân trang kết quả sau khi lọc
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        if (start > filtered.size()) return new PageImpl<>(new ArrayList<>(), pageable, filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    private boolean matchesKeyword(UserResponse user, String kw) {
        return (user.getFullName() != null && user.getFullName().toLowerCase().contains(kw)) ||
               (user.getPhoneNumber() != null && user.getPhoneNumber().contains(kw)) ||
               (user.getEmail() != null && user.getEmail().toLowerCase().contains(kw));
    }

    private Map<Long, UserResponse> fetchFullUserDetails(List<Long> ids) {
        Map<Long, UserResponse> map = new HashMap<>();
        try {
            var res = userClient.getUsersByIds(ids);
            if (res != null && res.getResult() != null) {
                // Dùng getId() khớp với DTO UserResponse trong case-service
                res.getResult().forEach(u -> map.put(u.getId(), u));
            }
        } catch (Exception e) {
            log.error("Lỗi gọi user-service (Có thể do 403 hoặc Service chưa chạy): {}", e.getMessage());
        }
        return map;
    }

    private Map<Long, String> fetchUserNames(List<Long> userIds) {
        Map<Long, String> nameMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) return nameMap;
        try {
            var userRes = userClient.getUsersByIds(userIds);
            if (userRes != null && userRes.getResult() != null) {
                userRes.getResult().forEach(u -> nameMap.put(u.getId(), u.getFullName()));
            }
        } catch (Exception e) {
            log.error("KHÔNG LẤY ĐƯỢC TÊN TỪ USER-SERVICE: {}", e.getMessage());
        }
        return nameMap;
    }

    private List<Long> extractIdsFromList(List<Case> list) {
        return list.stream().flatMap(c -> Stream.of(c.getLawyerId(), c.getClientId()))
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private CaseResponse mapToResponseWithNames(Case c, Map<Long, String> nameMap) {
        CaseResponse res = mapToResponse(c);
        res.setLawyerName(nameMap.getOrDefault(c.getLawyerId(), "Không xác định"));
        res.setClientName(nameMap.getOrDefault(c.getClientId(), "Không xác định"));
        return res;
    }

    private CaseResponse mapToResponse(Case c) {
        List<CaseUpdateResponse> updates = (c.getProgressUpdates() == null) ? List.of() :
                c.getProgressUpdates().stream().map(u -> CaseUpdateResponse.builder().id(u.getId()).updateDescription(u.getUpdateDescription()).updateDate(u.getUpdateDate()).build()).collect(Collectors.toList());
        List<CaseDocumentResponse> docs = (c.getDocuments() == null) ? List.of() :
                c.getDocuments().stream().map(d -> CaseDocumentResponse.builder().id(d.getId()).fileName(d.getFileName()).filePath(d.getFilePath()).fileType(d.getFileType()).build()).collect(Collectors.toList());
        return CaseResponse.builder().id(c.getId()).title(c.getTitle()).description(c.getDescription()).status(c.getStatus()).lawyerId(c.getLawyerId()).clientId(c.getClientId()).createdAt(c.getCreatedAt()).progressUpdates(updates).documents(docs).build();
    }

    private void syncToSearch(Case c, Map<Long, String> nameMap, String type) {
        try {
            CaseEvent event = CaseEvent.builder().eventType(type).id(c.getId()).title(c.getTitle()).description(c.getDescription()).status(c.getStatus()).lawyerId(c.getLawyerId()).lawyerName(nameMap.get(c.getLawyerId())).clientId(c.getClientId()).clientName(nameMap.get(c.getClientId())).createdAt(c.getCreatedAt()).build();
            caseProducer.sendCaseEvent(event);
        } catch (Exception e) { log.error("LỖI ĐỒNG BỘ: {}", e.getMessage()); }
    }

    // Các hàm còn lại (getById, update, delete, upload...) giữ nguyên logic
    public CaseResponse getCaseById(Long id, Long userId) {
        Case c = caseRepository.findById(id).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy vụ án #" + id));
        if (!Objects.equals(c.getLawyerId(), userId) && !Objects.equals(c.getClientId(), userId)) throw new AppException(ErrorType.FORBIDDEN, "Không có quyền");
        Map<Long, String> nameMap = fetchUserNames(Arrays.asList(c.getLawyerId(), c.getClientId()));
        return mapToResponseWithNames(c, nameMap);
    }

    @Transactional
    public CaseResponse updateCase(Long id, CreateCaseRequest request, Long lawyerId) {
        Case c = caseRepository.findById(id).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy vụ án #" + id));
        if (!Objects.equals(c.getLawyerId(), lawyerId)) throw new AppException(ErrorType.FORBIDDEN, "Không có quyền");
        c.setTitle(request.getTitle()); c.setDescription(request.getDescription()); c.setClientId(request.getClientId());
        Case updated = caseRepository.save(c);
        Map<Long, String> nameMap = fetchUserNames(Arrays.asList(updated.getLawyerId(), updated.getClientId()));
        syncToSearch(updated, nameMap, "UPDATE");
        return mapToResponseWithNames(updated, nameMap);
    }

    @Transactional
    public void deleteCase(Long caseId, Long userId) {
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tồn tại"));
        if (!Objects.equals(c.getLawyerId(), userId)) throw new AppException(ErrorType.FORBIDDEN, "Không có quyền");
        caseRepository.delete(c);
        caseProducer.sendCaseEvent(CaseEvent.builder().id(caseId).eventType("DELETE").build());
    }

    @Transactional
    public String uploadCaseDocument(Long caseId, Long userId, MultipartFile file) {
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tồn tại"));
        if (!Objects.equals(c.getLawyerId(), userId)) throw new AppException(ErrorType.FORBIDDEN, "Chỉ luật sư mới được thêm");
        String fileId = fileClient.uploadFileDirectly(file, userId, caseId.toString());
        CaseDocument doc = CaseDocument.builder().legalCase(c).fileName(file.getOriginalFilename()).filePath(fileId).fileType(file.getContentType()).uploadedAt(LocalDateTime.now()).build();
        documentRepository.save(doc);
        return "Thành công. FileId: " + fileId;
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
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tồn tại"));
        if (!Objects.equals(c.getLawyerId(), userId) && !Objects.equals(c.getClientId(), userId)) throw new AppException(ErrorType.FORBIDDEN, "Không có quyền");
        CaseDocument doc = documentRepository.findById(docId).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Tài liệu không tồn tại"));
        if (!Objects.equals(doc.getLegalCase().getId(), caseId)) throw new AppException(ErrorType.NOT_FOUND, "Không thuộc hồ sơ này");
        return doc;
    }

    @Transactional
    public void deleteDocument(Long caseId, Long docId, Long userId) {
        CaseDocument doc = validateAndGetDocument(caseId, docId, userId);
        if (!Objects.equals(doc.getLegalCase().getLawyerId(), userId)) throw new AppException(ErrorType.FORBIDDEN, "Không có quyền xóa");
        documentRepository.delete(doc);
    }

    @Transactional
    public CaseUpdateResponse updateProgress(Long caseId, UpdateProgressRequest request, Long lawyerId) {
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tồn tại"));
        if (!Objects.equals(c.getLawyerId(), lawyerId)) throw new AppException(ErrorType.FORBIDDEN, "Không có quyền");
        CaseProgressUpdate update = CaseProgressUpdate.builder().legalCase(c).updateDescription(request.getDescription()).updateDate(LocalDateTime.now()).build();
        progressRepository.save(update);
        if (request.getStatus() != null) c.setStatus(request.getStatus());
        caseRepository.save(c);
        return CaseUpdateResponse.builder().id(update.getId()).updateDescription(update.getUpdateDescription()).updateDate(update.getUpdateDate()).build();
    }
}