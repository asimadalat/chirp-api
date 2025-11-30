package com.asimorphic.chirp.infra.mappers

import com.asimorphic.chirp.domain.model.enums.Platform
import com.asimorphic.chirp.infra.database.entities.PlatformEntity

fun Platform.toPlatformEntity(): PlatformEntity {
    return when (this) {
        Platform.ANDROID -> PlatformEntity.ANDROID
        Platform.IOS -> PlatformEntity.IOS
    }
}

fun PlatformEntity.toPlatform(): Platform {
    return when (this) {
        PlatformEntity.ANDROID -> Platform.ANDROID
        PlatformEntity.IOS -> Platform.IOS
    }
}