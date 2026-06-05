package com.socialapp.controller;

import com.socialapp.dto.request.CreatePostRequest;
import com.socialapp.dto.response.PostResponse;
import com.socialapp.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * POST /api/posts
     * Tạo bài viết mới.
     * Body: { content, mediaUrls: ["url1", "url2"] }
     * mediaUrls là URL đã upload qua POST /api/upload trước đó.
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(request, currentUser));
    }

    /**
     * DELETE /api/posts/{id}
     * Xóa bài viết — chỉ chủ bài mới được xóa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        postService.deletePost(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}