package game.server.scheduler

import game.server.enemy.EnemyManager
import game.server.websocket.GameRequestRouter
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class EnemyAIScheduler(
    private val router: GameRequestRouter,
    private val enemyManager: EnemyManager,
) {

    @Scheduled(initialDelay = 10500, fixedRate = 100)
    fun updateEnemies() {
        enemyManager.updateAllEnemies()
        notifyClients()
    }

    private fun notifyClients() {
        val enemyPackets = enemyManager.getAllEnemies().map { it.toPacket() }

        val message = mapOf(
            "type" to "enemy_positions",
            "data" to enemyPackets
        )
        router.sendToClient(message)
    }
}