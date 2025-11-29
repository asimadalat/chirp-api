package com.asimorphic.chirp.domain.exception

class FileStorageException(
    override val message: String?
): RuntimeException(
    message ?: "Upload file to storage operation failed"
)