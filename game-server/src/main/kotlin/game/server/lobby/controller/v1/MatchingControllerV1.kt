package game.server.lobby.controller.v1

import game.server.lobby.dto.v1.response.MatchResultDto
import game.server.lobby.service.MatchingService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/match")
class MatchingControllerV1(
    private val soloMatchingService: MatchingService
) {
    @PostMapping("/solo", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun requestSoloMatch(@RequestHeader("X-Session-Id") userId: String): Mono<MatchResultDto> {
        return soloMatchingService.requestMatch(userId)
    }
}