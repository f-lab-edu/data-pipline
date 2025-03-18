package com.game.dto.v1.maching

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.game.dto.v1.move.PlayerMoved


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Matched::class, name = "MATCHED"),
    JsonSubTypes.Type(value = PlayerMoved::class, name = "PLAYER_MOVED")
)
interface KafkaEvent {
    val eventType: String
}