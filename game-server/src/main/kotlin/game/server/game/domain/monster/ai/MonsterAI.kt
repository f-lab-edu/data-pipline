package game.server.game.domain.monster.ai

import game.server.game.domain.player.Player
import game.server.domain.Position
import game.server.game.domain.monster.AttackType
import game.server.game.domain.monster.Monster
import game.server.game.domain.monster.ai.bt.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MonsterAI(private val monster: Monster, private val player: Player) {
    private val behaviorTree: BTNode

    init {
        behaviorTree = SelectorNode(
            listOf(
                SequenceNode(
                    listOf(
                        ConditionNode { monster.attackType == AttackType.RANGED },
                        ConditionNode { distanceToPlayer() in 5..20 },
                        ActionNode { performRangedAttack() }
                    )
                ),
                SequenceNode(
                    listOf(
                        ConditionNode { monster.attackType == AttackType.MELEE },
                        ConditionNode { distanceToPlayer() <= 5 },
                        ActionNode { performMeleeAttack() }
                    )
                ),
                ActionNode { moveToPlayer() }
            )
        )
    }

    fun update() {
        behaviorTree.tick()
    }

    private fun distanceToPlayer(): Int {
        val dx = monster.position.x - player.position.x
        val dy = monster.position.y - player.position.y
        return sqrt((dx * dx + dy * dy).toDouble()).toInt()
    }

    private fun performRangedAttack(): Boolean {
        // TODO: 원거리 공격 로직 구현
        return true
    }

    private fun performMeleeAttack(): Boolean {
        // TODO: 근접 공격 로직 구현 (예: 플레이어 체력 감소 및 이펙트 추가)
        return true
    }

    private fun moveToPlayer(): Boolean {
        val dx = player.position.x - monster.position.x
        val dy = player.position.y - monster.position.y
        val angle = atan2(dy.toDouble(), dx.toDouble())

        val speed = monster.speed
        val newX = (monster.position.x + cos(angle) * speed).toInt()
        val newY = (monster.position.y + sin(angle) * speed).toInt()

        monster.position = Position(newX, newY)
        return true
    }
}