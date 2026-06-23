package com.socialapp.controller;

import com.socialapp.dto.response.PostResponse;
import com.socialapp.dto.response.UserResponse;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.LikeRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final UserRepository  userRepository;
    private final PostRepository  postRepository;
    private final FollowRepository followRepository;
    private final LikeRepository  likeRepository;

    private static final int MAX_RESULTS = 20;

    /**
     * GET /api/search/users?q=keyword
     * Tìm user theo username hoặc displayName.
     * Trả về List<UserResponse>.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String q,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (q == null || q.isBlank() || q.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<User> users = userRepository.searchByKeyword(
                q.trim(), PageRequest.of(0, MAX_RESULTS));

        List<UserResponse> result = users.stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/search/posts?q=keyword
     * Tìm bài viết theo nội dung.
     * Trả về List<PostResponse> (có isLiked + isFollowing đúng với currentUser).
     */
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @RequestParam String q,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (q == null || q.isBlank() || q.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        // Lấy ID của người đang đăng nhập để check isLiked + isFollowing
        User me = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow();

        List<Post> posts = postRepository.searchByContent(
                q.trim(), PageRequest.of(0, MAX_RESULTS));

        List<PostResponse> result = posts.stream()
                .map(post -> {
                    boolean isLiked = likeRepository
                            .existsByUserIdAndPostId(me.getId(), post.getId());
                    boolean isFollowing = !post.getUser().getId().equals(me.getId())
                            && followRepository.existsByFollowerIdAndFollowingId(
                                    me.getId(), post.getUser().getId());
                    return PostResponse.from(post, isLiked, isFollowing);
                })
                .toList();

        return ResponseEntity.ok(result);
    }
}