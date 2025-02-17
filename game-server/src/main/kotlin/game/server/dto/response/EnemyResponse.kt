package game.server.dto.response

import game.server.game.domain.vo.Position


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