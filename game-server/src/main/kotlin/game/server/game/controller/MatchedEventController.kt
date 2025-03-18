package game.server.game.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.Matched
import game.server.game.service.MatchedEventService
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Controller
class MatchedEventController(
    private val objectMapper: ObjectMapper,
    private val matchedEventService: MatchedEventService,
) : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> = session.receive()
        .map(WebSocketMessage::getPayloadAsText)
        .flatMap { payload ->
            val matchedEvent = objectMapper.readValue(payload, Matched::class.java)
            mono { matchedEventService.processMatchedEvent(matchedEvent) }.then()
        }.then()
}