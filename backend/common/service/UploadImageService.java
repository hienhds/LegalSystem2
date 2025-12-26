package com.example.backend.common.service;

import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
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
public class UploadImageService {

    private final UserRepository userRepository;
    public String uploadImage(Long userId, MultipartFile file, String folder){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "user not found"));

        if(file.isEmpty()){
            throw new AppException(ErrorType.BAD_REQUEST, "file khoong duoc de trong");
        }

        if(!isImage(file)){
            throw new AppException(ErrorType.BAD_REQUEST, "file phai la 1 anh (jpg, png, jpeg)");
        }

        // luu file vao thu muc /uploads/ + folder
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadDir = "uploads/" + folder + "/";

        File folderUpload = new File(uploadDir);

        if(!folderUpload.exists()){
            folderUpload.mkdirs();
        }

        try{
            Path path = Paths.get(uploadDir + fileName);
            Files.write(path, file.getBytes());
        }
        catch (IOException e) {
            throw new AppException(ErrorType.INTERNAL_ERROR, "Lỗi khi lưu file");
        }

        String folderUrl = "/uploads/" + folder + "/" + fileName;

        return folderUrl;
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg")
        );
    }
    public String uploadFile(Long userId, MultipartFile file, String folder) {
        // Logic giống hệt uploadImage nhưng bỏ check isImage hoặc check mở rộng hơn
        if(file.isEmpty()){
            throw new AppException(ErrorType.BAD_REQUEST, "File không được để trống");
        }

        // (Tùy chọn) Check đuôi file nếu muốn chặn file exe, sh...
        // if (!isValidDocument(file)) ...

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        // ... (Phần lưu file giữ nguyên như code cũ của bạn) ...
        // ... path, Files.write ...

        return "/uploads/" + folder + "/" + fileName;
    }

}
