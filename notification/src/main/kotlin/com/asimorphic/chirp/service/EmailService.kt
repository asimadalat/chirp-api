package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.type.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value($$"${chirp.email.sender}")
    private val sender: String,
    @param:Value($$"${chirp.email.url}")
    private val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun buildVerificationEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String
    ) {
        logger.info("Sending verification email for user $userId")

        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/verify-email")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/email-verification",
            variables = mapOf(
                "username" to username,
                "verificationUrl" to verificationUrl
            )
        )

        sendHtmlEmail(receiver = email, subject = "Chirp - Account Activation Required", html = htmlContent)
    }

    fun buildPasswordResetEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String,
        expiresIn: Duration
    ) {
        logger.info("Sending password reset email for user $userId")

        val passwordResetUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/reset-password")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/password-reset",
            variables = mapOf(
                "username" to username,
                "passwordResetUrl" to passwordResetUrl,
                "expiresInMinutes" to expiresIn.toMinutes()
            )
        )

        sendHtmlEmail(receiver = email, subject = "Chirp - Password Reset Request", html = htmlContent)
    }

    private fun sendHtmlEmail(receiver: String, subject: String, html: String) {
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(sender)
            setTo(receiver)
            setSubject(subject)
            setText(html, true)
        }

        try {
            javaMailSender.send(message)
        } catch (ex: Exception) {
            logger.error("Error sending email", ex)
        }
    }
}