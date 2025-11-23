package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddChatParticipantRequestDto(
    @field:Size(min = 1)
    val userIds: List<UserId>
)