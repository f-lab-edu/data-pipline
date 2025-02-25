package game.server.lobby.domain.user

import org.springframework.data.repository.kotlin.CoroutineCrudRepository


interface UserRepository : CoroutineCrudRepository<User, String> {
    suspend fun save(user: User): User
    suspend fun findByProviderId(userId: String): User?
}