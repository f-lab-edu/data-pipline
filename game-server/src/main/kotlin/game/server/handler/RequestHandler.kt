package game.server.handler

import game.server.dto.request.ApiRequest
import game.server.dto.request.Request
import game.server.dto.response.ApiResponse

interface RequestHandler<T : Request<*>, R: Any?> {
    fun handle(request: T): ApiResponse<R>
}