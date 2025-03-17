package game.infra

import com.game.dto.v1.maching.Matched
import com.game.service.v1.matching.MatchEventPublisher
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Repository

@Repository
open class KafkaEventPublisher(
    @Value("\${kafka.topic.match-start}") private val matchStartTopic: String,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Matched>,
) : MatchEventPublisher {

    override suspend fun publishMatchStart(matched: Matched) {
        kafkaTemplate.send(matchStartTopic, matched.matchId, matched).awaitSingle()
    }
}