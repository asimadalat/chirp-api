package com.asimorphic.chirp.api.dto

import jakarta.validation.constraints.NotBlank

data class ConfirmProfilePictureRequestDto(
    @field:NotBlank
    val publicUrl: String
)
