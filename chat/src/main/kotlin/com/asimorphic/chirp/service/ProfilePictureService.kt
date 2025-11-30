package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.event.ProfilePictureUpdatedEvent
import com.asimorphic.chirp.domain.exception.ChatParticipantNotFoundException
import com.asimorphic.chirp.domain.exception.InvalidProfilePictureException
import com.asimorphic.chirp.domain.models.ProfilePicUploadCredential
import com.asimorphic.chirp.domain.type.UserId
import com.asimorphic.chirp.infra.database.repositories.ChatParticipantRepository
import com.asimorphic.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilePictureService(
    @param:Value($$"${supabase.url}")
    private val supabaseUrl: String,

    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

    fun generateUploadCredentials(
        userId: UserId,
        mimeType: String
    ): ProfilePicUploadCredential {
        return supabaseStorageService.generateSignedUploadUrl(
            userId = userId,
            mimeType = mimeType
        )
    }

    @Transactional
    fun deleteProfilePicture(userId: UserId) {
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        participant.profilePicUrl?.let { url ->
            chatParticipantRepository.save(
                participant.apply { profilePicUrl = null }
            )

            supabaseStorageService.deleteFile(url)

            applicationEventPublisher.publishEvent(
                ProfilePictureUpdatedEvent(
                    userId = userId,
                    newUrl = null
                )
            )
        }
    }

    @Transactional
    fun confirmProfilePictureUpload(userId: UserId, publicUrl: String) {
        if (!publicUrl.startsWith(supabaseUrl))
            throw InvalidProfilePictureException("Invalid profile picture URL")

        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        val oldUrl = participant.profilePicUrl

        chatParticipantRepository.save(
            participant.apply { profilePicUrl = publicUrl }
        )

        try {
            oldUrl?.let {
                supabaseStorageService.deleteFile(oldUrl)
            }
        } catch(ex: Exception) {
            logger.warn("Deleting old profile picture for $userId failed", ex)
        }

        applicationEventPublisher.publishEvent(
            ProfilePictureUpdatedEvent(
                userId = userId,
                newUrl = publicUrl
            )
        )
    }
}