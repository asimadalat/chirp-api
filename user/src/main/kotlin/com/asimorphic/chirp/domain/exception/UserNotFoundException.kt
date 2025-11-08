package com.asimorphic.chirp.domain.exception

class UserNotFoundException: RuntimeException(
    "User not found."
)