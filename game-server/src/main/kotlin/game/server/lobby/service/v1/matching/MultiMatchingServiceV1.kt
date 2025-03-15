package game.server.lobby.service.v1.matching

import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.dto.v1.response.Matched
import game.server.lobby.dto.v1.response.Waiting
import org.springframework.stereotype.Service
import java.util.*

private const val MATCH_SIZE = 4

@Service
class MultiMatchingServiceV1(
    private val matchQueueRepository: MatchQueueRepository,
    private val eventPublisher: MatchEventPublisher
) {

    suspend fun requestMatch(sessionId: String): MatchResponseDto {
        matchQueueRepository.addWaitingSession(sessionId)

        val sessionIds = matchQueueRepository.getWaitingSessions(MATCH_SIZE.toLong())

        return if (sessionIds.size == MATCH_SIZE && sessionIds.contains(sessionId)) {
            matchQueueRepository.removeWaitingSessions(sessionIds)
            val matchResultId = UUID.randomUUID().toString()

            Matched(
                matchId = matchResultId,
                sessionIds = sessionIds,
                matchType = MatchType.MULTI
            ).also { eventPublisher.publishMatchStart(it) }
        } else {
            Waiting(matchType = MatchType.MULTI)
        }
    }

    suspend fun cancelMatch(sessionId: String) {
        matchQueueRepository.removeWaitingSession(sessionId)
    }
}