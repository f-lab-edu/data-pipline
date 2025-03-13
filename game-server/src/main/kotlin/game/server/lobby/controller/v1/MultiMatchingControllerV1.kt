package game.server.lobby.controller.v1

import game.server.lobby.dto.v1.response.MatchResponseDto
import game.server.lobby.service.v1.MultiMatchingServiceV1
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/match")
class MultiMatchingControllerV1(
    private val multiMatchingService: MultiMatchingServiceV1
) {

    @PostMapping("/multi", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun requestMultiMatch(@RequestHeader(SESSION_ID_HEADER) sessionId: String): MatchResponseDto =
        multiMatchingService.requestMatch(sessionId)


    @DeleteMapping("/multi", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    suspend fun cancelMultiMatch(@RequestHeader(SESSION_ID_HEADER) sessionId: String): Unit =
        multiMatchingService.cancelMatch(sessionId)
}