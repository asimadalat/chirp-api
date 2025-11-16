package com.asimorphic.chirp.domain.model

import com.asimorphic.chirp.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val isEmailVerified: Boolean
)
