package game.server.game.service

import com.fasterxml.jackson.core.type.TypeReference
import game.server.game.dto.v1.request.Request
import game.server.game.dto.v1.response.ApiResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface RequestHandler<D, R> {
    val requestTypeReference: TypeReference<Request<D>>
    suspend fun handle(request: Request<D>, socket: WebSocketSession): ApiResponse<R>
}