package game.server.lobby.service.v1.matching

import com.game.service.v1.matching.MatchEventPublisher
import com.game.dto.v1.maching.MatchType
import com.game.dto.v1.maching.MatchResponseDto
import com.game.dto.v1.maching.Matched
import game.server.lobby.service.v1.matching.util.MatchIdGenerator
import org.springframework.stereotype.Service

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