package game.server.lobby.service

import com.game.dto.v1.UserDto
import com.game.repository.v1.UserRepository
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository
) {
    suspend fun retrieveOrCreateUser(
        provider: String,
        providerId: String,
        email: String,
        name: String
    ): UserDto {
        return userRepository.findByProviderId(providerId)
            ?: userRepository.save(
                UserDto(
                    provider = provider,
                    providerId = providerId,
                    email = email,
                    name = name
                )
            )
    }
}