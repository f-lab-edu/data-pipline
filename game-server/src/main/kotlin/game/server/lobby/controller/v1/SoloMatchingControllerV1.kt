package game.server.lobby.controller.v1

import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.service.v1.SoloMatchingServiceV1
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

const val SESSION_ID_HEADER = "X-Session-Id"

@RestController
@RequestMapping("/api/v1/match")
class SoloMatchingControllerV1(
    private val soloMatchingService: SoloMatchingServiceV1
) {
    @PostMapping("/solo", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun requestSoloMatch(@RequestHeader(SESSION_ID_HEADER) sessionId: String): MatchResponseDto =
        soloMatchingService.requestMatch(sessionId)
}
