package game.server.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.core.dto.ErrorResponse
import game.server.core.service.RequestService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
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

        val outputFlux = session.receive()
            .asFlow()
            .flatMapMerge(concurrency = 50) { message ->
                processMessage(session, message)
            }
            .asFlux()
        return session.send(outputFlux)
    }

    private fun processMessage(
        session: WebSocketSession,
        message: WebSocketMessage
    ): Flow<WebSocketMessage> = flow {
        try {
            val payload = message.payloadAsText
            val response = withContext(Dispatchers.IO) {
                requestService.routeRequest(payload)
            }
            logger.info("{}", response)

            val jsonResponse = objectMapper.writeValueAsString(response)
            emit(session.textMessage(jsonResponse))
        } catch (e: Exception) {
            val errorResponse = ErrorResponse.default(e)
            logger.error("{}", errorResponse)
            val errorJson = objectMapper.writeValueAsString(errorResponse)
            emit(session.textMessage(errorJson))
        }
    }

}
