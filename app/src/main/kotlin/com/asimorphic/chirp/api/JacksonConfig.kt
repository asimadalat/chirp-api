package com.asimorphic.chirp.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper = jsonMapper {
        addModule(kotlinModule())
    }
}