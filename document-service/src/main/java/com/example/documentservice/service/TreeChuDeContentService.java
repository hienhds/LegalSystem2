package com.example.documentservice.service;

import com.example.documentservice.dto.TreeChuDeContentResponse;
import com.example.documentservice.dto.TreeChuDeItem;
import com.example.documentservice.mongo.document.ChuongDocument;
import com.example.documentservice.mongo.document.DeMucDocument;
import com.example.documentservice.mongo.document.DieuDocument;
import com.example.documentservice.mongo.repository.ChuongRepository;
import com.example.documentservice.mongo.repository.DeMucRepository;
import com.example.documentservice.mongo.repository.DieuRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TreeChuDeContentService {

    private final DieuRepository dieuRepository;
    private final ChuongRepository chuongRepository;
    private final DeMucRepository deMucRepository;

    public TreeChuDeContentResponse loadChuDeContent(
            String chuDeId,
            String cursor,
            int limit
    ) {
        ObjectId chuDeObjId = new ObjectId(chuDeId);
        ObjectId cursorObjId = cursor == null ? new ObjectId("000000000000000000000000")
                : new ObjectId(cursor);

        Pageable pageable = PageRequest.of(0, limit, Sort.by("_id").ascending());

        List<DieuDocument> dieus =
                dieuRepository.findByChuDeIdWithCursor(chuDeObjId, cursorObjId, pageable);

        if (dieus.isEmpty()) {
            return TreeChuDeContentResponse.builder()
                    .hasMore(false)
                    .items(List.of())
                    .build();
        }

        // 1️⃣ Collect IDs
        List<ObjectId> chuongIds = dieus.stream()
                .map(DieuDocument::getChuongId)
                .distinct()
                .toList();

        // 2️⃣ Load Chuong
        Map<ObjectId, ChuongDocument> chuongMap =
                chuongRepository.findByIds(chuongIds)
                        .stream()
                        .collect(Collectors.toMap(ChuongDocument::getId, c -> c));

        // 3️⃣ Collect DeMuc IDs
        List<ObjectId> deMucIds = chuongMap.values()
                .stream()
                .map(c -> new ObjectId(c.getDeMucId()))
                .distinct()
                .toList();

        // 4️⃣ Load DeMuc
        Map<ObjectId, DeMucDocument> deMucMap =
                deMucRepository.findByIds(deMucIds)
                        .stream()
                        .collect(Collectors.toMap(DeMucDocument::getId, d -> d));

        // 5️⃣ Build response
        List<TreeChuDeItem> items = dieus.stream().map(dieu -> {

            ChuongDocument chuong = chuongMap.get(dieu.getChuongId());
            DeMucDocument deMuc = deMucMap.get(new ObjectId(chuong.getDeMucId()));

            return TreeChuDeItem.builder()
                    .deMuc(TreeChuDeItem.SimpleNode.builder()
                            .id(deMuc.getId().toHexString())
                            .text(deMuc.getText())
                            .build())
                    .chuong(TreeChuDeItem.SimpleNode.builder()
                            .id(chuong.getId().toHexString())
                            .text(chuong.getText())
                            .build())
                    .dieu(TreeChuDeItem.DieuContent.builder()
                            .id(dieu.getId().toHexString())
                            .tieuDe(dieu.getTieuDe())
                            .noiDung(dieu.getNoiDung())
                            .ghiChu(dieu.getGhiChu())
                            .chiDan(dieu.getChiDan())
                            .build())
                    .build();
        }).toList();

        String nextCursor = dieus.get(dieus.size() - 1).getId().toHexString();

        return TreeChuDeContentResponse.builder()
                .cursor(nextCursor)
                .hasMore(dieus.size() == limit)
                .items(items)
                .build();
    }
}
