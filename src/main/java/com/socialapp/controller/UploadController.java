package com.socialapp.controller;

import com.socialapp.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * POST /api/upload
     * Upload 1 file ảnh hoặc video lên Cloudinary.
     * Trả về URL để dùng khi tạo post.
     *
     * Form-data: file = <file>
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) {

        String url = uploadService.upload(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}