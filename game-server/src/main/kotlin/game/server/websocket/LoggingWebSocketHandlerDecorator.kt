package game.server.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class LoggingWebSocketHandlerDecorator(
    private val gameRequestRouter: GameRequestRouter,
    private val objectMapper: ObjectMapper
) : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        return gameRequestRouter.handle(LoggableWebSocketSession(session, objectMapper))
    }

    private class LoggableWebSocketSession(
        private val delegate: WebSocketSession,
        private val objectMapper: ObjectMapper
    ) : WebSocketSession by delegate {
        private val logger = LoggerFactory.getLogger(LoggableWebSocketSession::class.java)

        override fun receive(): Flux<WebSocketMessage> = delegate.receive()
            .doOnNext { message ->
                val logData = mapOf(
                    "event" to "websocket_request",
                    "payload" to message.payloadAsText,
                    "timestamp" to System.currentTimeMillis()
                )
                logger.info(objectMapper.writeValueAsString(logData))
            }

        override fun send(messages: Publisher<WebSocketMessage>): Mono<Void> {
            return delegate.send(
                Flux.from(messages)
                    .doOnNext { message ->
                        val logData = mapOf(
                            "event" to "websocket_response",
                            "payload" to message.payloadAsText,
                            "timestamp" to System.currentTimeMillis()
                        )
                        logger.info(objectMapper.writeValueAsString(logData))
                    }
            )
        }

    }
}