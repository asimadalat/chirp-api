package com.asimorphic.chirp.service

import com.asimorphic.chirp.api.dto.ChatMessageDto
import com.asimorphic.chirp.api.mappers.toChatMessageDto
import com.asimorphic.chirp.domain.exception.ChatNotFoundException
import com.asimorphic.chirp.domain.exception.ChatParticipantNotFoundException
import com.asimorphic.chirp.domain.exception.ForbiddenException
import com.asimorphic.chirp.domain.exception.InvalidChatSizeException
import com.asimorphic.chirp.domain.models.Chat
import com.asimorphic.chirp.domain.models.ChatMessage
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.entities.ChatEntity
import com.asimorphic.chirp.infra.database.mappers.toChat
import com.asimorphic.chirp.infra.database.mappers.toChatMessage
import com.asimorphic.chirp.infra.database.repositories.ChatMessageRepository
import com.asimorphic.chirp.infra.database.repositories.ChatParticipantRepository
import com.asimorphic.chirp.infra.database.repositories.ChatRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository
) {

    @Transactional
    fun createChat(creatorId: UserId, otherUserIds: Set<UserId>): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = otherParticipants + creatorId
        if (allParticipants.size < 2 )
            throw InvalidChatSizeException()

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null)
    }

    fun getChatMessages(chatId: ChatId, before: Instant?, pageSize: Int): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            ).content.asReversed().map {
                it.toChatMessage().toChatMessageDto()
            }
    }

    @Transactional
    fun addParticipantsToChat(requestUserId: UserId, chatId: ChatId, userIds: Set<UserId>): Chat {
        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }
        if (!isRequestingUserInChat)
            throw ForbiddenException()

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage)

        return updatedChat
    }

    @Transactional
    fun removeParticipantsFromChat(chatId: ChatId, userId: UserId) {
        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException()
        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val updatedParticipantsSize = chat.participants.size - 1
        if (updatedParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}