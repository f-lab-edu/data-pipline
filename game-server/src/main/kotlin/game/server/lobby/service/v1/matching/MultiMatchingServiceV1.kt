package game.server.lobby.service.v1.matching

import com.game.service.v1.matching.MatchEventPublisher
import com.game.service.v1.matching.MatchQueueRepository
import com.game.dto.v1.maching.MatchType
import com.game.dto.v1.maching.MatchResponseDto
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.maching.Waiting
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
        matchQueueRepository.addWaitingSessionIfNotExists(sessionId)
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
