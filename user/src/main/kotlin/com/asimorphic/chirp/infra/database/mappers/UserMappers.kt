package com.asimorphic.chirp.infra.database.mappers

import com.asimorphic.chirp.domain.model.User
import com.asimorphic.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        isEmailVerified = hasVerifiedEmail
    )
}