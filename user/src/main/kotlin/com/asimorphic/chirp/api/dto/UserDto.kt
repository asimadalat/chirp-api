package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val isEmailVerified: Boolean
)