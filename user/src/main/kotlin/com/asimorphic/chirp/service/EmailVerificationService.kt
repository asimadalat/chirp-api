package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.exception.InvalidTokenException
import com.asimorphic.chirp.domain.exception.UserNotFoundException
import com.asimorphic.chirp.domain.model.EmailVerificationToken
import com.asimorphic.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.asimorphic.chirp.infra.database.mappers.toEmailVerificationToken
import com.asimorphic.chirp.infra.database.mappers.toUser
import com.asimorphic.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.asimorphic.chirp.infra.database.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value($$"${chirp.email.verification.expiry-hours}") private val expiryHours: Long
) {
    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmailToken(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid.")

        if (verificationToken.isUsed)
            throw InvalidTokenException("Email verification token has already been used.")

        if (verificationToken.isExpired)
            throw InvalidTokenException("Email verification token has expired.")

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )
        userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            }
        ).toUser()
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(now = Instant.now())
    }
}