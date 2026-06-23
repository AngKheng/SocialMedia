package com.socialapp.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request sửa bài viết.
 * - content: null = giữ nguyên
 * - mediaUrls: null = giữ nguyên; [] = xóa hết; [url] = thay bằng list mới
 */
public record UpdatePostRequest(

 @Size(max = 5000, message = "Nội dung tối đa 5000 ký tự")
 String content,

 /**
 * Danh sách URL ảnh/video mới. Tối đa 4 ảnh HOẶC 1 video — validate trong service.
 * null = không thay đổi media hiện tại.
 */
 List<String> mediaUrls
) {}
