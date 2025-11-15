package com.asimorphic.chirp.infra.rate_limiters

import com.asimorphic.chirp.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {
    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { IpAddressMatcher(it) }

        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers: List<IpAddressMatcher> = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy
                proxy.count { it == ':' } >= 2 -> "$proxy/128"
                else -> "$proxy/32"
            }
            IpAddressMatcher(cidr)
        }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip) }
    }

    private fun fromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { matcher ->
            matcher.matches(ip)
        }
    }

    private fun extractFromXRealIp(request: HttpServletRequest, proxyIp: String): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            validateAndNormaliseIp(header, "X-Real-IP", proxyIp)
        }
    }

    private fun validateAndNormaliseIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()
        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.debug("Invalid IP in $headerName: $ip from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddress = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))
                        -> Inet4Address.getByName(trimmedIp)
                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                    return null
                }
            }

            if (isPrivateIp(inetAddress.hostAddress))
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")

            inetAddress.hostAddress
        } catch (ex: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", ex)
            null
        }
    }

    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddress = request.remoteAddr

        if (!fromTrustedProxy(remoteAddress)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddress")
                throw SecurityException("No valid client IP in proxy headers")
            }

            return remoteAddress
        }

        val clientIp = extractFromXRealIp(request, remoteAddress)

        if (clientIp == null) {
            logger.warn("No valid client IP in proxy headers")
            if (nginxConfig.requireProxy)
                throw SecurityException("No valid client IP in proxy headers")
        }

        return clientIp ?: remoteAddress
    }
}