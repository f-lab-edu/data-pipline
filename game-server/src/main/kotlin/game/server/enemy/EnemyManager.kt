package game.server.enemy

import game.server.Player
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


@Component
class EnemyManager(
    private val player: Player
) {

    private val enemies: MutableMap<String, Enemy> = ConcurrentHashMap()

    fun addEnemy(enemy: Enemy) {
        enemy.also {
            it.initializeAI(player)
            enemies[enemy.id] = it
        }
    }

    fun removeEnemy(enemyId: String) {
        enemies.remove(enemyId)
    }

    fun getEnemy(enemyId: String): Enemy? {
        return enemies[enemyId]
    }

    fun getAllEnemies(): Collection<Enemy> {
        return enemies.values
    }

    fun updateAllEnemies() {
        enemies.values.forEach { it.enemyAI.update() }
    }

    fun enemyClear() {
        enemies.clear()
    }
}