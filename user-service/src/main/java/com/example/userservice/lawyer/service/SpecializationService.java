package com.example.userservice.lawyer.service;

import com.example.userservice.lawyer.dto.response.SpecializationResponse;
import com.example.userservice.lawyer.entity.Specialization;
import com.example.userservice.lawyer.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecializationService {
    private final SpecializationRepository specializationRepository;

    public List<SpecializationResponse> getAll(){
        return specializationRepository.findAll().stream()
                .map(spec -> SpecializationResponse.builder()
                        .specId(spec.getSpecId())
                        .specName(spec.getSpecName())
                        .build())
                .collect(Collectors.toList());
    }
}
