package com.example.documentservice.service;

import com.example.documentservice.dto.DeMucResponse;
import com.example.documentservice.mongo.document.DeMucDocument;
import com.example.documentservice.mongo.repository.DeMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeMucService {

    private final DeMucRepository deMucRepository;

    public List<DeMucResponse> getDeMucByChuDeId(String chuDeId) {
        return deMucRepository.findByChuDeId(chuDeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DeMucResponse toResponse(DeMucDocument deMuc) {
        return DeMucResponse.builder()
                .id(deMuc.getId().toHexString())
                .deMucId(deMuc.getDeMucId())
                .chuDeId(deMuc.getChuDeId())
                .text(deMuc.getText())
                .build();
    }
}
