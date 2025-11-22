package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.domain.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePicUrl: String?
)
