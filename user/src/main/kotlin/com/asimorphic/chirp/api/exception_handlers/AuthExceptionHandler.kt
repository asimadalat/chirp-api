package com.asimorphic.chirp.api.exception_handlers

import com.asimorphic.chirp.domain.exception.EmailNotVerifiedException
import com.asimorphic.chirp.domain.exception.InvalidCredentialsException
import com.asimorphic.chirp.domain.exception.InvalidTokenException
import com.asimorphic.chirp.domain.exception.RateLimitException
import com.asimorphic.chirp.domain.exception.SamePasswordException
import com.asimorphic.chirp.domain.exception.UserAlreadyExistsException
import com.asimorphic.chirp.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onUserAlreadyExists(ex: UserAlreadyExistsException) = mapOf("code" to "USER_EXISTS", "message" to ex.message)

    @ExceptionHandler(EmailNotVerifiedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onEmailNotVerified(ex: EmailNotVerifiedException) = mapOf("code" to "EMAIL_NOT_VERIFIED", "message" to ex.message)

    @ExceptionHandler(RateLimitException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun onRateLimitExceeded(ex: RateLimitException) = mapOf("code" to "RATE_LIMIT_EXCEEDED", "message" to ex.message)

    @ExceptionHandler(SamePasswordException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onSamePassword(ex: SamePasswordException) = mapOf("code" to "SAME_PASSWORD", "message" to ex.message)

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onUserNotFound(ex: UserNotFoundException) = mapOf("code" to "USER_NOT_FOUND", "message" to ex.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidCredentials(ex: InvalidCredentialsException) = mapOf("code" to "INVALID_CREDENTIALS", "message" to ex.message)

    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidToken(ex: InvalidTokenException) = mapOf("code" to "INVALID_TOKEN", "message" to ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "code" to "VALIDATION_ERROR",
                    "errors" to errors
                )
            )
    }
}