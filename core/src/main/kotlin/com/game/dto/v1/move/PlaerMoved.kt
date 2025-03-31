package com.game.dto.v1.move

import com.game.dto.v1.maching.KafkaEvent

data class PlayerMoved(
    override val eventType: String = "PLAYER_MOVED",
    val seq: Int,
    val playerId: String,
    val matchId: String,
    val newPositionX: Int,
    val newPositionY: Int,
    val receivers: List<String>,
    val timestamp: Long
) : KafkaEvent

