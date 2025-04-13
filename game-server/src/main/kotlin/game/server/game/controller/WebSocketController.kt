package game.server.game.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.util.coroutine.WebSocketSessionContext
import game.server.game.dto.v1.request.ApiRequest
import game.server.game.session.WebSocketSessionManager
import game.server.game.dto.v1.response.ErrorResponse
import game.server.game.service.RequestService
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
        val sessionKey = socket.handshakeInfo.uri.query.let { query -> query.split("=")[1] }
        sessionManager.register(sessionKey, socket)

        val sessionContext = WebSocketSessionContext()

        val outputFlux = socket.receive()
            .map { msg -> msg.payloadAsText }.asFlow()
            .flatMapMerge(concurrency = 50) { payload ->
                handlePayload(socket, payload, sessionContext)
            }
            .asFlux()

        return socket.send(outputFlux).doFinally {
            sessionContext.close()
            sessionManager.remove(sessionKey)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handlePayload(
        socket: WebSocketSession,
        payloadText: String,
        context: WebSocketSessionContext
    ): Flow<WebSocketMessage> = flow {
        val request = objectMapper.readValue(payloadText, ApiRequest::class.java) as ApiRequest<Any>
        logger.info("{}", request)

        val response = context.async {
            requestService.routeRequest(request, socket)
        }.await()

        val jsonResponse = objectMapper.writeValueAsString(response)
        emit(socket.textMessage(jsonResponse))
    }.catch { e ->
        val errorResponse = ErrorResponse.default(e)
        emit(socket.textMessage(objectMapper.writeValueAsString(errorResponse)))
    }
}