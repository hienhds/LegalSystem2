package com.example.chatservice.grpc;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class FileServiceGrpcClient {
    @GrpcClient("file-service")
    private  FileServiceGrpc.FileServiceBlockingStub fileStub;

    public PresignedUrlResponse generateUploadUrl(
            String fileName,
            String contentType,
            long fileSize,
            String businessType,
            String businessId,
            Long uploadedBy
    ) {
        GenerateUploadUrlRequest request =
                GenerateUploadUrlRequest.newBuilder()
                        .setFileName(fileName)
                        .setContentType(contentType)
                        .setFileSize(fileSize)
                        .setBusinessType(businessType) // GROUP_AVATAR
                        .setBusinessId(businessId)     // conversationId
                        .setUploadedBy(uploadedBy.toString())
                        .build();

        return fileStub.generateUploadUrl(request);
    }

    public void confirmUpload(String fileId) {
        fileStub.confirmUpload(
                ConfirmUploadRequest.newBuilder()
                        .setFileId(fileId)
                        .build()
        );
    }
}
