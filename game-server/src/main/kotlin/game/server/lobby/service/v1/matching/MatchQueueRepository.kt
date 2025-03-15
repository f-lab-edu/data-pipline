package game.server.lobby.service.v1.matching

interface MatchQueueRepository {
    suspend fun addWaitingSession(sessionId: String)
    suspend fun popSessionsIfReady(matchCount: Int): List<String>
    suspend fun removeWaitingSession(sessionId: String): Long
}