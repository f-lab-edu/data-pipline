package game.server.game.dto.v1.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PlayerMoveRequest::class, name = "MOVE"),
)
sealed class ApiRequest<T>(
    open val type: String,
    val messageType: String = "request",
    open val data: T
)

data class PlayerMoveRequest(
    override val type: String,
    override val data: PlayerMoveRequestData
) : ApiRequest<PlayerMoveRequestData>(type = type, data = data)