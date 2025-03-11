package game.server.lobby.service.impl

import game.server.lobby.dto.v1.response.MatchResultDto
import game.server.lobby.service.MatchingService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service("multiMatchingService")
class MultiMatchingServiceImpl : MatchingService {

    override fun requestMatch(userId: String): Mono<MatchResultDto> {

    }
}