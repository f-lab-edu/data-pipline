package game.server.game.domain.monster

import game.server.game.domain.player.Player
import game.server.game.domain.vo.Position
import game.server.dto.response.EnemyInfoResponse
import game.server.game.domain.monster.ai.MonsterAI

data class Monster(
    val id: String,
    var position: Position,
    val monsterStatus: MonsterStatus,
) {
    val displayName = monsterStatus.displayName
    val attackType = monsterStatus.attackType
    val health = monsterStatus.health
    val damage = monsterStatus.damage
    val defense = monsterStatus.defense
    val width = monsterStatus.width
    val height = monsterStatus.height
    val speed = monsterStatus.speed

    lateinit var monsterAI: MonsterAI

    fun initializeAI(player: Player) {
        monsterAI = MonsterAI(this, player)
    }

    fun toPacket() = EnemyInfoResponse(id, monsterStatus.toPacket(), position)
}