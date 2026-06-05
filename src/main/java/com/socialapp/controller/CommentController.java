package com.socialapp.controller;

import com.socialapp.dto.request.CreateCommentRequest;
import com.socialapp.dto.response.CommentResponse;
import com.socialapp.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * POST /api/comments
     * Tạo comment hoặc reply.
     * Body: { postId, content, parentCommentId }
     * parentCommentId = null → comment gốc
     * parentCommentId = 5   → reply vào comment id=5
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.createComment(request, currentUser));
    }

    /**
     * GET /api/comments/post/{id}
     * Lấy toàn bộ comment của một bài viết.
     * Mỗi comment gốc kèm theo danh sách replies (1 cấp).
     */
    @GetMapping("/post/{id}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @PathVariable Long id) {

        return ResponseEntity.ok(commentService.getCommentsByPost(id));
    }

    /**
     * DELETE /api/comments/{id}
     * Xóa comment — chỉ chủ comment mới được xóa.
     * Xóa comment gốc sẽ xóa luôn toàn bộ replies.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        commentService.deleteComment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}