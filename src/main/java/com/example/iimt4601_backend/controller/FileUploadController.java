package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.UploadResponseDto;
import com.example.iimt4601_backend.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/payment-proof")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadResponseDto> uploadPaymentProof(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new UploadResponseDto(false, null, "Empty file"));
            }

            // 파일 유형 검증 (이미지만 허용)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(
                        new UploadResponseDto(false, null, "Only image files are allowed"));
            }

            // 파일 업로드 수행
            String imageUrl = fileUploadService.uploadPaymentProof(file);
            return ResponseEntity.ok(new UploadResponseDto(true, imageUrl, "Upload successful"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    new UploadResponseDto(false, null, "Failed to upload file: " + e.getMessage()));
        }
    }
}