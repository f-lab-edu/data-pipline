package game.server.handler

import game.server.dto.Request
import org.springframework.stereotype.Component

@Component
class RequestHandlerFactory(
    private val handlers: Map<String, RequestHandler<*>>
) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Request> getHandler(type: String): RequestHandler<T> {
        return handlers[type] as? RequestHandler<T>
            ?: throw IllegalArgumentException("Unknown request type: $type")
    }
}