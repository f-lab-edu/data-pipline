package game.server.game.dto.v1.response

sealed class ApiResponse<T>(
    open val type: String,
    open val success: Boolean = true,
    open val messageType: String = "response",
    open val data: T?
)

data class Response<T>(
    override val type: String,
    override val success: Boolean = true,
    override val data: T
) : ApiResponse<T>(type = type, data = data)

data class ErrorResponse<T>(
    override val type: String = "error",
    override val success: Boolean = false,
    val message: String,
) : ApiResponse<T>(
    type = type,
    success = success,
    data = null
) {
    companion object {
        fun default(e: Throwable? = null) =
            ErrorResponse<Nothing>(
                type = "error",
                message = e?.message ?: "Unknown error"
            )
    }
}