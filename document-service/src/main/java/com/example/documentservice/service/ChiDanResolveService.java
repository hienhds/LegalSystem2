package com.example.documentservice.service;

import com.example.documentservice.dto.DieuRedirectResponse;
import com.example.documentservice.mongo.document.*;
import com.example.documentservice.mongo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class ChiDanResolveService {

    private final DieuRepository dieuRepository;
    private final ChuongRepository chuongRepository;
    private final DeMucRepository deMucRepository;
    private final ChuDeRepository chuDeRepository;

    public DieuRedirectResponse resolve(String chiDanText) {

        // 1. Parse text chỉ dẫn để lấy phần "Điều X.X.LQ.X."
        String dieuPrefix = extractDieuPrefix(chiDanText);
        
        if (dieuPrefix == null) {
            throw new IllegalArgumentException("Không parse được chỉ dẫn: " + chiDanText);
        }
        
        System.out.println("=== DEBUG Chi Dan ===");
        System.out.println("Input text: " + chiDanText);
        System.out.println("Dieu prefix: " + dieuPrefix);
        
        // 2. Tìm điều có tieu_de chứa prefix này
        List<DieuDocument> matchingDieus = dieuRepository.findByTieuDeContaining(dieuPrefix);
        
        System.out.println("Found " + matchingDieus.size() + " matching dieus");
        if (!matchingDieus.isEmpty()) {
            System.out.println("First match tieu_de: " + matchingDieus.get(0).getTieuDe());
        }
        
        if (matchingDieus.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy điều: " + dieuPrefix);
        }
        
        // Lấy điều đầu tiên nếu có nhiều kết quả
        DieuDocument dieu = matchingDieus.get(0);

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

    /**
     * Parse text chỉ dẫn để lấy prefix "Điều X.X.LQ.X."
     * VD: "Điều 1.6.LQ.16. Nhiệm vụ..." -> "Điều 1.6.LQ.16."
     */
    private String extractDieuPrefix(String chiDanText) {
        // Regex để match "Điều X.X.ABC.X."
        Pattern p = Pattern.compile(
            "(Điều\\s+[\\d\\.]+[A-ZĐ-ỹ]*\\.[\\d]+\\.)",
            Pattern.UNICODE_CHARACTER_CLASS
        );
        Matcher m = p.matcher(chiDanText);
        
        if (m.find()) {
            return m.group(1);
        }
        
        return null;
    }
}
