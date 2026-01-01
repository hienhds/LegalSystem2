package com.example.documentservice.service;

import com.example.documentservice.dto.DieuRedirectResponse;
import com.example.documentservice.mongo.document.*;
import com.example.documentservice.mongo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChiDanResolveService {

    private final DieuRepository dieuRepository;
    private final ChuongRepository chuongRepository;
    private final DeMucRepository deMucRepository;
    private final ChuDeRepository chuDeRepository;

    public DieuRedirectResponse resolve(String chiDanText) {

        // 1. Tìm điều ĐÍCH thông qua chi_dan.text
        DieuDocument dieu = dieuRepository.findOneByChiDanText(chiDanText)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không resolve được chỉ dẫn: " + chiDanText));

        // 2. Truy ngược cây
        ChuongDocument chuong = chuongRepository.findById(dieu.getChuongId())
                .orElseThrow();

        DeMucDocument deMuc = deMucRepository.findByDeMucId(chuong.getDeMucId())
                .orElseThrow();

        ChuDeDocument chuDe = chuDeRepository.findByChuDeId(deMuc.getChuDeId())
                .orElseThrow();

        // 3. Trả về context cho frontend
        return DieuRedirectResponse.builder()
                .chuDeId(chuDe.getId().toHexString())
                .deMucId(deMuc.getId().toHexString())
                .chuongId(chuong.getId().toHexString())
                .dieuId(dieu.getId().toHexString())
                .build();
    }
}
