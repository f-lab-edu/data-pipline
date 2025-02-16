package game.server.game.domain

import game.server.game.domain.player.Player
import game.server.game.domain.monster.Monster
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


@Component
class EnemyManager(
    private val player: Player
) {

    private val enemies: MutableMap<String, Monster> = ConcurrentHashMap()

    fun addEnemy(monster: Monster) {
        monster.also {
            it.initializeAI(player)
            enemies[monster.id] = it
        }
    }

    fun removeEnemy(enemyId: String) {
        enemies.remove(enemyId)
    }

    fun getEnemy(enemyId: String): Monster? {
        return enemies[enemyId]
    }

    fun getAllEnemies(): Collection<Monster> {
        return enemies.values
    }

    fun updateAllEnemies() {
        enemies.values.forEach { it.monsterAI.update() }
    }

    fun enemyClear() {
        enemies.clear()
    }
}