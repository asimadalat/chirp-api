package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.annotations.IpRateLimit
import com.asimorphic.chirp.api.dto.AuthenticatedUserDto
import com.asimorphic.chirp.api.dto.EmailRequest
import com.asimorphic.chirp.api.dto.LoginRequest
import com.asimorphic.chirp.api.dto.PasswordChangeRequest
import com.asimorphic.chirp.api.dto.PasswordResetRequest
import com.asimorphic.chirp.api.dto.RefreshRequest
import com.asimorphic.chirp.api.dto.RegisterRequest
import com.asimorphic.chirp.api.dto.UserDto
import com.asimorphic.chirp.api.mappers.toAuthenticatedUserDto
import com.asimorphic.chirp.api.mappers.toUserDto
import com.asimorphic.chirp.infra.rate_limiters.EmailRateLimiter
import com.asimorphic.chirp.service.AuthService
import com.asimorphic.chirp.service.EmailVerificationService
import com.asimorphic.chirp.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val resetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter
) {
    @PostMapping("/register")
    @IpRateLimit(5, 1L, TimeUnit.HOURS)
    fun register(@Valid @RequestBody body: RegisterRequest): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }

    @PostMapping("/login")
    @IpRateLimit(5, 1L, TimeUnit.HOURS)
    fun login(@RequestBody body: LoginRequest): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(5, 1L, TimeUnit.HOURS)
    fun refresh(@RequestBody body: RefreshRequest): AuthenticatedUserDto {
        return authService.refresh(body.refreshToken).toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(@RequestBody body: RefreshRequest) {
        authService.logout(body.refreshToken)
    }

    @PostMapping("/resend-verification")
    @IpRateLimit(5, 1L, TimeUnit.HOURS)
    fun resendVerification(@Valid @RequestBody body: EmailRequest) {
        emailRateLimiter.withRateLimit(body.email) {
            emailVerificationService.resendVerificationEmail(body.email)
        }
    }

    @GetMapping("/verify-email")
    fun verifyEmail(@RequestParam token: String) {
        emailVerificationService.verifyEmailToken(token)
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(5, 1L, TimeUnit.HOURS)
    fun resetPassword(@Valid @RequestBody body: EmailRequest) {
        resetService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody body: PasswordResetRequest) {
        resetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody body: PasswordChangeRequest) {
        // Make authenticated endpoint, extract user ID from JWT, call service
    }
}