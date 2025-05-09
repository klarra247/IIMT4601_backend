package com.example.iimt4601_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload.dir:/Users/Klarra/uploads}")  // 절대 경로로 변경
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    public String uploadPaymentProof(MultipartFile file) throws IOException {
        // 디렉토리 확인 및 생성
        File directory = new File(uploadDir + "/payment-proofs");
        if (!directory.exists()) {
            directory.mkdirs();  // 모든 상위 디렉토리도 함께 생성
        }

        // 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFilename = UUID.randomUUID().toString() + fileExtension;
        File destFile = new File(directory, newFilename);

        // 파일 저장
        file.transferTo(destFile);

        // 접근 가능한 URL 반환
        return baseUrl + "/uploads/payment-proofs/" + newFilename;
    }
}