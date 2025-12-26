package com.example.backend.lawyer.service;

import com.example.backend.lawyer.entity.Specialization;
import com.example.backend.lawyer.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecializationService {
    private final SpecializationRepository specializationRepository;

    public List<Specialization> getAll(){
        return specializationRepository.findAll();
    }
}
