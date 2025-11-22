package com.asimorphic.chirp.domain.models

import com.asimorphic.chirp.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePicUrl: String?
)
