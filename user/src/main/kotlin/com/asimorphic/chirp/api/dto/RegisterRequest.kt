package com.asimorphic.chirp.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email(message = "Must be a valid email address")
    val email: String,
    @field:Length(min = 4, max = 20, message = "Username length must be between 4 and 20 characters")
    val username: String,
    @field:Pattern(regexp = "^(?:(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}|[A-Za-z ]{20,})\$",
        message = "Password must be at least 8 characters with at least one number and one symbol, or be a passphrase of 20 or more letters/spaces."
    )
    val password: String
)