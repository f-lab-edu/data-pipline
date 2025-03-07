package game.server.lobby.config

import game.server.lobby.service.SessionManagementService
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI


@Component
class OAuth2SuccessHandler(
    private val sessionManagement: SessionManagementService
) : ServerAuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> =
        mono {
            val (provider, attributes) = extractOAuthDetails(authentication)
            val providerId = generateProviderId(provider, attributes)
            val email = getEmail(attributes)
            val name = getName(attributes)

            val persistedUser = sessionManagement.retrieveOrCreateUser(provider, providerId, email, name)
            val sessionId = sessionManagement.handleUserSession(persistedUser)

            sessionId
        }.flatMap { sessionId ->
            redirectToLobby(webFilterExchange, sessionId)
        }

    private fun redirectToLobby(
        webFilterExchange: WebFilterExchange,
        sessionId: String
    ): Mono<Void> {
        return with(webFilterExchange.exchange.response) {
            statusCode = HttpStatus.FOUND
            headers.location = URI.create("http://localhost:8080")
            headers.add("X-Session-Id", sessionId)
            setComplete()
        }
    }

    private fun extractOAuthDetails(authentication: Authentication): Pair<String, Map<String, Any>> {
        val oauthToken = authentication as OAuth2AuthenticationToken
        val oauthUser = oauthToken.principal as OAuth2User
        return Pair(oauthToken.authorizedClientRegistrationId, oauthUser.attributes)
    }

    private fun generateProviderId(provider: String, attrs: Map<String, Any>): String =
        when (provider.lowercase()) {
            "google" -> attrs["sub"] as String
            "kakao" -> (attrs["id"] as Long).toString()
            "naver" -> (attrs["response"] as Map<*, *>)["id"].toString()
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

    private fun getEmail(attrs: Map<String, Any>): String =
        attrs["email"]?.toString() ?: throw IllegalStateException("Email claim missing")

    private fun getName(attrs: Map<String, Any>): String =
        attrs["name"]?.toString() ?: "Unknown User"
}