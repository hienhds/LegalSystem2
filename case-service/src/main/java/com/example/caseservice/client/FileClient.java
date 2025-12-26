package com.example.caseservice.client;

import com.example.fileservice.grpc.FileServiceGrpc;
import com.example.fileservice.grpc.GenerateUploadUrlRequest;
import com.example.fileservice.grpc.PresignedUrlResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileClient {

    @GrpcClient("file-service")
    private FileServiceGrpc.FileServiceBlockingStub fileServiceStub;

    public String getPresignedUploadUrl(MultipartFile file, Long userId) {
        GenerateUploadUrlRequest request = GenerateUploadUrlRequest.newBuilder()
                .setFileName(file.getOriginalFilename())
                .setContentType(file.getContentType())
                .setFileSize(file.getSize())
                .setUploadedBy(userId.toString())
                .setBusinessType("CASE_DOCUMENT")
                .build();

        PresignedUrlResponse response = fileServiceStub.generateUploadUrl(request);

        if (response.getSuccess()) {
            return response.getPresignedUrl();
        } else {
            throw new RuntimeException("Lỗi từ File Service: " + response.getErrorMessage());
        }
    }
}