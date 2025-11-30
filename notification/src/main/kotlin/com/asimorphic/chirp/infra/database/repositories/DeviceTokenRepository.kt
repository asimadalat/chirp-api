package com.asimorphic.chirp.infra.database.repositories

import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.entities.DeviceTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository: JpaRepository<DeviceTokenEntity, Long> {
    fun findByUserIdIn(userIds: List<UserId>): List<DeviceTokenEntity>
    fun findByToken(token: String): DeviceTokenEntity?
    fun deleteByToken(token: String)
}