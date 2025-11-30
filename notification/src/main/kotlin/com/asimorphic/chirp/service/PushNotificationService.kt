package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.model.enums.Platform
import com.asimorphic.chirp.domain.exception.InvalidDeviceTokenException
import com.asimorphic.chirp.domain.model.DeviceToken
import com.asimorphic.chirp.domain.model.PushNotification
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.entities.DeviceTokenEntity
import com.asimorphic.chirp.infra.database.repositories.DeviceTokenRepository
import com.asimorphic.chirp.infra.mappers.toDeviceToken
import com.asimorphic.chirp.infra.mappers.toPlatformEntity
import com.asimorphic.chirp.infra.push_notification.FirebasePushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {
    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)

    fun registerDevice(
        userId: UserId,
        token: String,
        platform: Platform
    ): DeviceToken {
        val existing = deviceTokenRepository.findByToken(token)
        val trimmedToken = token.trim()

        if (existing == null && !firebasePushNotificationService.isValidToken(trimmedToken))
            throw InvalidDeviceTokenException()

        val entity = if (existing != null) {
            deviceTokenRepository.save(
                existing.apply {
                    this.userId = userId
                }
            )
        } else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform.toPlatformEntity()
                )
            )
        }

        return entity.toDeviceToken()
    }

    @Transactional
    fun deregisterDevice(token: String) =
        deviceTokenRepository.deleteByToken(token.trim())

    fun sendNewMessageNotifications(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUsername: String,
        chatId: ChatId,
        message: String
    ) {
        val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)
        if (deviceTokens.isEmpty()) {
            logger.info("No device tokens found for users with IDs $recipientUserIds")
            return
        }

        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val notification = PushNotification(
            title = "New message from $senderUsername",
            recipients = recipients,
            message = message,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )

        firebasePushNotificationService.sendNotification(notification)
    }
}