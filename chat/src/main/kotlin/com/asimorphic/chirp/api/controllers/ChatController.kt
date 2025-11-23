package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.dto.AddChatParticipantRequestDto
import com.asimorphic.chirp.api.dto.ChatDto
import com.asimorphic.chirp.api.dto.CreateChatRequestDto
import com.asimorphic.chirp.api.mappers.toChatDto
import com.asimorphic.chirp.api.utils.requestUserId
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    @PostMapping
    fun createChat(@Valid @RequestBody body: CreateChatRequestDto): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }

    @PostMapping("/{chatId}/add")
    fun addChatParticipants(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddChatParticipantRequestDto
    ): ChatDto {
        return chatService.addParticipantsToChat(
            requestUserId = requestUserId,
            chatId = chatId,
            userIds = body.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChatById(@PathVariable chatId: ChatId) {
        return chatService.removeParticipantsFromChat(
            userId = requestUserId,
            chatId = chatId
        )
    }
}