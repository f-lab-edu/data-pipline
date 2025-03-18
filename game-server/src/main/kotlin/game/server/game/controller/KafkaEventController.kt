package game.server.game.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.KafkaEvent
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import game.server.game.service.MatchedEventService
import game.server.game.service.PlayerMovedEventService
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Controller
class KafkaEventController(
    private val objectMapper: ObjectMapper,
    private val matchedEventService: MatchedEventService,
    private val playerMovedEventService: PlayerMovedEventService,
) : WebSocketHandler {

    override fun handle(socket: WebSocketSession): Mono<Void> =
        socket.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .flatMap { payload ->
                val event = objectMapper.readValue(payload, KafkaEvent::class.java)
                mono { dispatchEvent(event) }.then()
            }.then()

    private suspend fun dispatchEvent(event: KafkaEvent) {
        when (event) {
            is Matched -> matchedEventService.processMatchedEvent(event)
            is PlayerMoved -> playerMovedEventService.processPlayerMovedEvent(event)
            else -> throw IllegalArgumentException("지원되지 않는 이벤트 타입: ${event.eventType}")
        }
    }

}