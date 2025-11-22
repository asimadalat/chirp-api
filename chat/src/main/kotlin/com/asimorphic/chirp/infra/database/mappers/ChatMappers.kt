package com.asimorphic.chirp.infra.database.mappers

import com.asimorphic.chirp.domain.models.Chat
import com.asimorphic.chirp.domain.models.ChatMessage
import com.asimorphic.chirp.domain.models.ChatParticipant
import com.asimorphic.chirp.infra.database.entities.ChatEntity
import com.asimorphic.chirp.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        participants = participants.map {
            it.toChatParticipant()
        }.toSet(),
        creator = creator.toChatParticipant(),
        lastMessage = lastMessage,
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePicUrl = profilePicUrl
    )
}