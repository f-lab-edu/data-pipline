package game.server.handler

import com.fasterxml.jackson.core.type.TypeReference
import game.server.dto.request.Request
import game.server.dto.response.ApiResponse

interface RequestHandler<D, R> {
    val requestTypeReference: TypeReference<Request<D>>
    fun handle(request: Request<D>): ApiResponse<R>
}