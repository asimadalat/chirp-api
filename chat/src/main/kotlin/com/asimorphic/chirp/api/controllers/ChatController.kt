package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.dto.ChatDto
import com.asimorphic.chirp.api.dto.CreateChatRequestDto
import com.asimorphic.chirp.api.mappers.toChatDto
import com.asimorphic.chirp.api.utils.requestUserId
import com.asimorphic.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    fun createChat(@Valid @RequestBody body: CreateChatRequestDto): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
}