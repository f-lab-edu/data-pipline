package game.server.game.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.KafkaEvent
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import game.server.game.service.MatchedEventService
import game.server.game.service.PlayerMovedEventService
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Import(ObjectMapper::class)
@Controller
class KafkaEventController(
    private val objectMapper: ObjectMapper,
    private val matchedEventService: MatchedEventService,
    private val playerMovedEventService: PlayerMovedEventService,
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun handle(socket: WebSocketSession): Mono<Void> =
        socket.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .flatMap { payload ->
                val event = objectMapper.readValue(payload, KafkaEvent::class.java)
                logger.info("kafkaEventCOntroller ========{}", event)
                mono { dispatchEvent(event) }.then()
            }.then()


    private suspend fun dispatchEvent(event: KafkaEvent) {
        logger.info("================================kafkaEventCOntroller ========{}", event)
        when (event) {
            is Matched -> matchedEventService.processMatchedEvent(event)
            is PlayerMoved -> playerMovedEventService.processPlayerMovedEvent(event)
            else -> throw IllegalArgumentException("지원되지 않는 이벤트 타입: ${event.eventType}")
        }
    }

}