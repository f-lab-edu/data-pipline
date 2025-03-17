package com.game.repository.v1

import com.game.dto.v1.UserDto

interface UserRepository {
    suspend fun save(userDto: UserDto): UserDto
    suspend fun findByProviderId(providerId: String): UserDto?
}
