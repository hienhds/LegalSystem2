package com.example.fileservice.minio;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {


    private final MinioClient minioClient;

    @Value("${minio.bucket.default}")
    private String bucket;

    @Value("${minio.presigned-expiry:10}")
    private int presignedExpiryMinutes;



    public String generatePresignedUploadUrl(
            String objectKey,
            String contentType
    ) throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
//        headers.put("Host", "localhost:9000");
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                        .extraHeaders(headers)
                        .build()
        );
    }

    public String generatePresignedDownloadUrl(String objectKey) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                        .build()
        );
    }
    public void deleteObject(String objectKey) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
    }


    private  void createBucketIfNoExists() throws Exception{
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
