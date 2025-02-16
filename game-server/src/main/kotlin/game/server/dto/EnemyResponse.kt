package game.server.dto

import game.server.game.domain.v0.Position


data class EnemyInfoResponse(
    val enemyId: String,
    val status: EnemyStatusPacket,
    val position: Position
)

data class EnemyStatusPacket(
    val name: String,
    val health: Int,
    val damage: Int,
    val defense: Int,
    val width: Int,
    val height: Int
)