package game.infra

import com.game.service.v1.matching.MatchQueueRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository

@Repository
open class RedisMatchQueueRepository(
    @Qualifier("reactiveStringRedisTemplate")
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    @Value("\${spring.data.redis.match-queue-key}") private val redisMatchQueue: String
) : MatchQueueRepository {

    companion object {
        private val MATCHING_LUA_SCRIPT: RedisScript<List<String>> = RedisScript.of(
            """
            local key = KEYS[1]
            local matchCount = tonumber(ARGV[1])

            local sessions = redis.call('LRANGE', key, 0, matchCount - 1)

            if #sessions == matchCount then
                redis.call('LTRIM', key, matchCount, -1)
                return sessions
            else
                return {}
            end
            """.trimIndent(),
            List::class.java as Class<List<String>>
        )

        private val ADD_IF_NOT_EXISTS_LUA_SCRIPT: RedisScript<Long> = RedisScript.of(
            """
            local key = KEYS[1]
            local sessionId = ARGV[1]
            
            local index = redis.call('LPOS', key, sessionId)

            if (not index) then
                redis.call('RPUSH', key, sessionId)
                return 1
            end
            
            return 0
             """.trimIndent(),
            Long::class.java
        )
    }

    override suspend fun popSessionsIfReady(matchCount: Int): List<String> =
        redisTemplate.execute(
            MATCHING_LUA_SCRIPT,
            listOf(redisMatchQueue),
            listOf(matchCount.toString())
        )
            .collectList()
            .awaitSingle()
            .flatten()

    override suspend fun addWaitingSessionIfNotExists(sessionId: String) {
        redisTemplate.execute(
            ADD_IF_NOT_EXISTS_LUA_SCRIPT,
            listOf(redisMatchQueue),
            listOf(sessionId)
        )
            .awaitSingle()
    }

    override suspend fun removeWaitingSession(sessionId: String): Long =
        redisTemplate.opsForList()
            .remove(redisMatchQueue, 0, sessionId)
            .awaitFirstOrNull() ?: 0L

}