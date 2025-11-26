package com.asimorphic.chirp.api.dto.websocket

import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.ChatMessageId

data class DeleteMessageDto(
    val chatId: ChatId,
    val messageId: ChatMessageId
)