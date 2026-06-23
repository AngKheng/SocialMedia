package com.socialapp.service;

import com.socialapp.dto.request.CreatePostRequest;
import com.socialapp.dto.request.UpdatePostRequest;
import com.socialapp.dto.response.PageResponse;
import com.socialapp.dto.response.PostResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.LikeRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

 private final PostRepository postRepository;
 private final UserRepository userRepository;
 private final LikeRepository likeRepository;
 private final FollowRepository followRepository;

 private static final int MAX_IMAGES = 4;
 private static final int MAX_VIDEOS = 1;

 // =============================================
 // POST /api/posts
 // =============================================

 @Transactional
 public PostResponse createPost(CreatePostRequest request, UserDetails currentUser) {
 User me = getUser(currentUser.getUsername());

 List<String> mediaUrls = request.mediaUrls();
 String imageUrlsStr = null;

 if (mediaUrls != null && !mediaUrls.isEmpty()) {
 validateMediaUrls(mediaUrls);
 imageUrlsStr = String.join(",", mediaUrls);
 }

 Post post = Post.builder()
 .user(me)
 .content(request.content())
 .imageUrls(imageUrlsStr)
 .isRepost(false)
 .build();

 Post saved = postRepository.save(post);
 log.info("@{} đã tạo post id={}", me.getUsername(), saved.getId());

 // Vừa tạo thì chưa thể tự like hay follow chính mình
 return PostResponse.from(saved, false, false);
 }

 // =============================================
 // POST /api/posts/{id}/repost (Phase 9K)
 // =============================================

 /**
 * Repost: tạo 1 Post mới với isRepost=true, originalPost=post gốc, content=content gốc.
 * Tăng repostCount của post gốc.
 *
 * Quy tắc:
 * - Không thể repost chính mình
 * - Không thể repost 2 lần cùng 1 bài
 * - Không thể repost 1 bài đã là repost (chỉ repost bài gốc)
 */
 @Transactional
 public PostResponse repost(Long originalPostId, UserDetails currentUser) {
 User me = getUser(currentUser.getUsername());

 Post originalPost = postRepository.findById(originalPostId)
 .orElseThrow(() -> new ResourceNotFoundException("Post", originalPostId));

 if (originalPost.getUser().getId().equals(me.getId())) {
 throw new IllegalArgumentException("Không thể repost bài của chính mình");
 }

 if (Boolean.TRUE.equals(originalPost.getIsRepost())) {
 throw new IllegalArgumentException("Không thể repost một bài đã là repost");
 }

 // Check đã repost chưa
 if (postRepository.findByUserIdAndOriginalPostIdAndIsRepostTrue(
 me.getId(), originalPostId).isPresent()) {
 throw new IllegalArgumentException("Bạn đã repost bài này rồi");
 }

 Post repost = Post.builder()
 .user(me)
 .content(originalPost.getContent()) // copy content gốc
 .imageUrls(originalPost.getImageUrls())
 .isRepost(true)
 .originalPost(originalPost)
 .build();

 Post saved = postRepository.save(repost);

 // Tăng repostCount của post gốc
 originalPost.setRepostCount(originalPost.getRepostCount() + 1);
 postRepository.save(originalPost);

 log.info("@{} đã repost bài id={}", me.getUsername(), originalPostId);

 return PostResponse.from(saved, false, false);
 }

 // =============================================
 // DELETE /api/posts/{id}/repost (Phase 9K)
 // =============================================

 /**
 * Undo repost: tìm Post là repost của user với originalPostId, xóa nó.
 * Giảm repostCount của post gốc.
 *
 * Body không cần — id trên URL là id của bài GỐC.
 */
 @Transactional
 public void unrepost(Long originalPostId, UserDetails currentUser) {
 User me = getUser(currentUser.getUsername());

 Post repost = postRepository
 .findByUserIdAndOriginalPostIdAndIsRepostTrue(me.getId(), originalPostId)
 .orElseThrow(() -> new IllegalArgumentException(
 "Bạn chưa repost bài này"));

 postRepository.delete(repost);

 // Giảm repostCount của post gốc
 postRepository.findById(originalPostId).ifPresent(originalPost -> {
 originalPost.setRepostCount(Math.max(0, originalPost.getRepostCount() - 1));
 postRepository.save(originalPost);
 });

 log.info("@{} đã undo repost bài id={}", me.getUsername(), originalPostId);
 }

 // =============================================
 // PUT /api/posts/{id}
 // =============================================

 @Transactional
 public PostResponse updatePost(Long postId, UpdatePostRequest request, UserDetails currentUser) {
 Post post = postRepository.findById(postId)
 .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

 if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
 throw new IllegalArgumentException("Bạn không có quyền sửa bài này");
 }

 if (request.content() != null) {
 post.setContent(request.content());
 }

 if (request.mediaUrls() != null) {
 // null = giữ nguyên; [] hoặc [..] = thay thế
 if (request.mediaUrls().isEmpty()) {
 post.setImageUrls(null);
 } else {
 validateMediaUrls(request.mediaUrls());
 post.setImageUrls(String.join(",", request.mediaUrls()));
 }
 }

 Post saved = postRepository.save(post);
 log.info("@{} đã cập nhật post id={}", currentUser.getUsername(), postId);

 User me = getUser(currentUser.getUsername());
 return toResponse(saved, me.getId());
 }

 // =============================================
 // DELETE /api/posts/{id}
 // =============================================

 @Transactional
 public void deletePost(Long postId, UserDetails currentUser) {
 Post post = postRepository.findById(postId)
 .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

 if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
 throw new IllegalArgumentException("Bạn không có quyền xóa bài này");
 }

 postRepository.delete(post);
 log.info("@{} đã xóa post id={}", currentUser.getUsername(), postId);
 }

 // =============================================
 // GET /api/posts/feed
 // =============================================

 @Transactional(readOnly = true)
 public PageResponse<PostResponse> getFeed(int page, int size, UserDetails currentUser) {
 User me = getUser(currentUser.getUsername());
 Pageable pageable = PageRequest.of(page, size);

 return PageResponse.from(
 postRepository.findFeedByUserId(me.getId(), pageable)
 .map(post -> toResponse(post, me.getId()))
 );
 }

 // =============================================
 // GET /api/posts/discover (Phase 9I)
 // =============================================

 /**
 * Discover feed: trả về N bài random từ user không follow + không phải chính mình.
 * Không phân trang — mỗi lần gọi là 1 lần random mới.
 */
 @Transactional(readOnly = true)
 public List<PostResponse> getDiscover(int limit, UserDetails currentUser) {
 User me = getUser(currentUser.getUsername());

 // Giới hạn tối đa 50 để tránh abuse
 int safeLimit = Math.min(Math.max(limit, 1), 50);

 return postRepository.findDiscoverByUserId(me.getId(), safeLimit).stream()
 .map(post -> toResponse(post, me.getId()))
 .toList();
 }

 // =============================================
 // GET /api/posts/{id}
 // =============================================

 @Transactional(readOnly = true)
 public PostResponse getPost(Long postId, UserDetails currentUser) {
 Post post = postRepository.findById(postId)
 .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

 User me = getUser(currentUser.getUsername());
 return toResponse(post, me.getId());
 }

 // =============================================
 // GET /api/posts/user/{id}
 // =============================================

 @Transactional(readOnly = true)
 public PageResponse<PostResponse> getPostsByUser(Long userId, int page, int size,
 UserDetails currentUser) {
 userRepository.findById(userId)
 .orElseThrow(() -> new ResourceNotFoundException("User", userId));

 User me = getUser(currentUser.getUsername());
 Pageable pageable = PageRequest.of(page, size);

 return PageResponse.from(
 postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
 .map(post -> toResponse(post, me.getId()))
 );
 }

 // =============================================
 // Helper: build PostResponse với isLiked + isFollowing
 // =============================================

 private PostResponse toResponse(Post post, Long currentUserId) {
 boolean isLiked = likeRepository
 .existsByUserIdAndPostId(currentUserId, post.getId());

 // Không follow chính mình
 boolean isFollowing = !post.getUser().getId().equals(currentUserId)
 && followRepository.existsByFollowerIdAndFollowingId(
 currentUserId, post.getUser().getId());

 return PostResponse.from(post, isLiked, isFollowing);
 }

 // =============================================
 // Validate media
 // =============================================

 private void validateMediaUrls(List<String> mediaUrls) {
 long videoCount = mediaUrls.stream()
 .filter(url -> url.contains("/video/") || isVideoExtension(url))
 .count();
 long imageCount = mediaUrls.size() - videoCount;

 if (videoCount > 0 && imageCount > 0) {
 throw new IllegalArgumentException(
 "Không thể đăng ảnh và video cùng lúc trong một bài");
 }
 if (videoCount > MAX_VIDEOS) {
 throw new IllegalArgumentException("Tối đa " + MAX_VIDEOS + " video mỗi bài");
 }
 if (imageCount > MAX_IMAGES) {
 throw new IllegalArgumentException("Tối đa " + MAX_IMAGES + " ảnh mỗi bài");
 }
 }

 private boolean isVideoExtension(String url) {
 String lower = url.toLowerCase();
 return lower.endsWith(".mp4") || lower.endsWith(".mov")
 || lower.endsWith(".avi") || lower.endsWith(".webm");
 }

 // =============================================
 // Helper
 // =============================================

 private User getUser(String username) {
 return userRepository.findByUsername(username)
 .orElseThrow(() -> new ResourceNotFoundException(
 "User không tồn tại: " + username));
 }
}
