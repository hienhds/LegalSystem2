package com.example.fileservice.minio;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
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
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                        .extraHeaders(headers)
                        .build()
        );

        log.info("sinh thanh cong url ===========");
        return url;

    }

    public String generatePresignedPreviewUrl(String objectKey) throws Exception{
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                        // "inline" giúp trình duyệt hiểu là cần hiển thị nội dung ngay lập tức
                        .extraQueryParams(Map.of("response-content-disposition", "inline"))
                        .build()
        );
    }


    public String generatePresignedDownloadUrl(String objectKey, String fileName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                        // "attachment" ép trình duyệt tải xuống và đặt tên file mong muốn
                        .extraQueryParams(Map.of(
                                "response-content-disposition",
                                "attachment; filename=\"" + fileName + "\""
                        ))
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
