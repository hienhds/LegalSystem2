package com.example.documentservice.service;

import com.example.documentservice.client.UserClient;
import com.example.documentservice.dto.DieuResponse;
import com.example.documentservice.dto.UserSummary;
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
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DieuService {
    private final ChuDeRepository chuDeRepository;
    private final DeMucRepository deMucRepository;
    private final ChuongRepository chuongRepository;
    private final DieuRepository dieuRepository;
    private final UserClient  userClient;


    public List<DieuResponse> getAllDieuByChuongId(String chuongId){
        ObjectId chuongObjectId = new ObjectId(chuongId);

        ChuongDocument chuongDocument = chuongRepository.findByObjectId(chuongObjectId)
                .orElseThrow(()-> new AppException(ErrorType.NOT_FOUND, "loi not found"));

        DeMucDocument demuc = deMucRepository.findByDeMucId(chuongDocument.getDeMucId())
                .orElseThrow(()-> new AppException(ErrorType.NOT_FOUND, "loi not found"));

        ChuDeDocument chude = chuDeRepository.findByChuDeId(demuc.getChuDeId())
                .orElseThrow(()-> new AppException(ErrorType.NOT_FOUND, "loi not found"));

        List<DieuDocument> dieus = dieuRepository.findByChuongId(chuongObjectId);

        List<DieuResponse> dieuResponses = new ArrayList<>();

        DieuDocument dieu1 = dieus.get(0);

//        log.info("id: " + dieu1.getId().toHexString());
//        log.info("idChuong: " + dieu1.getChuongId());
//        List<String> noidung = dieu1.getNoiDung();
//        for(String nd: noidung){
//            log.info("nội dung: " + nd);
//        }
        for(DieuDocument dieu: dieus){
            // ===== Map GhiChu =====
            List<DieuResponse.GhiChuResponse> ghiChuResponses = new ArrayList<>();
            if (dieu.getGhiChu() != null) {
                for (DieuDocument.GhiChu gc : dieu.getGhiChu()) {
                    ghiChuResponses.add(
                            new DieuResponse.GhiChuResponse(
                                    gc.getText(),
                                    gc.getLink()
                            )
                    );
                }
            }

            // ===== Map ChiDan =====
            List<DieuResponse.ChiDanResponse> chiDanResponses = new ArrayList<>();
            if (dieu.getChiDan() != null) {
                for (DieuDocument.ChiDan cd : dieu.getChiDan()) {
                    chiDanResponses.add(
                            new DieuResponse.ChiDanResponse(
                                    cd.getMapc(),
                                    cd.getText()
                            )
                    );
                }
            }
            log.info("========================" + dieu.getNoiDung());
            // ===== Build Response =====
            DieuResponse response = DieuResponse.builder()
                    .id(dieu.getId().toHexString())
                    .tieuDe(dieu.getTieuDe())
                    .noiDung(dieu.getNoiDung())
                    .ghiChu(ghiChuResponses)
                    .chiDan(chiDanResponses)
                    .chuongId(chuongDocument.getId())
                    .deMucId(demuc.getDeMucId())
                    .chuDeId(chude.getChuDeId())
                    .build();

            dieuResponses.add(response);
        }

        return dieuResponses;
    }
}
