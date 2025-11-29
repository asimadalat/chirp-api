package com.asimorphic.chirp.api.controllers

import com.asimorphic.chirp.api.dto.ChatParticipantDto
import com.asimorphic.chirp.api.dto.ConfirmProfilePictureRequestDto
import com.asimorphic.chirp.api.dto.ProfilePictureUploadResponse
import com.asimorphic.chirp.api.mappers.toChatParticipantDto
import com.asimorphic.chirp.api.mappers.toResponseDto
import com.asimorphic.chirp.api.utils.requestUserId
import com.asimorphic.chirp.service.ChatParticipantService
import com.asimorphic.chirp.service.ProfilePictureService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/chat/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
    private val profilePictureService: ProfilePictureService
) {

    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam(required = false) query: String?
    ): ChatParticipantDto {
        val participant = if (query == null)
            chatParticipantService.findChatParticipantById((requestUserId))
        else
            chatParticipantService.findChatParticipantByEmailOrUsername(query)

        return participant?.toChatParticipantDto()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/upload-profile-picture")
    fun getProfilePictureUploadUrl(
        @RequestParam mimeType: String
    ): ProfilePictureUploadResponse {
        return profilePictureService.generateUploadCredentials(
            userId = requestUserId,
            mimeType = mimeType
        ).toResponseDto()
    }

    @PostMapping("/confirm-profile-picture")
    fun confirmProfilePictureUpload(
        @Valid @RequestBody body: ConfirmProfilePictureRequestDto
    ) {
        profilePictureService.confirmProfilePictureUpload(
            userId = requestUserId,
            publicUrl = body.publicUrl
        )
    }

    @DeleteMapping("/profile-picture")
    fun deleteProfilePicture() {
        profilePictureService.deleteProfilePicture(
            userId = requestUserId
        )
    }
}