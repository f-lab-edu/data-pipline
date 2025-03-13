package game.server.lobby.service.v1


import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.dto.v1.response.MatchStatus
import game.server.lobby.service.KafkaEventPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

private const val MATCH_SIZE = 4

@Service
class MultiMatchingServiceV1(
    private val kafkaEventPublisher: KafkaEventPublisher,
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    @Value("\${spring.data.redis.match-queue-key}") private val redisMatchQueue: String
) {

    suspend fun requestMatch(sessionId: String): MatchResponseDto {
        redisTemplate.opsForList().rightPush(redisMatchQueue, sessionId).awaitSingle()

        val sessionIds = redisTemplate.opsForList()
            .range(redisMatchQueue, 0, MATCH_SIZE.toLong() - 1)
            .collectList()
            .awaitSingle()

        return if (sessionIds.size == MATCH_SIZE && sessionIds.contains(sessionId)) {
            redisTemplate.opsForList().trim(redisMatchQueue, MATCH_SIZE.toLong(), -1).awaitFirstOrNull()
            val matchResultId = UUID.randomUUID().toString()

            MatchResponseDto(
                status = MatchStatus.MATCHED,
                matchId = matchResultId,
                sessionIds = sessionIds,
                matchType = MatchType.MULTI
            ).also { kafkaEventPublisher.publishMatchStart(it).awaitFirstOrNull() }

        } else {
            MatchResponseDto(
                status = MatchStatus.WAITING,
                matchId = null,
                sessionIds = emptyList(),
                matchType = MatchType.MULTI
            )
        }
    }

    suspend fun cancelMatch(sessionId: String) {
        redisTemplate.opsForList().remove(redisMatchQueue, 0, sessionId).awaitFirstOrNull()
    }
}
