package game.server.lobby.service

import game.server.lobby.dto.v1.response.MatchResultDto
import reactor.core.publisher.Mono

interface MatchingService {
    fun requestMatch(userId: String): Mono<MatchResultDto>
}