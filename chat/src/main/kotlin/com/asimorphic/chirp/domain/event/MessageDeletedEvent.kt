package com.asimorphic.chirp.domain.event

import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId
)
