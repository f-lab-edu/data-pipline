package game.infra

import com.game.dto.v1.UserDto
import com.game.dto.v1.UserSession
import com.game.service.v1.SessionManagement
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration


@Repository
open class RedisSessionManagement(
    private val redisTemplate: ReactiveRedisTemplate<String, UserSession>,
) : SessionManagement {

    override suspend fun handleUserSession(
        userDto: UserDto,
        sessionKey: String,
        duration: Duration,
        serverIp: String,
        serverPort: String
    ): String {
        return if (sessionExists(sessionKey)) {
            renewSession(sessionKey, duration)
            sessionKey
        } else createNewSession(sessionKey, userDto, duration, serverIp, serverPort)
    }

    private suspend fun sessionExists(sessionKey: String): Boolean {
        return redisTemplate.hasKey(sessionKey).awaitSingle()
    }

    private suspend fun renewSession(sessionKey: String, duration: Duration) {
        redisTemplate.expire(sessionKey, duration).awaitSingle()
    }

    private suspend fun createNewSession(
        sessionKey: String,
        userDto: UserDto,
        duration: Duration,
        serverIp: String,
        serverPort: String
    ): String {
        val userSession = UserSession(userDto, serverIp, serverPort)
        redisTemplate.opsForValue().set(sessionKey, userSession, duration).awaitSingle()
        return sessionKey
    }
}