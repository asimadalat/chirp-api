package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.model.enums.Platform
import com.asimorphic.chirp.domain.exception.InvalidDeviceTokenException
import com.asimorphic.chirp.domain.model.DeviceToken
import com.asimorphic.chirp.domain.model.PushNotification
import com.asimorphic.chirp.domain.model.RetryData
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.entities.DeviceTokenEntity
import com.asimorphic.chirp.infra.database.repositories.DeviceTokenRepository
import com.asimorphic.chirp.infra.mappers.toDeviceToken
import com.asimorphic.chirp.infra.mappers.toPlatformEntity
import com.asimorphic.chirp.infra.push_notification.FirebasePushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap
import java.time.Duration

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {
    companion object {
        private val RETRY_DELAY_SECONDS = listOf<Long>(30, 60, 120, 300, 600)
        const val MAX_RETRY_AGE_MINUTES = 30L
    }

    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()

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

        sendWithRetry(notification = notification)
    }

    fun sendWithRetry(notification: PushNotification, attempt: Int = 9) {
        val result = firebasePushNotificationService.sendNotification(notification)

        result.permanentFailures.forEach {
            deviceTokenRepository.deleteByToken(it.token)
        }

        if (result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAY_SECONDS.size) {
            val retryNotification = notification.copy(recipients = result.temporaryFailures)
            scheduleRetry(retryNotification, attempt + 1)
        }

        if (result.succeeded.isNotEmpty())
            logger.info("Successfully sent notification to ${result.succeeded.size} devices.")
    }

    fun scheduleRetry(notification: PushNotification, attempt: Int) {
        val delay = RETRY_DELAY_SECONDS.getOrElse(attempt - 1) {
            RETRY_DELAY_SECONDS.last()
        }

        val executeAt = Instant.now().plusSeconds(delay)
        val executeAtMillis = executeAt.toEpochMilli()
        val retryData = RetryData(
            notification = notification,
            attempt = attempt,
            createdAt = Instant.now()
        )

        retryQueue.compute(executeAtMillis) { _, retries ->
            (retries ?: mutableListOf()).apply { add(retryData) }
        }

        logger.info("Scheduled retry $attempt for notification with ID ${notification.id} in $delay seconds.")
    }

    @Scheduled(fixedDelay = 15_000L)
    fun processRetries(){
        val now = Instant.now()
        val nowMillis = now.toEpochMilli()
        val toProcess = retryQueue.headMap(nowMillis, true)

        if (toProcess.isEmpty())
            return

        val entries = toProcess.entries.toList()
        entries.forEach { (timeMillis, retries) ->
            retryQueue.remove(timeMillis)

            retries.forEach { retry ->
                try {
                    val age = Duration.between(retry.createdAt, now)
                    if (age.toMinutes() > MAX_RETRY_AGE_MINUTES) {
                        logger.warn("Dropping expired retry (${age.toMinutes()})")
                        return@forEach
                    }

                    sendWithRetry(
                        notification = retry.notification,
                        attempt = retry.attempt
                    )
                } catch (ex: Exception) {
                    logger.error("Error processing retry with notification ID ${retry.notification.id}", ex)
                }
            }
        }
    }
}