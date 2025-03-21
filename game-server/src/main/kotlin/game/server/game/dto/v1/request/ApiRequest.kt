package game.server.game.dto.v1.request

sealed class ApiRequest<T>(
    open val type: String,
    val messageType: String = "request",
    open val data: T?
)

data class Request<T>(
    override val type: String,
    override val data: T
) : ApiRequest<T>(type = type, data = data)