package com.example.caseservice.service;

import com.example.caseservice.client.UserClient;
import com.example.caseservice.dto.*;
import com.example.caseservice.entity.Case;
import com.example.caseservice.entity.CaseStatus;
import com.example.caseservice.entity.CaseDocument;       // THÊM DÒNG NÀY
import com.example.caseservice.entity.CaseProgressUpdate; // THÊM DÒNG NÀY
import com.example.caseservice.repository.CaseRepository;
import com.example.caseservice.repository.CaseDocumentRepository;       // THÊM DÒNG NÀY
import com.example.caseservice.repository.CaseProgressUpdateRepository; // THÊM DÒNG NÀY
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseRepository caseRepository;
    private final UserClient userClient; // Tiêm Feign Client vào
    private final CaseProgressUpdateRepository progressRepository;
    private final CaseDocumentRepository documentRepository;


    public CaseResponse createCase(CreateCaseRequest request, Long lawyerId) {
        Case legalCase = Case.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .clientId(request.getClientId())
                .lawyerId(lawyerId) // Lấy từ token người dùng đang đăng nhập
                .status(CaseStatus.PENDING)
                .build();

        Case savedCase = caseRepository.save(legalCase);
        return mapToResponse(savedCase);
    }

    public List<CaseResponse> getMyCases(Long userId) {
        // Tìm cả vụ án mà user đó là luật sư hoặc khách hàng
        List<Case> cases = caseRepository.findByLawyerId(userId);
        if (cases.isEmpty()) {
            cases = caseRepository.findByClientId(userId);
        }
        return cases.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    // Hàm thêm tiến độ vụ án
    public CaseUpdateResponse updateProgress(Long caseId, UpdateProgressRequest request, Long lawyerId) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Kiểm tra xem người cập nhật có đúng là luật sư của vụ án này không
        if (!legalCase.getLawyerId().equals(lawyerId)) {
            throw new RuntimeException("You are not authorized to update this case");
        }

        CaseProgressUpdate update = CaseProgressUpdate.builder()
                .legalCase(legalCase)
                .updateDescription(request.getDescription())
                .build();

        progressRepository.save(update);

        return CaseUpdateResponse.builder()
                .id(update.getId())
                .updateDescription(update.getUpdateDescription())
                .updateDate(update.getUpdateDate())
                .build();
    }

    private CaseResponse mapToResponse(Case c) {
        // Dùng Feign Client để lấy tên từ user-service
        String lawyerName = "Unknown";
        String clientName = "Unknown";

        try {
            var lawyer = userClient.getUserById(c.getLawyerId()).getResult();
            lawyerName = lawyer.getFullName();

            var client = userClient.getUserById(c.getClientId()).getResult();
            clientName = client.getFullName();
        } catch (Exception e) {
            // Nếu user-service sập, vẫn trả về thông tin vụ án kèm tên "Unknown"
        }

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
                .build();
    }
    public void addDocument(Long caseId, String fileName, String filePath, String fileType) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        CaseDocument document = CaseDocument.builder()
                .legalCase(legalCase)
                .fileName(fileName)
                .filePath(filePath)
                .fileType(fileType)
                .build();

        documentRepository.save(document);
    }
}