package com.socialapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(

 @NotBlank(message = "Nội dung comment không được để trống")
 @Size(max = 1000, message = "Comment tối đa 1000 ký tự")
 String content
) {}
