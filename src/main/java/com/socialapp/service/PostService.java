package com.socialapp.service;

import com.socialapp.dto.request.CreatePostRequest;
import com.socialapp.dto.response.PostResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // Giới hạn như Twitter/X
    private static final int MAX_IMAGES = 4;
    private static final int MAX_VIDEOS = 1;

    // =============================================
    // POST /api/posts
    // =============================================

    @Transactional
    public PostResponse createPost(CreatePostRequest request, UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());

        // Validate số lượng media
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
        return PostResponse.from(saved);
    }

    // =============================================
    // DELETE /api/posts/{id}
    // =============================================

    @Transactional
    public void deletePost(Long postId, UserDetails currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // Chỉ chủ bài mới được xóa
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa bài này");
        }

        postRepository.delete(post);
        log.info("@{} đã xóa post id={}", currentUser.getUsername(), postId);
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
            throw new IllegalArgumentException(
                    "Tối đa " + MAX_VIDEOS + " video mỗi bài");
        }
        if (imageCount > MAX_IMAGES) {
            throw new IllegalArgumentException(
                    "Tối đa " + MAX_IMAGES + " ảnh mỗi bài");
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