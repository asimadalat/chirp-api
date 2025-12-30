package com.asimorphic.chirp.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyFilter(
    @param:Value(value = $$"${chirp.security.api-key.header}")
    private val header: String,

    @param:Value(value = $$"${chirp.security.api-key.value}")
    private val expectedKey: String,
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val keyHeader = request.getHeader(header)

        if (keyHeader == null || keyHeader != expectedKey) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key")
            return
        }

        filterChain.doFilter(request, response)
    }
}