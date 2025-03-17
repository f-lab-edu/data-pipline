package game.infra

import game.infra.r2dbc.entity.UserEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SpringDataUserRepository : CoroutineCrudRepository<UserEntity, String> {
    suspend fun findByProviderId(providerId: String): UserEntity?
}