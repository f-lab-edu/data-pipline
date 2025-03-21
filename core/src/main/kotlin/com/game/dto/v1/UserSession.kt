package com.game.dto.v1

data class UserSession(
    val sessionId: String,
    val user: UserDto,
    val serverIp: String,
    val serverPort: String
)