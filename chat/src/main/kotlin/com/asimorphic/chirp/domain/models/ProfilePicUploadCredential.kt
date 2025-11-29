package com.asimorphic.chirp.domain.models

import java.time.Instant

data class ProfilePicUploadCredential(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>,
    val expiresAt: Instant
)
