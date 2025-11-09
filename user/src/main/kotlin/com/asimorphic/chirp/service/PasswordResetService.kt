package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.exception.InvalidCredentialsException
import com.asimorphic.chirp.domain.exception.InvalidTokenException
import com.asimorphic.chirp.domain.exception.SamePasswordException
import com.asimorphic.chirp.domain.exception.UserNotFoundException
import com.asimorphic.chirp.domain.model.UserId
import com.asimorphic.chirp.infra.database.entities.PasswordResetTokenEntity
import com.asimorphic.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.asimorphic.chirp.infra.database.repositories.RefreshTokenRepository
import com.asimorphic.chirp.infra.database.repositories.UserRepository
import com.asimorphic.chirp.infra.security.PasswordHasher
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher,
    @param:Value($$"${chirp.email.password-reset.expiry-minutes}")
    private val expiryMinutes: Long
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)
        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)

        // Send email with reset link to user registered email address
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Password reset token is invalid.")

        if (resetToken.isUsed)
            throw InvalidTokenException("Password reset token has already been used.")

        if (resetToken.isExpired)
            throw InvalidTokenException("Password reset token has expired.")

        val user = resetToken.user
        if (passwordHasher.verify(newPassword, user.hashedPassword))
            throw SamePasswordException()

        val hashedNewPassword = passwordHasher.hash(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                this.usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(userId: UserId, oldPassword: String, newPassword: String) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        if (passwordHasher.verify(oldPassword, user.hashedPassword))
            throw InvalidCredentialsException()

        if (oldPassword == newPassword)
            throw SamePasswordException()

        refreshTokenRepository.deleteByUserId(user.id!!)

        val hashedNewPassword = passwordHasher.hash(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(now = Instant.now())
    }
}