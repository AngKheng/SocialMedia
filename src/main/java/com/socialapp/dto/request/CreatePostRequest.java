package com.socialapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(

        @NotBlank(message = "Nội dung bài viết không được để trống")
        @Size(max = 5000, message = "Nội dung tối đa 5000 ký tự")
        String content,

        /**
         * Danh sách URL ảnh/video đã upload lên Cloudinary.
         * Tối đa 4 ảnh HOẶC 1 video — validate trong service.
         */
        List<String> mediaUrls
) {}