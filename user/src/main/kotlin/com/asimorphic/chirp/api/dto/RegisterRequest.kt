package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.api.utils.Password
import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email(message = "Must be a valid email address")
    val email: String,

    @field:Length(min = 4, max = 20, message = "Username length must be between 4 and 20 characters")
    val username: String,

    @field:Password
    val password: String
)