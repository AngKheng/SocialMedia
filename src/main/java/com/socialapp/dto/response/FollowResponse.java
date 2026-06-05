package com.socialapp.dto.response;

/**
 * Trả về sau khi follow/unfollow:
 * cho client biết trạng thái hiện tại + số follower mới nhất.
 */
public record FollowResponse(
        boolean isFollowing,
        long followerCount
) {}