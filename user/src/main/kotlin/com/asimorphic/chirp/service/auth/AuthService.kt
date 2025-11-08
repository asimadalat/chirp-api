package com.asimorphic.chirp.service.auth

import com.asimorphic.chirp.domain.exception.InvalidCredentialsException
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
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun register(email: String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(email.trim(), username.trim())
        if (user != null)
            throw UserAlreadyExistsException()

        val savedUser = userRepository.save(
            UserEntity(
            email = email.trim(),
            username = username.trim(),
            hashedPassword = passwordHasher.hash(password)
            )
        ).toUser()

        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim()) ?: throw InvalidCredentialsException()
        if (!passwordHasher.verify(password, user.hashedPassword))
            throw InvalidCredentialsException()

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
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}