package game.server.infra

import game.server.lobby.service.v1.matching.MatchQueueRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisMatchQueueRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    @Value("\${spring.data.redis.match-queue-key}") private val redisMatchQueue: String
) : MatchQueueRepository {

    override suspend fun addWaitingSession(sessionId: String) {
        redisTemplate.opsForList()
            .rightPush(redisMatchQueue, sessionId)
            .awaitSingle()
    }

    override suspend fun getWaitingSessions(limit: Long): List<String> =
        redisTemplate.opsForList()
            .range(redisMatchQueue, 0, limit - 1)
            .collectList()
            .awaitSingle()

    override suspend fun removeWaitingSessions(sessionIds: List<String>) {
        coroutineScope {
            sessionIds.map { id ->
                async {
                    redisTemplate.opsForList()
                        .remove(redisMatchQueue, 0, id)
                        .awaitFirstOrNull()
                }
            }.awaitAll()
        }
    }

    override suspend fun removeWaitingSession(sessionId: String) {
        redisTemplate.opsForList()
            .remove(redisMatchQueue, 0, sessionId)
            .awaitFirstOrNull()
    }
}