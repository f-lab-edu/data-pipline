package game.server.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.dto.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RequestService(
    private val objectMapper: ObjectMapper,
    private val requestHandlerFactory: RequestHandlerFactory
) {

    private val logger = LoggerFactory.getLogger(RequestService::class.java)

    fun routeRequest(payload: String): ApiResponse<*> {
        val rootNode = objectMapper.readTree(payload)
        val type = rootNode["type"].asText() ?: throw IllegalArgumentException("Missing 'type'")

        val handler = requestHandlerFactory.getHandler<Any, Any>(type)
        val request = objectMapper.convertValue(rootNode, handler.requestTypeReference)

        logger.info("{}", request)
        return handler.handle(request)
    }
}