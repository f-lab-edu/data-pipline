package game.infra.consumer

import com.game.dto.v1.UserSession
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository

@Repository
open class RedisUserSessionRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, UserSession>
) {
    suspend fun findBySessionId(sessionIds: List<String>): List<UserSession> = coroutineScope {
        sessionIds.map { sessionId ->
            async {
                redisTemplate.opsForValue().get(sessionId).awaitSingleOrNull()
            }
        }.awaitAll().filterNotNull()
    }
}