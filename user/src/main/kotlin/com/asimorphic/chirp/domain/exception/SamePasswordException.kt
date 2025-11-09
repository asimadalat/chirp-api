package com.asimorphic.chirp.domain.exception

class SamePasswordException: RuntimeException(
    "New password must not equal existing password."
)