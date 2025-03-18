package com.game.service.v1.matching

import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved


interface MatchEventPublisher {
    suspend fun publishMatchStart(matched: Matched)
    suspend fun publishPlayerMovement(playerMoved: PlayerMoved)
}
