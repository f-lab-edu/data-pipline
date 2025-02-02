package game.server.websocket

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import game.server.dto.request.PlayerMoveRequestData
import game.server.dto.request.Request
import game.server.dto.response.ApiResponse
import game.server.dto.response.ErrorResponse
import game.server.dto.response.MoveResponseData
import game.server.handler.RequestHandlerFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class GameRequestRouter(
    private val objectMapper: ObjectMapper,
    private val requestHandlerFactory: RequestHandlerFactory
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(GameRequestRouter::class.java)
    private lateinit var currentSession: WebSocketSession

    override fun handle(session: WebSocketSession): Mono<Void> {
        currentSession = session

        return session.send(
            session.receive()
                .map { message ->
                    val payload = message.payloadAsText
                    val response = processRequest(payload)
                    session.textMessage(response)
                }
        )
    }

    private fun processRequest(payload: String): String {
        return try {
            val requestMap: Map<String, Any> = objectMapper.readValue(payload, Map::class.java) as Map<String, Any>
            val type = requestMap["type"] as? String ?: throw IllegalArgumentException("Missing 'type' field in request")

            val apiResponse = when (type) {
                "move" -> {
                    val request: Request<PlayerMoveRequestData> =
                        objectMapper.convertValue(requestMap, object : TypeReference<Request<PlayerMoveRequestData>>() {})
                    logger.info("request: {}", request)
                    requestHandlerFactory.getHandler<Request<PlayerMoveRequestData>, MoveResponseData>(request.type).handle(request)
                }
                else -> {
                    throw IllegalArgumentException("Unknown request type: $type")
                }
            }
            objectMapper.writeValueAsString(apiResponse)
        } catch (e: Exception) {
            e.printStackTrace()
            objectMapper.writeValueAsString(ErrorResponse<Nothing>(type = "error", message = e.message ?: "Unknown error"))
        }
    }

    fun <T> sendToClient(message: ApiResponse<T>) {
        logger.info("response: {}", message)
        val jsonMessage = objectMapper.writeValueAsString(message)
        currentSession.send(Mono.just(currentSession.textMessage(jsonMessage))).subscribe()
    }
}