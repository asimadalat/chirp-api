package com.asimorphic.chirp.domain.exception

import com.asimorphic.chirp.domain.type.UserId

class ChatParticipantNotFoundException(
    private val id: UserId
): RuntimeException("Chat participant with ID $id not found.")