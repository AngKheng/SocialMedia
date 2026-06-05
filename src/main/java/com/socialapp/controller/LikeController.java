package com.socialapp.controller;

import com.socialapp.dto.response.LikeResponse;
import com.socialapp.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * POST /api/posts/{id}/like
     * Like một bài viết.
     */
    @PostMapping("/api/posts/{id}/like")
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(likeService.likePost(id, currentUser));
    }

    /**
     * DELETE /api/posts/{id}/like
     * Unlike một bài viết.
     */
    @DeleteMapping("/api/posts/{id}/like")
    public ResponseEntity<LikeResponse> unlikePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(likeService.unlikePost(id, currentUser));
    }

    /**
     * POST /api/comments/{id}/like
     * Like một comment.
     */
    @PostMapping("/api/comments/{id}/like")
    public ResponseEntity<LikeResponse> likeComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(likeService.likeComment(id, currentUser));
    }

    /**
     * DELETE /api/comments/{id}/like
     * Unlike một comment.
     */
    @DeleteMapping("/api/comments/{id}/like")
    public ResponseEntity<LikeResponse> unlikeComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(likeService.unlikeComment(id, currentUser));
    }
}