package com.asimorphic.chirp.infra.storage

import com.asimorphic.chirp.domain.exception.FileStorageException
import com.asimorphic.chirp.domain.exception.InvalidProfilePictureException
import com.asimorphic.chirp.domain.models.ProfilePicUploadCredential
import com.asimorphic.chirp.domain.models.SignedFileUploadResponse
import com.asimorphic.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.UUID

@Service
class SupabaseStorageService(
    @param:Value($$"${supabase.url}")
    private val supabaseUrl: String,
    private val supabaseRestClient: RestClient
) {

    companion object {
        private val allowedMimeTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp"
        )
    }

    fun generateSignedUploadUrl(
        userId: UserId,
        mimeType: String
    ): ProfilePicUploadCredential {
        val extension = allowedMimeTypes[mimeType]
            ?:throw InvalidProfilePictureException(
                "Invalid MIME type: $mimeType"
            )

        val filename = "user_${userId}_${UUID.randomUUID()}.$extension"
        val path = "profile-pictures/$filename"
        val publicUrl = "$supabaseUrl/storage/v1/object/public/$path"

        return ProfilePicUploadCredential(
            uploadUrl = createSignedUrl(path = path),
            publicUrl = publicUrl,
            headers = mapOf("Content-Type" to mimeType),
            expiresAt = Instant.now().plusSeconds(300)
        )
    }

    private fun createSignedUrl(path: String): String {
        val json = """
            { "expires-in": 300}
        """.trimIndent()

        val response = supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$path")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(SignedFileUploadResponse::class.java)
            ?: throw FileStorageException("Failed to create signed URL")

        return "$supabaseUrl/storage/v1${response.url}"
    }

    fun deleteFile(url: String) {
        val path = if (url.contains("/object/public/"))
            url.substringAfter("/object/public/")
        else
            throw FileStorageException("Invalid file URL format")

        val deleteUrl = "/storage/v1/object/$path"
        val response = supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if (response.statusCode.isError)
            throw FileStorageException("File delete operation failed")
    }
}