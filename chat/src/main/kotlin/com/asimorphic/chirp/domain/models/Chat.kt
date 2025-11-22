package com.asimorphic.chirp.domain.models

import com.asimorphic.chirp.domain.type.ChatId
import java.time.Instant

data class Chat(
    val id: ChatId,
    val participants: Set<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val creator: ChatParticipant,
    val createdAt: Instant,
    val lastActivityAt: Instant
)