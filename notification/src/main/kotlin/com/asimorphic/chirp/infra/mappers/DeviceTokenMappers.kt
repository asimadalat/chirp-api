package com.asimorphic.chirp.infra.mappers

import com.asimorphic.chirp.domain.model.DeviceToken
import com.asimorphic.chirp.infra.database.entities.DeviceTokenEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        id = id,
        userId = userId,
        token = token,
        platform = platform.toPlatform(),
        createdAt = createdAt
    )
}