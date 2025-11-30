package com.asimorphic.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SupabaseRestClientConfig(
    @param:Value($$"${supabase.url}")
    private val supabaseUrl: String,

    @param:Value($$"${supabase.service-key}")
    private val supabaseKey: String
) {

    @Bean
    fun supabaseRestClient(): RestClient {
        return RestClient
            .builder()
            .baseUrl(supabaseUrl)
            .defaultHeader("Authorization", "Bearer $supabaseKey")
            .build()
    }
}