package com.asimorphic.chirp.api.exception_handlers

import com.asimorphic.chirp.domain.exception.ForbiddenException
import com.asimorphic.chirp.domain.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CommonExceptionHandler {

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onForbidden(ex: ForbiddenException) = mapOf("code" to "FORBIDDEN", "message" to ex.message)

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onUnauthorized(ex: UnauthorizedException) = mapOf("code" to "UNAUTHORIZED", "message" to ex.message)
}