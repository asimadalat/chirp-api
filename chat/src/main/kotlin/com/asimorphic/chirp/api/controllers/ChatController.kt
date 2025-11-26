package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.dto.AddChatParticipantRequestDto
import com.asimorphic.chirp.api.dto.ChatDto
import com.asimorphic.chirp.api.dto.ChatMessageDto
import com.asimorphic.chirp.api.dto.CreateChatRequestDto
import com.asimorphic.chirp.api.mappers.toChatDto
import com.asimorphic.chirp.api.utils.requestUserId
import com.asimorphic.chirp.domain.exception.ChatNotFoundException
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

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

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable ("chatId") chatId: ChatId
    ): ChatDto {
        return chatService.getChatById(
            chatId = chatId,
            requestUserId = requestUserId
        )?.toChatDto() ?: throw ChatNotFoundException()
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto> {
        return chatService.getChatsByUser(
            userId = requestUserId
        ).map { it.toChatDto() }
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

    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable("chatId") chatId: ChatId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<ChatMessageDto> {
        return chatService.getChatMessages(
            chatId = chatId,
            before = before,
            pageSize = pageSize
        )
    }
}