package com.game.service.v1

import com.game.dto.v1.UserDto
import com.game.dto.v1.UserSession
import java.time.Duration

interface SessionManagement {
    suspend fun handleUserSession(
        userDto: UserDto,
        sessionKey: String,
        duration: Duration,
        serverIp: String,
        serverPort: String
    ): String

    suspend fun findBySessionId(sessionIds: List<String>): List<UserSession>
}