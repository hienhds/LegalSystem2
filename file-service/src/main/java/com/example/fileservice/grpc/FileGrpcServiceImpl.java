package com.example.fileservice.grpc;

import com.example.fileservice.document.FileMetadataDocument;
import com.example.fileservice.minio.MinioService;
import com.example.fileservice.repository.FileMetadataRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class FileGrpcServiceImpl
        extends FileServiceGrpc.FileServiceImplBase {

    private final MinioService minioService;
    private final FileMetadataRepository repository;

    @Value("${minio.bucket.default}")
    private String bucket;


    @Override
    public void generateUploadUrl(
            GenerateUploadUrlRequest request,
            StreamObserver<PresignedUrlResponse> responseObserver
    ) {
        try {
            String fileId = UUID.randomUUID().toString();
            String objectKey =
                    request.getBusinessType() + "/"
                            + request.getBusinessId() + "/"
                            + fileId + "_" + request.getFileName();

            String presignedUrl =
                    minioService.generatePresignedUploadUrl(
                            objectKey,
                            request.getContentType()
                    );

            FileMetadataDocument doc =
                    FileMetadataDocument.builder()
                            .fileId(fileId)
                            .fileName(request.getFileName())
                            .contentType(request.getContentType())
                            .fileSize(request.getFileSize())
                            .uploadedByUserId(
                                    Long.parseLong(request.getUploadedBy())
                            )
                            .bucket("file-service")
                            .objectKey(objectKey)
                            .businessType(request.getBusinessType())
                            .businessId(request.getBusinessId())
                            .uploadedAt(Instant.now())
                            .status("PENDING")
                            .build();

            repository.save(doc);

            responseObserver.onNext(
                    PresignedUrlResponse.newBuilder()
                            .setSuccess(true)
                            .setFileId(fileId)
                            .setPresignedUrl(presignedUrl)
                            .setExpiredAt(
                                    Instant.now().plusSeconds(600).toEpochMilli()
                            )
                            .build()
            );
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onNext(
                    PresignedUrlResponse.newBuilder()
                            .setSuccess(false)
                            .setErrorMessage(e.getMessage())
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void confirmUpload(
            ConfirmUploadRequest request,
            StreamObserver<ConfirmUploadResponse> responseObserver
    ) {
        repository.findById(request.getFileId())
                .ifPresent(doc -> {
                    doc.setStatus("COMPLETED");
                    repository.save(doc);
                });

        responseObserver.onNext(
                ConfirmUploadResponse.newBuilder()
                        .setSuccess(true)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
