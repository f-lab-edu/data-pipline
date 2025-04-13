package game.server.game.service

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.game.dto.v1.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class RequestService(
    private val objectMapper: ObjectMapper,
    private val requestHandlerFactory: RequestHandlerFactory
) {

    private val logger = LoggerFactory.getLogger(RequestService::class.java)

    suspend fun routeRequest(payload: String, socket: WebSocketSession): ApiResponse<*> {
        val rootNode = objectMapper.readTree(payload)
        val type = rootNode["type"].asText() ?: throw IllegalArgumentException("Missing 'type'")

        val handler = requestHandlerFactory.getHandler<Any, Any>(type)
        val request = objectMapper.convertValue(rootNode, handler.requestTypeReference)

        logger.info("{}", request)
        return handler.handle(request, socket)
    }
}