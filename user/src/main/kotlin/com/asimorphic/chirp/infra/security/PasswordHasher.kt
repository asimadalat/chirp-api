package com.asimorphic.chirp.infra.security

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordHasher {
    private val argon2 = Argon2PasswordEncoder(16, 32, 1, 1 shl 13, 3)

    fun hash(rawPassword: String): String = argon2.encode(rawPassword) ?: throw IllegalStateException("Failed to hash password")

    fun verify(rawPassword: String, hashedPassword: String): Boolean {
        return argon2.matches(rawPassword, hashedPassword)
    }
}