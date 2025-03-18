package com.game.dto.v1.maching


sealed class MatchResponseDto(open val status: MatchStatus, open val matchType: MatchType)

data class Matched(
    val matchId: String,
    val sessionIds: List<String>,
    override val matchType: MatchType,
    override val eventType: String = "MATCHED"
) : MatchResponseDto(MatchStatus.MATCHED, matchType), KafkaEvent

data class Waiting(
    override val matchType: MatchType
) : MatchResponseDto(MatchStatus.WAITING, matchType)


enum class MatchStatus {
    MATCHED, WAITING
}