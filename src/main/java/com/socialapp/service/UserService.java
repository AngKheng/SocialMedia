package com.socialapp.service;

import com.socialapp.dto.request.UpdateProfileRequest;
import com.socialapp.dto.response.UserProfileResponse;
import com.socialapp.dto.response.UserResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.User;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository   userRepository;
    private final FollowRepository followRepository;

    // =============================================
    // GET /api/users/{id}
    // =============================================

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long targetUserId, UserDetails currentUser) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        long followerCount  = followRepository.countByFollowingId(targetUserId);
        long followingCount = followRepository.countByFollowerId(targetUserId);

        // Kiểm tra người đang đăng nhập có follow target không
        User me = getUserByUsername(currentUser.getUsername());
        boolean isFollowing = !me.getId().equals(targetUserId)
                && followRepository.existsByFollowerIdAndFollowingId(me.getId(), targetUserId);

        return UserProfileResponse.from(target, followerCount, followingCount, isFollowing);
    }

    // =============================================
    // PUT /api/users/me
    // =============================================

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request, UserDetails currentUser) {
        User me = getUserByUsername(currentUser.getUsername());

        // Chỉ cập nhật field nào client gửi lên (không null)
        if (request.displayName() != null) {
            me.setDisplayName(request.displayName());
        }
        if (request.bio() != null) {
            me.setBio(request.bio());
        }
        if (request.avatarUrl() != null) {
            me.setAvatarUrl(request.avatarUrl());
        }

        userRepository.save(me);
        log.info("User @{} đã cập nhật profile", me.getUsername());
        return UserResponse.from(me);
    }

    // =============================================
    // Helper
    // =============================================

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại: " + username));
    }
}