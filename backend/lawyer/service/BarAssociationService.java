package com.example.backend.lawyer.service;

import com.example.backend.lawyer.dto.response.BarAssociationResponse;
import com.example.backend.lawyer.repository.BarAssociationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarAssociationService {

    private final BarAssociationRepository barAssociationRepository;

    public List<BarAssociationResponse> getAllBarAssociation(){
        return barAssociationRepository.findAll()
                .stream()
                .map(barAssociation -> new BarAssociationResponse(
                        barAssociation.getBarAssociationId(),
                        barAssociation.getAssociationName()
                )).toList();
    }
}
