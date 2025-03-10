package game.server.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.core.dto.ErrorResponse
import game.server.core.service.RequestService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class WebSocketController(
    private val objectMapper: ObjectMapper,
    private val requestService: RequestService,
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(WebSocketController::class.java)

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val outputFlux = session.receive()
            .map { message ->
                val payloadText = message.payloadAsText
                payloadText
            }
            .asFlow()
            .flatMapMerge(concurrency = 50) { payloadText ->
                handlePayload(session, payloadText, sessionScope)
            }
            .asFlux()

        return session.send(outputFlux)
            .doFinally {
                logger.info("Session(${session.id}) finished")
                sessionScope.cancel()
            }
    }

    private fun handlePayload(
        session: WebSocketSession,
        payloadText: String,
        scope: CoroutineScope
    ): Flow<WebSocketMessage> = flow {
        val response = withContext(scope.coroutineContext) {
            requestService.routeRequest(payloadText)
        }
        logger.info("{}", response)
        val jsonResponse = objectMapper.writeValueAsString(response)
        emit(session.textMessage(jsonResponse))
    }.catch { e ->
        val errorResponse = ErrorResponse.default(e)
        emit(session.textMessage(objectMapper.writeValueAsString(errorResponse)))
    }
}