package com.example.userservice.common.service;

import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.lawyer.dto.request.FilterLawyerRequest;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional
@RequiredArgsConstructor
public class UploadDocumentService {
    private final UserRepository userRepository;

    public String uploadDocument(Long userId, MultipartFile file, String folder){
        User user  = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "user not found"));

        if(file.isEmpty()){
            throw new AppException(ErrorType.BAD_REQUEST, "file khoong duoc de trong");
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        String uploadDir = "uploads/" + folder + "/";

        File folderUpload = new File(uploadDir);
        if(!folderUpload.exists()){
            folderUpload.mkdirs();
        }

        // Lưu file
        try {
            Path path = Paths.get(uploadDir + fileName);
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new AppException(ErrorType.INTERNAL_ERROR, "Lỗi khi lưu file");
        }

        return "/uploads/" + folder + "/" + fileName;
    }
}
