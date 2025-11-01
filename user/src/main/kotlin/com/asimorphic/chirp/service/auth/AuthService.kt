package com.asimorphic.chirp.service.auth

import com.asimorphic.chirp.domain.exception.UserAlreadyExistsException
import com.asimorphic.chirp.domain.model.User
import com.asimorphic.chirp.infra.database.entities.UserEntity
import com.asimorphic.chirp.infra.database.mappers.toUser
import com.asimorphic.chirp.infra.database.repositories.UserRepository
import com.asimorphic.chirp.infra.security.PasswordHasher
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
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
}