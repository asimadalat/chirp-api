package com.asimorphic.chirp.domain.event

import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)