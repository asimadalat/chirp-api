package com.asimorphic.chirp.api.websockets

import com.asimorphic.chirp.api.dto.websocket.ChatParticipantsChangeDto
import com.asimorphic.chirp.api.dto.websocket.DeleteMessageDto
import com.asimorphic.chirp.api.dto.websocket.ErrorDto
import com.asimorphic.chirp.api.dto.websocket.IncomingWebSocketMessage
import com.asimorphic.chirp.api.dto.websocket.IncomingWebSocketMessageType
import com.asimorphic.chirp.api.dto.websocket.OutgoingWebSocketMessage
import com.asimorphic.chirp.api.dto.websocket.OutgoingWebSocketMessageType
import com.asimorphic.chirp.api.dto.websocket.SendMessageDto
import com.asimorphic.chirp.api.mappers.toChatMessageDto
import com.asimorphic.chirp.domain.event.ChatParticipantLeftEvent
import com.asimorphic.chirp.domain.event.ChatParticipantsJoinedEvent
import com.asimorphic.chirp.domain.event.MessageDeletedEvent
import com.asimorphic.chirp.domain.type.ChatId
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.service.ChatMessageService
import com.asimorphic.chirp.service.ChatService
import com.asimorphic.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import tools.jackson.core.JacksonException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val jwtService: JwtService,
    private val chatService: ChatService,
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper
): TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session with ID ${session.id} was closed due to missing Authorization header.")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication FAILED."))
                return
            }

        val userId = jwtService.getUserIdFromToken(authHeader)
        val userSession = UserSession(
            userId = userId,
            session = session
        )

        val chatIds = chatService.getChatsByUser(userId).map { it.id }

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) {_, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChatIds.computeIfAbsent(userId) {
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIds)
                }
            }

            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("Websocket connection established for user with ID $userId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message ${message.payload}.")

        val userSession = connectionLock.read {
            sessions[session.id] ?: return
        }

        try {
            val webSocketMessage = objectMapper.readValue(
                message.payload,
                IncomingWebSocketMessage::class.java
            )
            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val msgInfo = objectMapper.readValue(
                        webSocketMessage.payload,
                        SendMessageDto::class.java
                    )
                    handleSendMessage(
                        msgInfo = msgInfo,
                        senderId = userSession.userId
                    )
                }
            }
        } catch (ex: JacksonException) {
            logger.warn("Message parsing FAILED: ${message.payload}")
            sendError(
                session = userSession.session,
                error = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming JSON or UUID is invalid."
                )
            )
        }
    }

    private fun handleSendMessage(msgInfo: SendMessageDto, senderId: UserId) {
        val userChats = connectionLock.read { this@ChatWebSocketHandler.userChatIds[senderId] } ?: return
        if (msgInfo.chatId !in userChats)
            return

        val savedMessage = chatMessageService.sendMessage(
            chatId = msgInfo.chatId,
            messageId = msgInfo.messageId,
            content = msgInfo.content,
            senderId = senderId
        )

        broadcastToChat(
            chatId = msgInfo.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(
                    savedMessage.toChatMessageDto()
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
                payload = objectMapper.writeValueAsString(
                    DeleteMessageDto(
                        chatId = event.chatId,
                        messageId = event.messageId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantsJoinedEvent) {
        connectionLock.write {
            event.userIds.forEach { userId ->
                userChatIds.compute(userId) {_, chatIds ->
                    (chatIds ?: mutableSetOf()).apply { add(event.chatId) }
                }

                userToSessions[userId]?.forEach { sessionId ->
                    chatToSessions.compute(event.chatId) { _, sessions ->
                        (sessions ?: mutableSetOf()).apply { add(sessionId) }
                    }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangeDto(chatId = event.chatId)
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeaveChat(event: ChatParticipantLeftEvent) {
        connectionLock.write {
            userChatIds.compute(event.userId) { _, chatIds ->
                chatIds?.apply { remove(event.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }

            userToSessions[event.userId]?.forEach { sessionId ->
                chatToSessions.compute(event.chatId) { _, sessions ->
                    sessions?.apply { remove(sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangeDto(chatId = event.chatId)
                )
            )
        )
    }

    private fun broadcastToChat(chatId: ChatId, message: OutgoingWebSocketMessage) {
        val chatSessions = connectionLock.read { chatToSessions[chatId]?.toList() ?: emptyList() }
        chatSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach

            sendToUser(
                userId = userSession.userId,
                message = message
            )
        }
    }

    private fun sendToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSessions = connectionLock.read {
            userToSessions[userId] ?: emptySet()
        }

        userSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId] ?: return@forEach
            }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.debug("Sent message to user with ID {}: {}", userId, messageJson)
                } catch (ex: Exception) {
                    logger.error("Error while sending message to user with ID $userId", ex)
                }
            }
        }
    }

    private fun sendError(session: WebSocketSession, error: ErrorDto) {
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(error)
            )
        )

        try {
            session.sendMessage(TextMessage(webSocketMessage))
        } catch (ex: Exception) {
            logger.warn("Could not send error message.")
        }
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )
}