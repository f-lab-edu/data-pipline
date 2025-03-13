package game.server.lobby.service

import game.server.lobby.dto.v1.response.MatchResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaEventPublisher(
    @Value("\${kafka.topic.match-start}") private val matchStartTopic: String,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
) {
    fun publishMatchStart(matchResponseDto: MatchResponseDto): Mono<Void> {
        return kafkaTemplate.send(
            matchStartTopic,
            matchResponseDto.matchId!!,
            matchResponseDto
        ).then()
    }
}