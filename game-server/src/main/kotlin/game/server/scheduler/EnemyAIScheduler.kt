package game.server.scheduler

import game.server.enemy.EnemyManager
import game.server.websocket.GameWebSocketHandler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class EnemyAIScheduler(
    private val webSocketHandler: GameWebSocketHandler,
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
        webSocketHandler.sendToClient(message)
    }
}