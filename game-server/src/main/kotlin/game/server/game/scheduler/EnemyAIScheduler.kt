package game.server.game.scheduler

import game.server.game.domain.EnemyManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class EnemyAIScheduler(
    private val enemyManager: EnemyManager,
) {

    @Scheduled(initialDelay = 10500, fixedRate = 100)
    fun updateEnemies() {
        enemyManager.updateAllEnemies()
    }
}