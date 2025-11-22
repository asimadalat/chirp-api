package com.asimorphic.chirp.api.mappers

import com.asimorphic.chirp.api.dto.ChatDto
import com.asimorphic.chirp.api.dto.ChatMessageDto
import com.asimorphic.chirp.api.dto.ChatParticipantDto
import com.asimorphic.chirp.domain.models.Chat
import com.asimorphic.chirp.domain.models.ChatMessage
import com.asimorphic.chirp.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map {
            it.toChatParticipantDto()
        },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePicUrl = profilePicUrl
    )
}