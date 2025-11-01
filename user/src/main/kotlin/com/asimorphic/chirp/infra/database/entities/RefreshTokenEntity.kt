package com.asimorphic.chirp.infra.database.entities

import com.asimorphic.chirp.domain.model.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity
@Table(name = "refresh_tokens", schema = "user_service", indexes = [
    Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    Index(name = "idx_refresh_tokens_user_token", columnList = "user_id,hashed_token")])
class RefreshTokenEntity @OptIn(ExperimentalTime::class) constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var userId: UserId,

    @Column(nullable = false)
    var hashedToken: String,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var createdAt: Instant = Clock.System.now()
)