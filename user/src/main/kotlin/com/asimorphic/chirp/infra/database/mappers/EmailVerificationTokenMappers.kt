package com.asimorphic.chirp.infra.database.mappers

import com.asimorphic.chirp.domain.model.EmailVerificationToken
import com.asimorphic.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}