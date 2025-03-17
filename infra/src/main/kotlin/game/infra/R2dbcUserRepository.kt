package game.infra

import com.game.dto.v1.UserDto
import com.game.repository.v1.UserRepository
import game.infra.r2dbc.entity.UserEntity
import org.springframework.stereotype.Repository


@Repository
open class R2dbcUserRepository(
    private val delegate: SpringDataUserRepository
) : UserRepository {

    override suspend fun save(userDto: UserDto): UserDto {
        val entity = UserEntity.fromDomain(userDto)
        val savedEntity = delegate.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun findByProviderId(providerId: String): UserDto? {
        return delegate.findByProviderId(providerId)?.toDomain()
    }
}