package game.server.lobby.service.v1.matching

import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.dto.v1.response.Matched
import game.server.lobby.dto.v1.response.Waiting
import game.server.lobby.service.v1.matching.util.MatchIdGenerator
import org.springframework.stereotype.Service

private const val MATCH_SIZE = 4

@Service
class MultiMatchingServiceV1(
    private val matchQueueRepository: MatchQueueRepository,
    private val eventPublisher: MatchEventPublisher,
    private val matchIdGenerator: MatchIdGenerator,
) {

    suspend fun requestMatch(sessionId: String): MatchResponseDto {
        matchQueueRepository.addWaitingSession(sessionId)
        val sessionIds = matchQueueRepository.popSessionsIfReady(MATCH_SIZE)

        return if (sessionIds.size == MATCH_SIZE && sessionIds.contains(sessionId)) {
            val matchResultId = matchIdGenerator.generate()

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
        val removedCount = matchQueueRepository.removeWaitingSession(sessionId)
        if (removedCount == 0L) {
            TODO("이미 게임이 시작되어 매칭을 취소할 수 없습니다 토스트 메시지 기능 구현")
        }
    }

}
