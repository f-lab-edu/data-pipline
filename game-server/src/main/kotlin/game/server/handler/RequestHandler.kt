package game.server.handler

import com.fasterxml.jackson.core.type.TypeReference
import game.server.dto.request.Request
import game.server.dto.response.ApiResponse

interface RequestHandler<T : Request<*>, R: Any?> {
    val requestTypeReference: TypeReference<T>
    fun handle(request: T): ApiResponse<R>
}