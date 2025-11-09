package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.api.utils.Password
import jakarta.validation.constraints.NotBlank

data class PasswordChangeRequest(
    @field:NotBlank
    val oldPassword: String,

    @field:Password
    val newPassword: String
)
