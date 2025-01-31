package game.server.handler

import game.server.dto.Request
import game.server.dto.response.ApiResponse

interface RequestHandler<T : Request> {
    fun handle(request: T): ApiResponse
}