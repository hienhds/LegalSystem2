package com.example.documentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TreeChuDeContentResponse {
    private String cursor;
    private boolean hasMore;
    private List<TreeChuDeItem> items;
}

