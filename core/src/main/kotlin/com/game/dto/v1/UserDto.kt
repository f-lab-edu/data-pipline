package com.game.dto.v1

data class UserDto(
    val id: Long? = null,
    val providerId: String,
    val provider: String,
    val email: String,
    val name: String
)