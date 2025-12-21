package com.example.userservice.lawyer.service;

import com.example.userservice.lawyer.entity.Specialization;
import com.example.userservice.lawyer.repository.SpecializationRepository;
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
