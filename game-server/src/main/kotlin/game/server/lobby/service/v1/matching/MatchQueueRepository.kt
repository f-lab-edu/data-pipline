package game.server.lobby.service.v1.matching

interface MatchQueueRepository {
    suspend fun addWaitingSession(sessionId: String)
    suspend fun getWaitingSessions(limit: Long): List<String>
    suspend fun removeWaitingSessions(sessionIds: List<String>)
    suspend fun removeWaitingSession(sessionId: String)
}