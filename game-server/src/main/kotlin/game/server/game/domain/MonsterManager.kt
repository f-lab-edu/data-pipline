package game.server.game.domain

import game.server.game.domain.player.Player
import game.server.game.domain.monster.Monster
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


@Component
class MonsterManager {

    private val monsters: MutableMap<String, Monster> = ConcurrentHashMap()

    fun addEnemy(monster: Monster) {
        monster.also {
//            it.initializeAI(player)
            monsters[monster.id] = it
        }
    }

    fun removeEnemy(enemyId: String) {
        monsters.remove(enemyId)
    }

    fun getEnemy(enemyId: String): Monster? {
        return monsters[enemyId]
    }

    fun getAllEnemies(): Collection<Monster> {
        return monsters.values
    }

    fun updateAllEnemies() {
        monsters.values.forEach { it.monsterAI.update() }
    }

    fun enemyClear() {
        monsters.clear()
    }
}