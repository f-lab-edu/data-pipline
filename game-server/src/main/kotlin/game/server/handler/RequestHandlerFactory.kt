package game.server.handler

import game.server.dto.request.ApiRequest
import game.server.dto.request.Request
import org.springframework.stereotype.Component

@Component
class RequestHandlerFactory(
    private val handlers: Map<String, RequestHandler<*, *>>
) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Request<*>, R> getHandler(type: String): RequestHandler<T, R> {
        return handlers[type] as? RequestHandler<T, R>
            ?: throw IllegalArgumentException("Unknown request type: $type")
    }
}