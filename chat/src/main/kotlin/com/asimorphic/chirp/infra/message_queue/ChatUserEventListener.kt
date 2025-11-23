package com.asimorphic.chirp.infra.message_queue

import com.asimorphic.chirp.domain.events.user.UserEvent
import com.asimorphic.chirp.domain.models.ChatParticipant
import com.asimorphic.chirp.service.ChatParticipantService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(private val chatParticipantService: ChatParticipantService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        logger.info("Received user event: {}", event)
        when (event) {
            is UserEvent.Verified -> chatParticipantService.createChatParticipant(
                chatParticipant = ChatParticipant(
                    userId = event.userId,
                    username = event.username,
                    email = event.email,
                    profilePicUrl = null
                )
            )
            else -> Unit
        }
    }
}