package com.example.caseservice.client;

import com.example.fileservice.grpc.FileServiceGrpc;
import com.example.fileservice.grpc.UploadFileRequest;
import com.example.fileservice.grpc.UploadFileResponse;
import com.google.protobuf.ByteString;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileClient {

    @GrpcClient("file-service") // Tên service cấu hình trong application.properties
    private FileServiceGrpc.FileServiceBlockingStub fileServiceStub;

    public String uploadFile(MultipartFile file) throws IOException {
        // Chuyển MultipartFile sang định dạng gRPC bytes
        UploadFileRequest request = UploadFileRequest.newBuilder()
                .setContent(ByteString.copyFrom(file.getBytes()))
                .setFileName(file.getOriginalFilename())
                .setFileType(file.getContentType())
                .build();

        // Gọi sang file-service
        UploadFileResponse response = fileServiceStub.uploadFile(request);

        // Trả về URL hoặc Path của file đã upload
        return response.getFileUrl();
    }
}