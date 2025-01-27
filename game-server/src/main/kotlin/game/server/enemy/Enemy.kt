package game.server.enemy

import game.server.dto.EnemyStatus
import game.server.enemy.AttackType.MELEE
import game.server.enemy.AttackType.RANGED

enum class Enemy(
    val displayName: String,
    val attackType: AttackType,
    val health: Int,
    val damage: Int,
    val defense: Int,
    val width: Int,
    val height: Int
) {

    ZERGLING(
        displayName = "Zergling",
        attackType = MELEE,
        health = 35,
        damage = 5,
        defense = 3,
        width = 10,
        height = 10
    ),

    HYDRALISK(
        displayName = "Hydralisk",
        attackType = RANGED,
        health = 80,
        damage = 10,
        defense = 3,
        width = 15,
        height = 15
    )
    ;

    fun toPacket() = EnemyStatus(displayName, health, damage, defense, width, height)
}