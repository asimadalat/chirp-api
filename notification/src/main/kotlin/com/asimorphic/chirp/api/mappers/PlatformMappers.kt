package com.asimorphic.chirp.api.mappers

import com.asimorphic.chirp.api.dto.enums.PlatformDto
import com.asimorphic.chirp.domain.model.enums.Platform

fun PlatformDto.toPlatform(): Platform {
    return when (this) {
        PlatformDto.IOS -> Platform.IOS
        PlatformDto.ANDROID -> Platform.ANDROID
    }
}