package game.server.core.service

import com.fasterxml.jackson.core.type.TypeReference
import game.server.core.dto.Request
import game.server.core.dto.ApiResponse

interface RequestHandler<D, R> {
    val requestTypeReference: TypeReference<Request<D>>
    fun handle(request: Request<D>): ApiResponse<R>
}