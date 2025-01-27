package game.server.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.dto.PlayerMoveRequest
import game.server.handler.RequestHandlerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class GameWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val requestHandlerFactory: RequestHandlerFactory
) : WebSocketHandler {

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
            val type = requestMap["type"] as? String ?: throw IllegalArgumentException("Missing type")
            val data = requestMap["data"] ?: throw IllegalArgumentException("Missing data")

            when (type) {
                "move" -> {
                    val request = objectMapper.convertValue(data, PlayerMoveRequest::class.java)
                    requestHandlerFactory.getHandler<PlayerMoveRequest>("move").handle(request)
                }
                else -> {
                    throw IllegalArgumentException("Unknown request type: $type")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            objectMapper.writeValueAsString(
                mapOf(
                    "type" to "error",
                    "message" to e.message
                )
            )
        }
    }

    fun sendToClient(message: Any) {
        val jsonMessage = objectMapper.writeValueAsString(message)
        currentSession.send(Mono.just(currentSession.textMessage(jsonMessage))).subscribe()
    }
}