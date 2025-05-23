package game.server.scheduler

import game.server.domain.Position
import game.server.dto.response.Response
import game.server.handler.CANVAS_HEIGHT
import game.server.handler.CANVAS_WIDTH
import game.server.enemy.Enemy
import game.server.enemy.EnemyManager
import game.server.enemy.EnemyStatus
import game.server.websocket.GameRequestRouter
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.Math.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@Component
class EnemySpawnScheduler(
    private val gameRequestRouter: GameRequestRouter,
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

        val response = Response(type = "enemy_spawn", data = enemies)
        round++
        gameRequestRouter.sendToClient(response)
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