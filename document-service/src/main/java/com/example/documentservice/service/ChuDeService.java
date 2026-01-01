package com.example.documentservice.service;

import com.example.documentservice.dto.ChuDeResponse;
import com.example.documentservice.mongo.document.ChuDeDocument;
import com.example.documentservice.mongo.repository.ChuDeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChuDeService {
    private final ChuDeRepository chuDeRepository;

    public List<ChuDeResponse> getAllChuDe(){
        return chuDeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ChuDeResponse toResponse(ChuDeDocument chuDe){
        ChuDeResponse res = ChuDeResponse.builder()
                .id(chuDe.getId().toHexString())
                .chuDeId(chuDe.getChuDeId())
                .text(chuDe.getText())
                .build();

        return res;
    }
}
