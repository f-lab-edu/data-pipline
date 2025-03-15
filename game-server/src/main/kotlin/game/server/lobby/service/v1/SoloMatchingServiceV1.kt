package game.server.lobby.service.v1

import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.dto.v1.response.Matched
import game.server.lobby.service.KafkaEventPublisher
import org.springframework.stereotype.Service
import java.util.*


@Service
class SoloMatchingServiceV1(
    private val kafkaEventPublisher: KafkaEventPublisher
) {
    suspend fun requestMatch(sessionId: String): MatchResponseDto {
        val matchId = UUID.randomUUID().toString()

        return Matched(matchId, listOf(sessionId), MatchType.SOLO)
            .also { kafkaEventPublisher.publishMatchStart(it) }
    }
}
