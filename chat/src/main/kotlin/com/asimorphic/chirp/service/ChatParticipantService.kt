package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.models.ChatParticipant
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.mappers.toChatParticipant
import com.asimorphic.chirp.infra.database.mappers.toChatParticipantEntity
import com.asimorphic.chirp.infra.database.repositories.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(private val chatParticipantRepository: ChatParticipantRepository) {

    fun createChatParticipant(chatParticipant: ChatParticipant) {
        chatParticipantRepository.save(chatParticipant.toChatParticipantEntity())
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(query: String): ChatParticipant? {
        val normalisedQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(query = normalisedQuery)?.toChatParticipant()
    }
}