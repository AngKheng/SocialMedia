package com.socialapp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final Cloudinary cloudinary;

    // Giới hạn kích thước: 10MB ảnh, 100MB video
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;

    /**
     * Upload 1 file (ảnh hoặc video) lên Cloudinary.
     * Trả về URL công khai để lưu vào DB.
     */
    public String upload(MultipartFile file) {
        validateFile(file);

        try {
            String resourceType = isVideo(file) ? "video" : "image";

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder",        "social-network"   // lưu vào folder riêng
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("Upload thành công: {}", url);
            return url;

        } catch (IOException e) {
            throw new RuntimeException("Upload thất bại: " + e.getMessage());
        }
    }

    // =============================================
    // Validate
    // =============================================

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Không xác định được loại file");
        }

        if (isVideo(file)) {
            if (file.getSize() > MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException("Video không được vượt quá 100MB");
            }
        } else if (isImage(file)) {
            if (file.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Ảnh không được vượt quá 10MB");
            }
        } else {
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận file ảnh (jpg, png, gif, webp) hoặc video (mp4, mov, avi)");
        }
    }

    private boolean isImage(MultipartFile file) {
        String ct = file.getContentType();
        return ct != null && ct.startsWith("image/");
    }

    private boolean isVideo(MultipartFile file) {
        String ct = file.getContentType();
        return ct != null && ct.startsWith("video/");
    }
}