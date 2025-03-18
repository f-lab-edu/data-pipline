package game.server.game.session

import game.server.game.domain.player.Player
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

//@Component
//class PlayerManager {
//    private val players: ConcurrentHashMap<String, MutableMap<String, Player>> = ConcurrentHashMap()
//
//    fun addPlayer(matchId: String, sessionId: String, player: Player) {
//        players.computeIfAbsent(matchId) { ConcurrentHashMap() }[sessionId] = player
//    }
//
//    fun getPlayer(matchId: String, sessionId: String): Player? =
//        players[matchId]?.get(sessionId)
//
//    fun removePlayer(matchId: String, sessionId: String) {
//        players[matchId]?.remove(sessionId)
//        if (players[matchId]?.isEmpty() == true) players.remove(matchId)
//    }
//
//    fun getPlayersInMatch(matchId: String): Collection<Player> =
//        players[matchId]?.values ?: emptyList()
//}


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
}
