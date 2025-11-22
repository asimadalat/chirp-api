package com.asimorphic.chirp.domain.models

import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant
)
