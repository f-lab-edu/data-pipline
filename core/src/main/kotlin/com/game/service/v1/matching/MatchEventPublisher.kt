package com.game.service.v1.matching

import com.game.dto.v1.maching.Matched


interface MatchEventPublisher {
    suspend fun publishMatchStart(matched: Matched)
}
