package com.example.fileservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường thừa khác từ MinIO
public class MinioEvent {

    @JsonProperty("Records") // MinIO gửi chữ R viết hoa
    private List<Record> records;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Record {
        @JsonProperty("s3")
        private S3 s3;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class S3 {
        @JsonProperty("bucket")
        private Bucket bucket;
        @JsonProperty("object")
        private S3Object object;
    }

    @Data
    public static class Bucket {
        private String name;
    }

    @Data
    public static class S3Object {
        private String key;
        private Long size;
    }
}
