package com.socialapp.dto.response;

/**
 * Trả về sau khi like/unlike:
 * cho client biết trạng thái hiện tại + số like mới nhất.
 */
public record LikeResponse(
        boolean isLiked,
        int likeCount
) {}