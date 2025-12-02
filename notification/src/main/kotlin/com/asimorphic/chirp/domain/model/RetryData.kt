package com.asimorphic.chirp.domain.model

import java.time.Instant

data class RetryData(
    val notification: PushNotification,
    val attempt: Int,
    val createdAt: Instant
)
