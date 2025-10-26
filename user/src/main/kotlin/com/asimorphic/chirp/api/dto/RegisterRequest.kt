package com.asimorphic.chirp.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email("Must be a valid email address")
    val email: String,

    @field:Length(3, 20, "Username length must be between 3 and 20 characters")
    val username: String,

    @field:Pattern("^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$", message = "Password must be at least 8 characters and contain at least one digit and one special character")
    val password: String
)
