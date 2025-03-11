package game.server.lobby.service

import game.server.lobby.dto.v1.response.MatchResultDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaEventPublisher(
    @Value("\${kafka.topic.match-start}") private val matchStartTopic: String,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
) {
    fun publishMatchStart(matchResultDto: MatchResultDto): Mono<Void> {
        return kafkaTemplate.send(
            matchStartTopic,
            matchResultDto.matchId,
            matchResultDto
        ).then()
    }
}