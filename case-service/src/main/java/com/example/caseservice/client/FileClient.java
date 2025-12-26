package com.example.caseservice.client;

import com.example.fileservice.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileClient {

    @GrpcClient("file-service")
    private FileServiceGrpc.FileServiceBlockingStub fileServiceStub;

    public PresignedUrlResponse getPresignedUploadUrl(MultipartFile file, Long userId) {
        GenerateUploadUrlRequest request = GenerateUploadUrlRequest.newBuilder()
                .setFileName(file.getOriginalFilename())
                .setContentType(file.getContentType())
                .setFileSize(file.getSize())
                .setUploadedBy(userId.toString())
                .setBusinessType("CASE_DOCUMENT")
                .build();

        return fileServiceStub.generateUploadUrl(request);
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