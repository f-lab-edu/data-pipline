package com.game.dto.v1.maching


sealed class MatchResponseDto(open val status: MatchStatus, open val matchType: MatchType)

data class Matched(
    val matchId: String,
    var sessionIds: List<String>,
    override val matchType: MatchType
) : MatchResponseDto(MatchStatus.MATCHED, matchType)

data class Waiting(
    override val matchType: MatchType
) : MatchResponseDto(MatchStatus.WAITING, matchType)


enum class MatchStatus {
    MATCHED, WAITING
}