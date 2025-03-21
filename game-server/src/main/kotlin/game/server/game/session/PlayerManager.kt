package game.server.game.session

import game.server.game.domain.player.Player
import game.server.game.domain.vo.Position
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class PlayerManager {
    private val players = ConcurrentHashMap<String, MutableList<Player>>()

    fun addPlayer(matchId: String, player: Player) {
        players.computeIfAbsent(matchId) { mutableListOf() }.add(player)
    }

    fun getPlayer(socket: WebSocketSession): Player? = players.values.flatten().find { it.socket == socket }

    fun getReceivers(matchId: String): List<Player> = players[matchId] ?: emptyList()

    fun removePlayer(session: WebSocketSession) {
        players.remove(session.id)
    }

    fun updatePlayerPosition(matchId: String, sessionId: String, positionX: Int, positionY: Int) {
        val movePlayer = players.values.flatten().find { it.sessionId == sessionId }
            ?: throw IllegalArgumentException("Player with sessionId $sessionId not found")
        movePlayer.position = Position(positionX, positionY)
    }
}
