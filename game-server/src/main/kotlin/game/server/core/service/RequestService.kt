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

    fun process(payload: String): ApiResponse<*> {
        val requestMap: Map<String, Any> = objectMapper.readValue(payload, Map::class.java) as Map<String, Any>
        val type = requestMap["type"] as? String ?: throw IllegalArgumentException("Missing 'type'")

        val handler = requestHandlerFactory.getHandler<Any, Any>(type)
        val request = objectMapper.convertValue(requestMap, handler.requestTypeReference)

        logger.info("{}", request)
        return handler.handle(request)
    }
}