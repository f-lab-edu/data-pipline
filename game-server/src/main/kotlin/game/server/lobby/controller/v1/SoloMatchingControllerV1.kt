package game.server.lobby.controller.v1

import com.game.dto.v1.maching.MatchResponseDto
import game.server.lobby.service.v1.matching.SoloMatchingServiceV1
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

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
