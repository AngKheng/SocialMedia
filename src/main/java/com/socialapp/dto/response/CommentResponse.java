package com.socialapp.dto.response;

import com.socialapp.model.Comment;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record CommentResponse(
        Long id,
        UserResponse user,
        String content,
        Long parentCommentId,
        int likeCount,
        boolean isAiGenerated,
        LocalDateTime createdAt,

        /**
         * Danh sách reply (chỉ lồng 1 cấp, giống Twitter/X).
         * Khi trả về 1 comment đơn lẻ (sau khi tạo) thì replies = [].
         */
        List<CommentResponse> replies
) {
    /** Dùng khi tạo comment mới — chưa có replies */
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                UserResponse.from(comment.getUser()),
                comment.getContent(),
                comment.getParentComment() != null
                        ? comment.getParentComment().getId()
                        : null,
                comment.getLikeCount(),
                comment.getIsAiGenerated(),
                comment.getCreatedAt(),
                Collections.emptyList()
        );
    }

    /** Dùng khi lấy danh sách comment của post — kèm replies */
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                UserResponse.from(comment.getUser()),
                comment.getContent(),
                comment.getParentComment() != null
                        ? comment.getParentComment().getId()
                        : null,
                comment.getLikeCount(),
                comment.getIsAiGenerated(),
                comment.getCreatedAt(),
                replies
        );
    }
}