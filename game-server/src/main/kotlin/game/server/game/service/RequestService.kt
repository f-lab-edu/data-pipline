package game.server.game.service

import game.server.game.dto.v1.request.ApiRequest
import game.server.game.dto.v1.response.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class RequestService(
    private val requestHandlerFactory: RequestHandlerFactory
) {
    suspend fun routeRequest(request: ApiRequest<Any>, socket: WebSocketSession): ApiResponse<*> {
        val handler = requestHandlerFactory.getHandler<Any, Any>(request.type)
        return handler.handle(request, socket)
    }
}