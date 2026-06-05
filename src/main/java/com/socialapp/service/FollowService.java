package com.socialapp.service;

import com.socialapp.dto.response.FollowResponse;
import com.socialapp.dto.response.UserResponse;
import com.socialapp.exception.DuplicateResourceException;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Follow;
import com.socialapp.model.User;
import com.socialapp.repository.FollowRepository;
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
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository   userRepository;

    // =============================================
    // POST /api/follow/{id}
    // =============================================

    @Transactional
    public FollowResponse follow(Long targetId, UserDetails currentUser) {
        User me     = getUser(currentUser.getUsername());
        User target = getUserById(targetId);

        if (me.getId().equals(targetId)) {
            throw new IllegalArgumentException("Không thể tự follow chính mình");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(me.getId(), targetId)) {
            throw new DuplicateResourceException("Bạn đã follow @" + target.getUsername() + " rồi");
        }

        followRepository.save(Follow.builder()
                .follower(me)
                .following(target)
                .build());

        log.info("@{} đã follow @{}", me.getUsername(), target.getUsername());

        long followerCount = followRepository.countByFollowingId(targetId);
        return new FollowResponse(true, followerCount);
    }

    // =============================================
    // DELETE /api/follow/{id}
    // =============================================

    @Transactional
    public FollowResponse unfollow(Long targetId, UserDetails currentUser) {
        User me     = getUser(currentUser.getUsername());
        User target = getUserById(targetId);

        if (!followRepository.existsByFollowerIdAndFollowingId(me.getId(), targetId)) {
            throw new IllegalArgumentException("Bạn chưa follow @" + target.getUsername());
        }

        followRepository.deleteByFollowerIdAndFollowingId(me.getId(), targetId);

        log.info("@{} đã unfollow @{}", me.getUsername(), target.getUsername());

        long followerCount = followRepository.countByFollowingId(targetId);
        return new FollowResponse(false, followerCount);
    }

    // =============================================
    // GET /api/follow/{id}/followers
    // =============================================

    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId) {
        getUserById(userId); // 404 nếu không tồn tại

        return followRepository.findByFollowingId(userId).stream()
                .map(f -> UserResponse.from(f.getFollower()))
                .toList();
    }

    // =============================================
    // GET /api/follow/{id}/following
    // =============================================

    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId) {
        getUserById(userId); // 404 nếu không tồn tại

        return followRepository.findByFollowerId(userId).stream()
                .map(f -> UserResponse.from(f.getFollowing()))
                .toList();
    }

    // =============================================
    // Helper
    // =============================================

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại: " + username));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}