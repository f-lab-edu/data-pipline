package game.server.game.scheduler

import game.server.game.domain.vo.Position
import game.server.game.dto.v1.response.Response
import game.server.game.handler.CANVAS_HEIGHT
import game.server.game.handler.CANVAS_WIDTH
import game.server.game.domain.monster.Monster
import game.server.game.domain.MonsterManager
import game.server.game.domain.monster.MonsterStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.Math.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@Component
class MonsterSpawnScheduler(
    private val monsterManager: MonsterManager
) {
    private val center = Position(400, 300)
    private var round = 1

    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    fun spawnEnemies() {
        monsterManager.enemyClear()

        val radius = (CANVAS_WIDTH.coerceAtMost(CANVAS_HEIGHT) / 2)
        val enemyCount = 5 * round

        val enemies = (1..enemyCount).map {
            createRandomMonster(center, radius).toPacket()
        }

        val response = Response(type = "enemy_spawn", data = enemies)
        round++
    }

    private fun createRandomMonster(center: Position, radius: Int, minDistance: Int = 270): Monster {
        val angle = Random.nextDouble(0.0, 2 * PI)
        val distance = Random.nextDouble(minDistance.toDouble(), radius.toDouble())

        val x = (center.x + cos(angle) * distance).toInt()
        val y = (center.y + sin(angle) * distance).toInt()

        val randomMonsterStatus = MonsterStatus.entries.random()

        return Monster(
            id = UUID.randomUUID().toString(),
            position = Position(x, y),
            monsterStatus = randomMonsterStatus
        ).also {
            monsterManager.addEnemy(it)
        }
    }
}