package com.asimorphic.chirp.api.utils

import com.asimorphic.chirp.domain.exception.UnauthorizedException
import com.asimorphic.chirp.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()