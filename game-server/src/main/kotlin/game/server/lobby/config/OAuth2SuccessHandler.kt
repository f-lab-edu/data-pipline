package game.server.lobby.config

import com.game.dto.v1.UserDto
import com.game.service.v1.SessionManagement
import game.server.lobby.service.UserService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration


private object SessionConstants {
    const val SESSION_PREFIX = "session:"
    val SESSION_TTL: Duration = Duration.ofHours(1)
}

@Component
class OAuth2SuccessHandler(
    private val sessionManagement: SessionManagement,
    private val userService: UserService,
    @Value("\${server.ip}") private val serverIp: String,
    @Value("\${server.port}") private val serverPort: String
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

            val persistedUserDto = userService.retrieveOrCreateUser(provider, providerId, email, name)
            val sessionKey = getSessionKey(persistedUserDto)
            val sessionId =
                sessionManagement.handleUserSession(
                    persistedUserDto,
                    sessionKey,
                    SessionConstants.SESSION_TTL,
                    serverIp,
                    serverPort
                )
            sessionId
        }.flatMap { sessionId ->
            redirectToLobby(webFilterExchange, sessionId)
        }

    private fun getSessionKey(user: UserDto): String {
        return "${SessionConstants.SESSION_PREFIX}${user.providerId}"
    }

    private fun redirectToLobby(
        webFilterExchange: WebFilterExchange,
        sessionId: String
    ): Mono<Void> {
        return with(webFilterExchange.exchange.response) {
            statusCode = HttpStatus.FOUND
            headers.location = URI.create("http://localhost:8080/index.html?sessionId=$sessionId")
            setComplete()
        }
    }

    private fun extractOAuthDetails(authentication: Authentication): Pair<String, Map<String, Any>> {
        val oauthToken = authentication as? OAuth2AuthenticationToken
            ?: throw IllegalArgumentException("Authentication token is not OAuth2AuthenticationToken")
        val oauthUser = oauthToken.principal
            ?: throw IllegalArgumentException("OAuth2AuthenticationToken has no principal")
        return Pair(oauthToken.authorizedClientRegistrationId, oauthUser.attributes)
    }

    private fun generateProviderId(provider: String, attrs: Map<String, Any>): String =
        OAuth2Provider.from(provider).extractProviderId(attrs)

    private fun getEmail(attrs: Map<String, Any>): String =
        attrs["email"]?.toString() ?: throw IllegalStateException("Email claim missing")

    private fun getName(attrs: Map<String, Any>): String =
        attrs["name"]?.toString() ?: "Unknown User"
}