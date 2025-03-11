package game.server.lobby.service

import game.server.lobby.domain.user.User
import game.server.lobby.domain.user.UserRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

private object SessionConstants {
    const val SESSION_PREFIX = "session:"
    val SESSION_TTL: Duration = Duration.ofHours(1)
}

data class UserSession(
    val user: User,
    val serverIp: String,
    val serverPort: Int
)

@Service
class SessionManagementService(
    private val userRepository: UserRepository,
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    @Value("\${server.ip}") private val serverIp: String,
    @Value("\${server.port}") private val serverPort: Int
) {
    suspend fun handleUserSession(user: User): String {
        val sessionKey = getSessionKey(user)
        return if (sessionExists(sessionKey)) {
            renewSession(sessionKey)
            sessionKey
        } else createNewSession(user)
    }

    private fun getSessionKey(user: User): String {
        return "${SessionConstants.SESSION_PREFIX}${user.providerId}"
    }

    private suspend fun sessionExists(sessionKey: String): Boolean {
        return redisTemplate.hasKey(sessionKey).awaitSingle()
    }

    private suspend fun renewSession(sessionKey: String) {
        redisTemplate.expire(sessionKey, SessionConstants.SESSION_TTL).awaitSingle()
    }

    private suspend fun createNewSession(user: User): String {
        val sessionKey = getSessionKey(user)
        val userSession = UserSession(user, serverIp, serverPort)
        redisTemplate.opsForValue().set(sessionKey, userSession, SessionConstants.SESSION_TTL).awaitSingle()
        return sessionKey
    }

    suspend fun retrieveOrCreateUser(
        provider: String,
        providerId: String,
        email: String,
        name: String
    ): User {
        return userRepository.findByProviderId(providerId)
            ?: userRepository.save(
                User(
                    provider = provider,
                    providerId = providerId,
                    email = email,
                    name = name
                )
            )
    }
}