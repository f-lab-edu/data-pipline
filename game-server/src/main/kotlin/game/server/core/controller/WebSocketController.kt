package game.server.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.core.dto.ErrorResponse
import game.server.core.service.RequestService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketController(
    private val objectMapper: ObjectMapper,
    private val requestService: RequestService,
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(WebSocketController::class.java)
    private val sessionMap = ConcurrentHashMap<String, WebSocketSession>()

    override fun handle(session: WebSocketSession): Mono<Void> {

        sessionMap[session.id] = session

        return session.send(
            session.receive()
                .flatMap { message ->
                    val payload = message.payloadAsText
                    Mono.fromCallable {
                        requestService.routeRequest(payload)
                    }
                        .doOnSuccess { response -> logger.info("{}", response) }
                        .onErrorResume { e ->
                            Mono.just(ErrorResponse.default(e))
                        }
                }
                .map { response ->
                    session.textMessage(objectMapper.writeValueAsString(response))
                }
        )
    }

}