package game.server.lobby.service.v1.matching.util

interface MatchIdGenerator {
    fun generate(): String
}