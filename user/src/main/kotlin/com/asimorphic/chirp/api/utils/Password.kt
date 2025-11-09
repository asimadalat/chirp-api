package com.asimorphic.chirp.api.utils

import jakarta.validation.Constraint
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@Pattern(
    regexp = "^(?:(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}|[A-Za-z ]{20,})\$",
    message = "Password must be at least 8 characters with at least one number and one symbol, or be a passphrase of 20 or more letters/spaces."
)
annotation class Password(
    val message: String = "Password must be at least 8 characters with at least one number and one symbol, or be a passphrase of 20 or more letters/spaces.",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)
