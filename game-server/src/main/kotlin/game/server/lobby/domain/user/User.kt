package game.server.lobby.domain.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id val id: Long? = null,
    val providerId: String,
    val provider: String,
    val email: String,
    val name: String,
)