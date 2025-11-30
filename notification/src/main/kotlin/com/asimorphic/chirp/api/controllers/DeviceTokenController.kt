package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.dto.DeviceTokenDto
import com.asimorphic.chirp.api.dto.RegisterDeviceRequestDto
import com.asimorphic.chirp.api.mappers.toDeviceTokenDto
import com.asimorphic.chirp.api.mappers.toPlatform
import com.asimorphic.chirp.api.utils.requestUserId
import com.asimorphic.chirp.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequestDto
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatform()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/deregister/{token}")
    fun deregisterDeviceToken(
        @PathVariable("token") token: String
    ) = pushNotificationService.deregisterDevice(token)
}