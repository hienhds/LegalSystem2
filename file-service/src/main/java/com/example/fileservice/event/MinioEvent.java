package com.example.fileservice.event;

import lombok.Data;

import java.util.List;

@Data
public class MinioEvent {
    private List<Record> Records;

    @Data
    public static class Record {
        private S3 s3;
    }

    @Data
    public static class S3 {
        private Bucket bucket;
        private MinioObject object;
    }

    @Data
    public static class Bucket {
        private String name;
    }

    @Data
    public static class MinioObject {
        private String key;
        private Long size;
    }
}

