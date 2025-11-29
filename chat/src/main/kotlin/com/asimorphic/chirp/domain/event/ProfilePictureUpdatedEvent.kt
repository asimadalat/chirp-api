package com.asimorphic.chirp.domain.event

import com.asimorphic.chirp.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?
)
