package game.server.game.service

import com.fasterxml.jackson.core.type.TypeReference
import game.server.game.dto.v1.request.Request
import game.server.game.dto.v1.response.ApiResponse

interface RequestHandler<D, R> {
    val requestTypeReference: TypeReference<Request<D>>
    fun handle(request: Request<D>): ApiResponse<R>
}