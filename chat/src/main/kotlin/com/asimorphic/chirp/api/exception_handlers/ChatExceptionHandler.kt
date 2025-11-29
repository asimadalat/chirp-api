package com.asimorphic.chirp.api.exception_handlers

import com.asimorphic.chirp.domain.exception.ChatMessageNotFoundException
import com.asimorphic.chirp.domain.exception.ChatNotFoundException
import com.asimorphic.chirp.domain.exception.ChatParticipantNotFoundException
import com.asimorphic.chirp.domain.exception.FileStorageException
import com.asimorphic.chirp.domain.exception.InvalidChatSizeException
import com.asimorphic.chirp.domain.exception.InvalidProfilePictureException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

class ChatExceptionHandler {

    @ExceptionHandler(ChatNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onChatNotFound(ex: ChatNotFoundException) = mapOf("code" to "CHAT_NOT_FOUND", "message" to ex.message)

    @ExceptionHandler(ChatParticipantNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onParticipantNotFound(ex: ChatParticipantNotFoundException) = mapOf("code" to "PARTICIPANT_NOT_FOUND", "message" to ex.message)

    @ExceptionHandler(ChatMessageNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onMessageNotFound(ex: ChatMessageNotFoundException) = mapOf("code" to "MESSAGE_NOT_FOUND", "message" to ex.message)

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSize(ex: InvalidChatSizeException) = mapOf("code" to "INVALID_CHAT_SIZE", "message" to ex.message)

    @ExceptionHandler(InvalidProfilePictureException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfilePicture(ex: InvalidProfilePictureException) = mapOf("code" to "INVALID_PROFILE_PICTURE", "message" to ex.message)

    @ExceptionHandler(FileStorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStorageError(ex: FileStorageException) = mapOf("code" to "STORAGE_ERROR", "message" to ex.message)
}