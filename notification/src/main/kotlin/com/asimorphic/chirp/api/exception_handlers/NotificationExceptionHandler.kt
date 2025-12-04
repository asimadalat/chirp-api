package com.asimorphic.chirp.api.exception_handlers

import com.asimorphic.chirp.domain.exception.InvalidDeviceTokenException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {

    @ExceptionHandler(InvalidDeviceTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidDeviceToken(ex: InvalidDeviceTokenException) = mapOf("code" to "INVALID_DEVICE_TOKEN", "message" to ex.message)
}