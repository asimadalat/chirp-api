package com.asimorphic.chirp.infra.database.repositories

import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.entities.ChatParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatParticipantRepository: JpaRepository<ChatParticipantEntity, UserId> {
    fun findByUserIdIn(userIds: Set<UserId>): Set<ChatParticipantEntity>

    @Query("""SELECT p FROM ChatParticipantEntity p 
        WHERE p.username = :query OR LOWER(p.email) = :query""")
    fun findByEmailOrUsername(query: String): ChatParticipantEntity?
}