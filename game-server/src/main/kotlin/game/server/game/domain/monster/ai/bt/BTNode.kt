package game.server.game.domain.monster.ai.bt

interface BTNode {
    fun tick(): Boolean
}