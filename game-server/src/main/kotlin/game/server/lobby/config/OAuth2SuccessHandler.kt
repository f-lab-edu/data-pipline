package game.server.lobby.config

import game.server.lobby.domain.user.User
import game.server.lobby.domain.user.UserRepository
import kotlinx.coroutines.Dispatchers
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
    private val userRepository: UserRepository
) : ServerAuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> = mono {
        val oauthToken = authentication as OAuth2AuthenticationToken
        val oauthUser = authentication.principal as OAuth2User
        val attributes = oauthUser.attributes

        val provider = oauthToken.authorizedClientRegistrationId
        val providerId = generateProviderId(provider, attributes)
        val email = getEmail(attributes)
        val name = getName(attributes)

        val user = User(
            providerId = providerId,
            provider = provider,
            email = email,
            name = name
        )

        userRepository.findByProviderId(providerId) ?: userRepository.save(user)
    }.then(
        Mono.defer {
            val response = webFilterExchange.exchange.response
            response.statusCode = HttpStatus.FOUND
            response.headers.location = URI.create("http://localhost:8080")
            response.setComplete()
        }
    )

    private fun generateProviderId(provider: String, attrs: Map<String, Any>): String {
        return when(provider.lowercase()) {
            "google" -> attrs["sub"] as String
            "kakao" -> (attrs["id"] as Long).toString()
            "naver" -> (attrs["response"] as Map<*, *>)["id"].toString()
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }
    }

    private fun getEmail(attrs: Map<String, Any>): String {
        return attrs["email"]?.toString()
            ?: throw IllegalStateException("Email claim missing")
    }

    private fun getName(attrs: Map<String, Any>): String {
        return attrs["name"]?.toString() ?: "Unknown User"
    }
}
