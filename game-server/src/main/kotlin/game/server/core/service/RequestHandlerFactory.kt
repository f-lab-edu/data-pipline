package game.server.core.service

import org.springframework.stereotype.Component

@Component
class RequestHandlerFactory(
    private val handlers: Map<String, RequestHandler<*, *>>
) {
    @Suppress("UNCHECKED_CAST")
    fun <D, R> getHandler(type: String): RequestHandler<D, R> {
        return handlers[type] as? RequestHandler<D, R>
            ?: throw IllegalArgumentException("Unknown request type: $type")
    }
}