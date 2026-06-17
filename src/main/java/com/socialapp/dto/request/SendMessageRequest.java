package com.socialapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotNull(message = "receiverId không được để trống")
        Long receiverId,

        @NotBlank(message = "Nội dung tin nhắn không được để trống")
        @Size(max = 2000, message = "Tin nhắn tối đa 2000 ký tự")
        String content
) {}