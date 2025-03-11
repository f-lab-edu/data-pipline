package game.server.lobby.controller.v1

import game.server.lobby.dto.v1.response.MatchResultDto
import game.server.lobby.service.MatchingService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

const val SESSION_ID_HEADER = "X-Session-Id"

@RestController
@RequestMapping("/api/v1/match")
class SoloMatchingControllerV1(
    @Qualifier("soloMatchingService") private val soloMatchingService: MatchingService
) {
    @PostMapping("/solo", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun requestSoloMatch(@RequestHeader(SESSION_ID_HEADER) userId: String): Mono<MatchResultDto> {
        return soloMatchingService.requestMatch(userId)
    }
}