package game.server.game.controller

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.game.session.WebSocketSessionManager
import game.server.game.dto.v1.response.ErrorResponse
import game.server.game.service.RequestService
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
    private val sessionManager: WebSocketSessionManager,
    ) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(WebSocketController::class.java)

    override fun handle(socket: WebSocketSession): Mono<Void> {
        val sessionKey = socket.handshakeInfo.uri.query
            .let { query ->
                query.split("=")[1]
            }
        sessionManager.register(sessionKey, socket)

        val socketScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val outputFlux = socket.receive()
            .map { message ->
                message.payloadAsText
            }
            .asFlow()
            .flatMapMerge(concurrency = 50) { payloadText ->
                handlePayload(socket, payloadText, socketScope)
            }
            .asFlux()

        return socket.send(outputFlux)
            .doFinally {
                logger.info("Session(${socket.id}) finished")
                socketScope.cancel()
                sessionManager.remove(sessionKey)
            }
    }

    private fun handlePayload(
        socket: WebSocketSession,
        payloadText: String,
        scope: CoroutineScope
    ): Flow<WebSocketMessage> = flow {
        val response = withContext(scope.coroutineContext) {
            requestService.routeRequest(payloadText)
        }
        logger.info("{}", response)
        val jsonResponse = objectMapper.writeValueAsString(response)
        emit(socket.textMessage(jsonResponse))
    }.catch { e ->
        val errorResponse = ErrorResponse.default(e)
        emit(socket.textMessage(objectMapper.writeValueAsString(errorResponse)))
    }
}