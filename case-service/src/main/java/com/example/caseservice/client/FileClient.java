package com.example.caseservice.client;

import com.example.fileservice.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class FileClient {

    @GrpcClient("file-service")
    private FileServiceGrpc.FileServiceBlockingStub fileServiceStub;

    public String uploadFileDirectly(MultipartFile file, Long userId, String caseId) {
        // Bước 1: Xin link upload từ File Service qua gRPC
        GenerateUploadUrlRequest request = GenerateUploadUrlRequest.newBuilder()
                .setFileName(file.getOriginalFilename())
                .setContentType(file.getContentType())
                .setFileSize(file.getSize())
                .setUploadedBy(userId.toString())
                .setBusinessType("CASE_DOCUMENT")
                .setBusinessId(caseId)
                .build();

        PresignedUrlResponse uploadRes = fileServiceStub.generateUploadUrl(request);
        
        if (!uploadRes.getSuccess()) {
            throw new RuntimeException("Không lấy được link upload: " + uploadRes.getErrorMessage());
        }

        // Bước 2: Đẩy dữ liệu file lên MinIO bằng HttpURLConnection (để tránh lỗi Header của RestTemplate)
        try {
            URL url = new URL(uploadRes.getPresignedUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            // Quan trọng: Content-Type phải khớp chính xác với lúc xin link
            connection.setRequestProperty("Content-Type", file.getContentType());
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(file.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                throw new RuntimeException("MinIO trả về lỗi: " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đẩy file lên MinIO: " + e.getMessage());
        }

        // Bước 3: Xác nhận upload xong để chuyển trạng thái sang COMPLETED ngay lập tức
        ConfirmUploadRequest confirmRequest = ConfirmUploadRequest.newBuilder()
                .setFileId(uploadRes.getFileId())
                .build();
        
        fileServiceStub.confirmUpload(confirmRequest);

        return uploadRes.getFileId();
    }

    public String getPresignedDownloadUrl(String fileId, String fileName) {
        GenerateDownloadUrlRequest request = GenerateDownloadUrlRequest.newBuilder()
                .setFileId(fileId)
                .setFileName(fileName)
                .build();

        PresignedUrlResponse response = fileServiceStub.generateDownloadUrl(request);
        if (response.getSuccess()) {
            return response.getPresignedUrl();
        }
        throw new RuntimeException("Lỗi lấy link download: " + response.getErrorMessage());
    }

    public String getPresignedPreviewUrl(String fileId) {
        FileIdRequest request = FileIdRequest.newBuilder()
                .setFileId(fileId)
                .build();

        PresignedUrlResponse response = fileServiceStub.generatePreviewUrl(request);
        if (response.getSuccess()) {
            return response.getPresignedUrl();
        }
        throw new RuntimeException("Lỗi lấy link preview: " + response.getErrorMessage());
    }
}