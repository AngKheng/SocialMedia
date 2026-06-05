package com.socialapp.controller;

import com.socialapp.dto.response.FollowResponse;
import com.socialapp.dto.response.UserResponse;
import com.socialapp.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * POST /api/follow/{id}
     * Follow một user.
     */
    @PostMapping("/{id}")
    public ResponseEntity<FollowResponse> follow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(followService.follow(id, currentUser));
    }

    /**
     * DELETE /api/follow/{id}
     * Unfollow một user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<FollowResponse> unfollow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(followService.unfollow(id, currentUser));
    }

    /**
     * GET /api/follow/{id}/followers
     * Danh sách người đang follow user {id}.
     */
    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable Long id) {
        return ResponseEntity.ok(followService.getFollowers(id));
    }

    /**
     * GET /api/follow/{id}/following
     * Danh sách người mà user {id} đang follow.
     */
    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable Long id) {
        return ResponseEntity.ok(followService.getFollowing(id));
    }
}