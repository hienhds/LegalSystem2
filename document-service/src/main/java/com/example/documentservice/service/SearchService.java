package com.example.documentservice.service;

import com.example.documentservice.dto.SearchResultResponse;
import com.example.documentservice.exception.AppException;
import com.example.documentservice.exception.ErrorType;
import com.example.documentservice.mongo.document.ChuDeDocument;
import com.example.documentservice.mongo.document.ChuongDocument;
import com.example.documentservice.mongo.document.DeMucDocument;
import com.example.documentservice.mongo.document.DieuDocument;
import com.example.documentservice.mongo.repository.ChuDeRepository;
import com.example.documentservice.mongo.repository.ChuongRepository;
import com.example.documentservice.mongo.repository.DeMucRepository;
import com.example.documentservice.mongo.repository.DieuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final MongoTemplate mongoTemplate;
    private final ChuDeRepository chuDeRepository;
    private final DeMucRepository deMucRepository;
    private final ChuongRepository chuongRepository;
    private final DieuRepository dieuRepository;

    public List<SearchResultResponse> searchFullText(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String normalizedKeyword = keyword.trim();
        
        // Tìm kiếm trong DieuDocument (tieuDe và noiDung)
        Query query = new Query();
        
        // Tạo regex pattern case-insensitive
        Pattern pattern = Pattern.compile(normalizedKeyword, Pattern.CASE_INSENSITIVE);
        
        // Tìm trong tiêu đề HOẶC nội dung
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("tieu_de").regex(pattern),
            Criteria.where("noi_dung").regex(pattern)
        );
        
        query.addCriteria(criteria);
        query.limit(50); // Giới hạn kết quả
        
        List<DieuDocument> dieuDocuments = mongoTemplate.find(query, DieuDocument.class);
        
        log.info("Found {} dieu documents for keyword: {}", dieuDocuments.size(), keyword);
        
        // Map sang SearchResultResponse
        List<SearchResultResponse> results = new ArrayList<>();
        
        for (DieuDocument dieu : dieuDocuments) {
            try {
                // Lấy thông tin chương
                ChuongDocument chuong = chuongRepository.findByObjectId(dieu.getChuongId())
                    .orElse(null);
                
                if (chuong == null) continue;
                
                // Lấy thông tin đề mục
                DeMucDocument deMuc = deMucRepository.findByDeMucId(chuong.getDeMucId())
                    .orElse(null);
                
                if (deMuc == null) continue;
                
                // Lấy thông tin chủ đề
                ChuDeDocument chuDe = chuDeRepository.findByChuDeId(deMuc.getChuDeId())
                    .orElse(null);
                
                if (chuDe == null) continue;
                
                // Tạo highlight text (lấy đoạn đầu tiên chứa keyword)
                String highlightedText = extractHighlight(dieu, normalizedKeyword);
                
                SearchResultResponse result = SearchResultResponse.builder()
                    .dieuId(dieu.getId().toHexString())
                    .tieuDe(dieu.getTieuDe())
                    .noiDung(dieu.getNoiDung())
                    .chuongId(chuong.getId().toHexString())
                    .chuongText(chuong.getText())
                    .deMucId(deMuc.getDeMucId())
                    .deMucText(deMuc.getText())
                    .chuDeId(chuDe.getChuDeId())
                    .chuDeText(chuDe.getText())
                    .highlightedText(highlightedText)
                    .build();
                
                results.add(result);
                
            } catch (Exception e) {
                log.error("Error mapping dieu to search result: {}", e.getMessage());
            }
        }
        
        log.info("Returning {} search results", results.size());
        return results;
    }
    
    private String extractHighlight(DieuDocument dieu, String keyword) {
        Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        
        // Tìm trong tiêu đề trước
        if (dieu.getTieuDe() != null && pattern.matcher(dieu.getTieuDe()).find()) {
            return truncateAroundKeyword(dieu.getTieuDe(), keyword, 100);
        }
        
        // Nếu không có trong tiêu đề, tìm trong nội dung
        if (dieu.getNoiDung() != null) {
            for (String content : dieu.getNoiDung()) {
                if (pattern.matcher(content).find()) {
                    return truncateAroundKeyword(content, keyword, 150);
                }
            }
        }
        
        // Nếu không tìm thấy, return tiêu đề hoặc nội dung đầu tiên
        if (dieu.getTieuDe() != null) {
            return dieu.getTieuDe().length() > 150 
                ? dieu.getTieuDe().substring(0, 150) + "..." 
                : dieu.getTieuDe();
        }
        
        if (dieu.getNoiDung() != null && !dieu.getNoiDung().isEmpty()) {
            String firstContent = dieu.getNoiDung().get(0);
            return firstContent.length() > 150 
                ? firstContent.substring(0, 150) + "..." 
                : firstContent;
        }
        
        return "";
    }
    
    private String truncateAroundKeyword(String text, String keyword, int maxLength) {
        Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (!matcher.find()) {
            return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
        }
        
        int keywordStart = matcher.start();
        int keywordEnd = matcher.end();
        
        // Tính toán vị trí bắt đầu và kết thúc
        int start = Math.max(0, keywordStart - maxLength / 2);
        int end = Math.min(text.length(), keywordEnd + maxLength / 2);
        
        String result = text.substring(start, end);
        
        if (start > 0) result = "..." + result;
        if (end < text.length()) result = result + "...";
        
        return result;
    }
}
