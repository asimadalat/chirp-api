package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequestDto(
    @field:Size(min = 1, message = "Chats must have at least two unique participants")
    val otherUserIds: List<UserId>
)
