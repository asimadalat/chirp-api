package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.exception.ChatMessageNotFoundException
import com.asimorphic.chirp.domain.exception.ChatNotFoundException
import com.asimorphic.chirp.domain.exception.ChatParticipantNotFoundException
import com.asimorphic.chirp.domain.exception.ForbiddenException
import com.asimorphic.chirp.domain.models.ChatMessage
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.ChatMessageId
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.entities.ChatMessageEntity
import com.asimorphic.chirp.infra.database.mappers.toChatMessage
import com.asimorphic.chirp.infra.database.repositories.ChatMessageRepository
import com.asimorphic.chirp.infra.database.repositories.ChatParticipantRepository
import com.asimorphic.chirp.infra.database.repositories.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {

    @Transactional
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        return chatMessageRepository.save(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        ).toChatMessage()
    }

    @Transactional
    fun deleteMessage(messageId: ChatMessageId, requestUserId: UserId) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw ChatMessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId)
            throw ForbiddenException()

        chatMessageRepository.delete(message)
    }
}