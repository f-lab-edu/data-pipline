package com.game.service.v1.matching

interface MatchQueueRepository {
    suspend fun addWaitingSessionIfNotExists(sessionId: String)
    suspend fun popSessionsIfReady(matchCount: Int): List<String>
    suspend fun removeWaitingSession(sessionId: String): Long
}