package com.asimorphic.chirp.api.mappers

import com.asimorphic.chirp.api.dto.AuthenticatedUserDto
import com.asimorphic.chirp.api.dto.UserDto
import com.asimorphic.chirp.domain.model.AuthenticatedUser
import com.asimorphic.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        isEmailVerified = isEmailVerified
    )
}