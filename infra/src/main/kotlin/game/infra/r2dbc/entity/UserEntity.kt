package game.infra.r2dbc.entity

import com.game.dto.v1.UserDto
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Id val id: Long? = null,
    val providerId: String,
    val provider: String,
    val email: String,
    val name: String,
) {
    fun toDomain(): UserDto = UserDto(id, providerId, provider, email, name)

    companion object {
        fun fromDomain(user: UserDto): UserEntity =
            UserEntity(user.id, user.providerId, user.provider, user.email, user.name)
    }
}