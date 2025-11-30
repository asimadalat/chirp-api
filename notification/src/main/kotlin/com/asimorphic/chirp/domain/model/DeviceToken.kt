package com.asimorphic.chirp.domain.model

import com.asimorphic.chirp.domain.model.enums.Platform
import com.asimorphic.chirp.domain.type.UserId
import java.time.Instant

data class DeviceToken(
    val id: Long,
    val userId: UserId,
    val token: String,
    val platform: Platform,
    val createdAt: Instant = Instant.now()
)