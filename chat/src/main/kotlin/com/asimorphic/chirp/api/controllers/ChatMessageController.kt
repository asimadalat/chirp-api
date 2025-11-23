package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.utils.requestUserId
import com.asimorphic.chirp.domain.type.ChatMessageId
import com.asimorphic.chirp.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat/messages")
class ChatMessageController(private val chatMessageService: ChatMessageService) {

    @DeleteMapping("/{messageId}")
    fun deleteMessage(@PathVariable messageId: ChatMessageId) {
        chatMessageService.deleteMessage(messageId, requestUserId)
    }
}