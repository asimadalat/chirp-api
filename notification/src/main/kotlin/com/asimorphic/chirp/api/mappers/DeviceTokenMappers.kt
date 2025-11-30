package com.asimorphic.chirp.api.mappers

import com.asimorphic.chirp.api.dto.DeviceTokenDto
import com.asimorphic.chirp.domain.model.DeviceToken

fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto {
    return DeviceTokenDto(
        userId = userId,
        token = token,
        createdAt = createdAt
    )
}