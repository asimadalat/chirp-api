package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.exception.EmailNotVerifiedException
import com.asimorphic.chirp.domain.exception.InvalidCredentialsException
import com.asimorphic.chirp.domain.exception.InvalidTokenException
import com.asimorphic.chirp.domain.exception.UserAlreadyExistsException
import com.asimorphic.chirp.domain.exception.UserNotFoundException
import com.asimorphic.chirp.domain.model.AuthenticatedUser
import com.asimorphic.chirp.domain.model.User
import com.asimorphic.chirp.domain.model.UserId
import com.asimorphic.chirp.infra.database.entities.RefreshTokenEntity
import com.asimorphic.chirp.infra.database.entities.UserEntity
import com.asimorphic.chirp.infra.database.mappers.toUser
import com.asimorphic.chirp.infra.database.repositories.RefreshTokenRepository
import com.asimorphic.chirp.infra.database.repositories.UserRepository
import com.asimorphic.chirp.infra.security.PasswordHasher
import com.asimorphic.chirp.service.EmailVerificationService
import com.asimorphic.chirp.service.JwtService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService
) {
    @Transactional
    fun register(email: String, username: String, password: String): User {
        val emailTrimmed = email.trim()
        val user = userRepository.findByEmailOrUsername(emailTrimmed, username.trim())
        if (user != null)
            throw UserAlreadyExistsException()

        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = emailTrimmed,
                username = username.trim(),
                hashedPassword = passwordHasher.hash(password)
            )
        ).toUser()

        val token = emailVerificationService.createVerificationToken(emailTrimmed)

        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim()) ?: throw InvalidCredentialsException()
        if (!passwordHasher.verify(password, user.hashedPassword))
            throw InvalidCredentialsException()

        if (!user.hasVerifiedEmail)
            throw EmailNotVerifiedException()

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken))
            throw InvalidTokenException(message = "Invalid refresh token.")

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val hashedToken = hashToken(refreshToken)

        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashedToken
            ) ?: throw InvalidTokenException("Invalid refresh token.")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                    userId = userId,
                    hashedToken = hashedToken
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(userId, newRefreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashedToken = hashToken(refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashedToken)
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashedToken = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashedToken
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}