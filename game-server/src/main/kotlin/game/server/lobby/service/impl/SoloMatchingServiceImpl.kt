package game.server.lobby.service.impl

import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResultDto
import game.server.lobby.service.KafkaEventPublisher
import game.server.lobby.service.MatchingService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*


@Service
class SoloMatchingServiceImpl(
    private val kafkaEventPublisher: KafkaEventPublisher
) : MatchingService {
    override fun requestMatch(userId: String): Mono<MatchResultDto> {
        val matchId = UUID.randomUUID().toString()
        val matchResult = MatchResultDto(matchId, listOf(userId), MatchType.SOLO.name)

        return kafkaEventPublisher.publishMatchStart(matchResult)
            .thenReturn(matchResult)
    }
}