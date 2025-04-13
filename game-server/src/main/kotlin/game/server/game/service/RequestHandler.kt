package game.server.game.service

import game.server.game.dto.v1.request.ApiRequest
import game.server.game.dto.v1.response.ApiResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface RequestHandler<D, R> {
    suspend fun handle(request: ApiRequest<D>, socket: WebSocketSession): ApiResponse<R>
}