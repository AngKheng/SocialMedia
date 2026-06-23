package com.socialapp.controller;

import com.socialapp.dto.request.CreatePostRequest;
import com.socialapp.dto.request.UpdatePostRequest;
import com.socialapp.dto.response.PageResponse;
import com.socialapp.dto.response.PostResponse;
import com.socialapp.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

 private final PostService postService;

 /**
 * POST /api/posts
 * Tạo bài viết mới.
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
 * PUT /api/posts/{id}
 * Sửa bài — chỉ chủ bài mới được sửa.
 * Content hoặc mediaUrls = null nghĩa là giữ nguyên; [] = xóa hết media.
 */
 @PutMapping("/{id}")
 public ResponseEntity<PostResponse> updatePost(
 @PathVariable Long id,
 @Valid @RequestBody UpdatePostRequest request,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(postService.updatePost(id, request, currentUser));
 }

 /**
 * DELETE /api/posts/{id}
 * Xóa bài — chỉ chủ bài mới được xóa.
 */
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deletePost(
 @PathVariable Long id,
 @AuthenticationPrincipal UserDetails currentUser) {

 postService.deletePost(id, currentUser);
 return ResponseEntity.noContent().build();
 }

 /**
 * GET /api/posts/feed?page=0&size=10
 * Feed của người đang đăng nhập, kèm isLiked theo currentUser.
 */
 @GetMapping("/feed")
 public ResponseEntity<PageResponse<PostResponse>> getFeed(
 @RequestParam(defaultValue = "0") int page,
 @RequestParam(defaultValue = "10") int size,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(postService.getFeed(page, size, currentUser));
 }

 /**
 * GET /api/posts/discover?limit=20
 * Discover feed (Phase 9I): random posts từ user không follow + không phải chính mình.
 */
 @GetMapping("/discover")
 public ResponseEntity<List<PostResponse>> getDiscover(
 @RequestParam(defaultValue = "20") int limit,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(postService.getDiscover(limit, currentUser));
 }

 /**
 * GET /api/posts/{id}
 * Xem chi tiết 1 bài viết, kèm isLiked theo currentUser.
 */
 @GetMapping("/{id}")
 public ResponseEntity<PostResponse> getPost(
 @PathVariable Long id,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(postService.getPost(id, currentUser));
 }

 /**
 * GET /api/posts/user/{id}?page=0&size=10
 * Tất cả bài của một user, kèm isLiked theo currentUser.
 */
 @GetMapping("/user/{id}")
 public ResponseEntity<PageResponse<PostResponse>> getPostsByUser(
 @PathVariable Long id,
 @RequestParam(defaultValue = "0") int page,
 @RequestParam(defaultValue = "10") int size,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(postService.getPostsByUser(id, page, size, currentUser));
 }
}
