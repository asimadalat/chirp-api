package com.asimorphic.chirp.domain.exception

import com.asimorphic.chirp.domain.type.ChatMessageId

class ChatMessageNotFoundException(
    private val id: ChatMessageId
): RuntimeException("Message with ID $id not found.")