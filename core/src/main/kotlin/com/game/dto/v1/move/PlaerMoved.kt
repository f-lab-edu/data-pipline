package com.game.dto.v1.move

data class PlayerMoved(
    val playerId: String,
    val matchId: String,
    val newPositionX: Int,
    val newPositionY: Int,
    val receivers: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

