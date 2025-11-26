package com.asimorphic.chirp.api.dto.websocket

import com.asimorphic.chirp.domain.type.ChatId

data class ChatParticipantsChangeDto(
    val chatId: ChatId
)