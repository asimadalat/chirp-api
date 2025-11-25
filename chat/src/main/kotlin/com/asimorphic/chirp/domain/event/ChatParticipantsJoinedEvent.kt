package com.asimorphic.chirp.domain.event

import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)