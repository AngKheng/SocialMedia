package com.socialapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotNull(message = "postId không được để trống")
        Long postId,

        @NotBlank(message = "Nội dung comment không được để trống")
        @Size(max = 1000, message = "Comment tối đa 1000 ký tự")
        String content,

        /**
         * null  = comment gốc
         * !null = reply của comment có id này
         */
        Long parentCommentId
) {}