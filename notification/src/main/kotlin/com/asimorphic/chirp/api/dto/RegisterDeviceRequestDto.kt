package com.asimorphic.chirp.api.dto

import com.asimorphic.chirp.api.dto.enums.PlatformDto
import jakarta.validation.constraints.NotBlank

data class RegisterDeviceRequestDto(
    @field:NotBlank
    val token: String,

    val platform: PlatformDto
)
