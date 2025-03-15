package game.server.lobby.service.v1.matching

import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.dto.v1.response.Matched
import game.server.lobby.service.v1.matching.util.MatchIdGenerator
import org.springframework.stereotype.Service
import java.util.*

@Service
class SoloMatchingServiceV1(
    private val eventPublisher: MatchEventPublisher,
    private val matchIdGenerator: MatchIdGenerator
) {
    suspend fun requestMatch(sessionId: String): MatchResponseDto {
        val matchId = matchIdGenerator.generate()

        return Matched(matchId, listOf(sessionId), MatchType.SOLO)
            .also { eventPublisher.publishMatchStart(it) }
    }
}