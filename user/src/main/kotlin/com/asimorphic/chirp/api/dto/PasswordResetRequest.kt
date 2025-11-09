package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.api.utils.Password
import jakarta.validation.constraints.NotBlank

data class PasswordResetRequest(
    @field:NotBlank
    val token: String,

    @field:Password
    val newPassword: String
)
