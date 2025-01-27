package game.server.scheduler

import game.server.CANVAS_HEIGHT
import game.server.CANVAS_WIDTH
import game.server.dto.Position
import game.server.enemy.Enemy
import game.server.enemy.EnemyManager
import game.server.enemy.EnemyStatus
import game.server.websocket.GameWebSocketHandler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.Math.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@Component
class EnemySpawnScheduler(
    private val webSocketHandler: GameWebSocketHandler,
    private val enemyManager: EnemyManager
) {
    private val center = Position(400, 300)
    private var round = 1

    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    fun spawnEnemies() {
        enemyManager.enemyClear()

        val radius = (CANVAS_WIDTH.coerceAtMost(CANVAS_HEIGHT) / 2)
        val enemyCount = 5 * round

        val enemies = (1..enemyCount).map {
            createRandomEnemy(center, radius).toPacket()
        }

        val message = mapOf(
            "type" to "enemy_spawn",
            "data" to enemies,
            "round" to round
        )
        round++
        webSocketHandler.sendToClient(message)
    }

    private fun createRandomEnemy(center: Position, radius: Int, minDistance: Int = 270): Enemy {
        val angle = Random.nextDouble(0.0, 2 * PI)
        val distance = Random.nextDouble(minDistance.toDouble(), radius.toDouble())

        val x = (center.x + cos(angle) * distance).toInt()
        val y = (center.y + sin(angle) * distance).toInt()

        val randomEnemyStatus = EnemyStatus.entries.random()

        return Enemy(
            id = UUID.randomUUID().toString(),
            position = Position(x, y),
            enemyStatus = randomEnemyStatus
        ).also {
            enemyManager.addEnemy(it)
        }
    }
}