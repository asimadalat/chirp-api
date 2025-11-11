package com.asimorphic.chirp.infra.rate_limiters

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val  EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val  EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }

    fun withRateLimit(email: String, action: () -> Unit) {
        val normalisedEmail = email.lowercase().trim()
        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalisedEmail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalisedEmail"


    }
}