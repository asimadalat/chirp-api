package com.asimorphic.chirp.service

import com.asimorphic.chirp.domain.type.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class ChatMessageCacheEvictor {
    @CacheEvict(value = ["messages"], key = "#chatId")
    fun evictCachedMessages(chatId: ChatId) {
        // NO-OP: Annotation handles eviction
    }
}