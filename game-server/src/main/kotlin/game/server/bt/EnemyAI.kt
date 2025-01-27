package game.server.bt

import game.server.Player
import game.server.dto.Position
import game.server.enemy.AttackType
import game.server.enemy.Enemy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EnemyAI(private val enemy: Enemy, private val player: Player) {
    private val behaviorTree: BTNode

    init {
        behaviorTree = SelectorNode(
            listOf(
                SequenceNode(
                    listOf(
                        ConditionNode { enemy.attackType == AttackType.RANGED },
                        ConditionNode { distanceToPlayer() in 5..20 },
                        ActionNode { performRangedAttack() }
                    )
                ),
                SequenceNode(
                    listOf(
                        ConditionNode { enemy.attackType == AttackType.MELEE },
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
        val dx = enemy.position.x - player.position.x
        val dy = enemy.position.y - player.position.y
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
        val dx = player.position.x - enemy.position.x
        val dy = player.position.y - enemy.position.y
        val angle = atan2(dy.toDouble(), dx.toDouble())

        val speed = enemy.speed
        val newX = (enemy.position.x + cos(angle) * speed).toInt()
        val newY = (enemy.position.y + sin(angle) * speed).toInt()

        println("Enemy ${enemy.id} moving from (${enemy.position.x}, ${enemy.position.y}) to ($newX, $newY)")

        enemy.position = Position(newX, newY)
        return true
    }
}