package com.example.backend.admin.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.document.dto.BulkDeleteRequest;
import com.example.backend.document.dto.DocumentStatsResponse;
import com.example.backend.document.dto.UpdateStatusRequest;
import com.example.backend.document.entity.LegalDocument;
import com.example.backend.document.entity.LegalDocument.DocumentStatus;
import com.example.backend.document.repository.LegalDocumentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDocumentController {

    private final LegalDocumentRepository documentRepository;

    @Value("${upload.documents.path:uploads/documents}")
    private String uploadPath;

    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Lấy thống kê văn bản
     */
    @GetMapping("/stats")
    public ResponseEntity<DocumentStatsResponse> getStats() {
        long total = documentRepository.count();
        long active = documentRepository.countByStatus(DocumentStatus.ACTIVE);
        long inactive = documentRepository.countByStatus(DocumentStatus.INACTIVE);

        DocumentStatsResponse stats = DocumentStatsResponse.builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy danh sách văn bản với filter và pagination
     */
    @GetMapping
    public ResponseEntity<Page<LegalDocument>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("Fetching documents: page={}, size={}, search={}, category={}, status={}, sort={}",
                page, size, search, category, status, sort);

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<LegalDocument> documents;

        // Apply filters
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasSearch && hasCategory && hasStatus) {
            documents = documentRepository.findByTitleContainingAndCategoryAndStatus(
                    search, category, DocumentStatus.valueOf(status), pageable);
        } else if (hasSearch && hasCategory) {
            documents = documentRepository.findByTitleContainingAndCategory(search, category, pageable);
        } else if (hasSearch && hasStatus) {
            documents = documentRepository.findByTitleContainingAndStatus(
                    search, DocumentStatus.valueOf(status), pageable);
        } else if (hasCategory && hasStatus) {
            documents = documentRepository.findByCategoryAndStatus(
                    category, DocumentStatus.valueOf(status), pageable);
        } else if (hasSearch) {
            documents = documentRepository.findByTitleContaining(search, pageable);
        } else if (hasCategory) {
            documents = documentRepository.findByCategory(category, pageable);
        } else if (hasStatus) {
            documents = documentRepository.findByStatusEquals(DocumentStatus.valueOf(status), pageable);
        } else {
            documents = documentRepository.findAll(pageable);
        }

        return ResponseEntity.ok(documents);
    }

    /**
     * Lấy chi tiết văn bản
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LegalDocument>> getDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(doc -> ResponseEntity.ok(ApiResponse.success(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Thêm văn bản mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LegalDocument>> createDocument(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "status", defaultValue = "ACTIVE") String status) {

        log.info("Creating new document: title={}, category={}, status={}", title, category, status);

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xml")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ chấp nhận file XML"));
            }

            // Save file
            String fileUrl = saveFile(file);

            // Create document
            LegalDocument document = LegalDocument.builder()
                    .title(title)
                    .category(category)
                    .fileUrl(fileUrl)
                    .status(DocumentStatus.valueOf(status))
                    .viewCount(0)
                    .build();

            LegalDocument saved = documentRepository.save(document);
            log.info("Document created successfully with ID: {}", saved.getDocumentId());

            return ResponseEntity.ok(ApiResponse.success(saved, "Thêm văn bản thành công"));

        } catch (IOException e) {
            log.error("Error saving file", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi lưu file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Trạng thái không hợp lệ"));
        }
    }

    /**
     * Cập nhật văn bản
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LegalDocument>> updateDocument(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "status", required = false) String status) {

        log.info("Updating document ID: {}", id);

        return documentRepository.findById(id)
                .map(existing -> {
                    try {
                        if (title != null) existing.setTitle(title);
                        if (category != null) existing.setCategory(category);
                        if (status != null) existing.setStatus(DocumentStatus.valueOf(status));

                        // Update file if provided
                        if (file != null && !file.isEmpty()) {
                            String originalFilename = file.getOriginalFilename();
                            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xml")) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<LegalDocument>>body(ApiResponse.error("Chỉ chấp nhận file XML"));
                            }
                            String fileUrl = saveFile(file);
                            existing.setFileUrl(fileUrl);
                        }

                        LegalDocument updated = documentRepository.save(existing);
                        log.info("Document updated successfully: {}", id);
                        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật văn bản thành công"));

                    } catch (IOException e) {
                        log.error("Error saving file", e);
                        return ResponseEntity.internalServerError()
                                .<ApiResponse<LegalDocument>>body(ApiResponse.error("Lỗi khi lưu file: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Đổi trạng thái văn bản
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LegalDocument>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {

        log.info("Updating status for document ID: {} to {}", id, request.getStatus());

        return documentRepository.findById(id)
                .map(doc -> {
                    doc.setStatus(request.getStatus());
                    LegalDocument updated = documentRepository.save(doc);
                    return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật trạng thái thành công"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa văn bản
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        log.info("Deleting document ID: {}", id);

        if (!documentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        documentRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa văn bản thành công"));
    }

    /**
     * Xóa nhiều văn bản
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        log.info("Bulk deleting documents: {}", request.getIds());

        List<Long> ids = request.getIds();
        documentRepository.deleteAllById(ids);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa " + ids.size() + " văn bản"));
    }

    /**
     * Lấy danh sách danh mục
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = documentRepository.findDistinctCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Upload file XML
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xml")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ chấp nhận file XML"));
            }

            String fileUrl = saveFile(file);
            return ResponseEntity.ok(ApiResponse.success(fileUrl, "Upload thành công"));

        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi upload file: " + e.getMessage()));
        }
    }

    /**
     * Xem nội dung XML
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<ApiResponse<String>> getDocumentContent(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(doc -> {
                    try {
                        // Read XML file content
                        String fileUrl = doc.getFileUrl();
                        String filePath = fileUrl.replace(baseUrl + "/", "");
                        Path path = Paths.get(filePath);
                        String content = Files.readString(path);
                        return ResponseEntity.ok(ApiResponse.success(content));
                    } catch (IOException e) {
                        log.error("Error reading file", e);
                        return ResponseEntity.internalServerError()
                                .<ApiResponse<String>>body(ApiResponse.error("Lỗi khi đọc file"));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Save file to disk
     */
    private String saveFile(MultipartFile file) throws IOException {
        // Create upload directory if not exists
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL
        return baseUrl + "/" + uploadPath + "/" + filename;
    }
}
