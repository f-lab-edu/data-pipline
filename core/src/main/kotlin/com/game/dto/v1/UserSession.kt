package com.game.dto.v1

data class UserSession(
    val user: UserDto,
    val serverIp: String,
    val serverPort: String
)