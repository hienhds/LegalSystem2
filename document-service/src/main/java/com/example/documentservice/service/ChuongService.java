package com.example.documentservice.service;

import com.example.documentservice.dto.ChuongResponse;
import com.example.documentservice.dto.DeMucResponse;
import com.example.documentservice.exception.AppException;
import com.example.documentservice.exception.ErrorType;
import com.example.documentservice.mongo.document.ChuDeDocument;
import com.example.documentservice.mongo.document.ChuongDocument;
import com.example.documentservice.mongo.document.DeMucDocument;
import com.example.documentservice.mongo.repository.ChuDeRepository;
import com.example.documentservice.mongo.repository.ChuongRepository;
import com.example.documentservice.mongo.repository.DeMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChuongService {

    private final ChuongRepository chuongRepository;
    private final DeMucRepository deMucRepository;
    private final ChuDeRepository chuDeRepository;

    public List<ChuongResponse> getChuongByDeMucId(String deMucId) {
        return chuongRepository.findByDeMucId(deMucId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ChuongResponse toResponse(ChuongDocument chuong) {
        DeMucDocument demuc = deMucRepository.findByDeMucId(chuong.getDeMucId())
                .orElseThrow(()-> new AppException(ErrorType.NOT_FOUND, "khong thay de muc"));

        ChuDeDocument chude = chuDeRepository.findByChuDeId(demuc.getChuDeId())
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "khong thay chu đe"));

        return ChuongResponse.builder()
                .id(chuong.getId().toHexString())
                .deMucId(chuong.getDeMucId())
                .chuDeId(chude.getChuDeId())
                .text(chuong.getText())
                .build();
    }
}
