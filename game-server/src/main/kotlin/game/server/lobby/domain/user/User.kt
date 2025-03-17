package game.server.lobby.domain.user

import com.game.dto.v1.UserDto

data class User(
    val id: Long? = null,
    val providerId: String,
    val provider: String,
    val email: String,
    val name: String,
) {

    fun toDto() = UserDto(id, providerId, provider, email, name)
}