package com.socialapp.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = 100, message = "Display name tối đa 100 ký tự")
        String displayName,

        @Size(max = 255, message = "Bio tối đa 255 ký tự")
        String bio,

        @Size(max = 500, message = "Avatar URL tối đa 500 ký tự")
        String avatarUrl
) {}