package com.asimorphic.chirp.api.dto.websocket

import com.asimorphic.chirp.domain.type.UserId

data class ProfilePictureUpdatedDto(
    val userId: UserId,
    val newUrl: String?
)
