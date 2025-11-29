package com.asimorphic.chirp.api.mappers

import com.asimorphic.chirp.api.dto.ProfilePictureUploadResponse
import com.asimorphic.chirp.domain.models.ProfilePicUploadCredential

fun ProfilePicUploadCredential.toResponseDto(): ProfilePictureUploadResponse {
    return ProfilePictureUploadResponse(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers,
        expiresAt = expiresAt
    )
}