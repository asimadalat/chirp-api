package com.asimorphic.chirp.infra.message_queue

import com.asimorphic.chirp.domain.events.user.UserEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationUserEventListener {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> println("User created!")
            is UserEvent.RequestResendVerification -> println("Request resend verification")
            is UserEvent.RequestResetPassword -> println("Request resend password!")
            else -> Unit
        }
    }
}