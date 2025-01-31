package game.server.dto.response

sealed class ApiResponse {
    abstract val type: String
}

data class Success<T>(override val type: String, val success: Boolean = true, val data: T) : ApiResponse()
data class Error(override val type: String, val success: Boolean = false, val message: String) : ApiResponse()