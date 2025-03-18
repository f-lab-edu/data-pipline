package game.server.game.scheduler

import game.server.game.domain.MonsterManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class MonsterAIScheduler(
    private val monsterManager: MonsterManager,
) {

//    @Scheduled(initialDelay = 10500, fixedRate = 100)
//    fun updateEnemies() {
//        monsterManager.updateAllEnemies()
//    }
}