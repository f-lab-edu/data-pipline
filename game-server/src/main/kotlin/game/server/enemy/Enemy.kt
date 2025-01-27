package game.server.enemy

import game.server.Player
import game.server.bt.EnemyAI
import game.server.dto.EnemyInfoResponse
import game.server.dto.Position

data class Enemy(
    val id: String,
    var position: Position,
    val enemyStatus: EnemyStatus,
) {
    val displayName = enemyStatus.displayName
    val attackType = enemyStatus.attackType
    val health = enemyStatus.health
    val damage = enemyStatus.damage
    val defense = enemyStatus.defense
    val width = enemyStatus.width
    val height = enemyStatus.height
    val speed = enemyStatus.speed

    lateinit var enemyAI: EnemyAI

    fun initializeAI(player: Player) {
        enemyAI = EnemyAI(this, player)
    }

    fun toPacket() = EnemyInfoResponse(id, enemyStatus.toPacket(), position)
}