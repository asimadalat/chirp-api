package com.asimorphic.chirp.infra.database.repositories

import com.asimorphic.chirp.domain.model.UserId
import com.asimorphic.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, UserId>{
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}