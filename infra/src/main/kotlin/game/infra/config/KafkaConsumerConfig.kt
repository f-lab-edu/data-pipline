package game.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient

@Profile("consumer-local | consumer-prod")
@Configuration
@EnableKafka
@Import(ObjectConfig::class)
open class KafkaConsumerConfig(
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.ip}") private val kafkaIp: String,
    @Value("\${kafka.port}") private val kafkaPort: String,
) {

    private fun <T> createConsumerFactory(type: Class<T>): ConsumerFactory<String, T> {
        val jsonDeserializer = JsonDeserializer(type, objectMapper).apply {
            addTrustedPackages("*")
        }

        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "$kafkaIp:$kafkaPort",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to jsonDeserializer::class.java
        )

        return DefaultKafkaConsumerFactory(props, StringDeserializer(), jsonDeserializer)
    }

    private fun <T> createKafkaListenerContainerFactory(type: Class<T>): ConcurrentKafkaListenerContainerFactory<String, T> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, T>()
        factory.consumerFactory = createConsumerFactory(type)
        return factory
    }

    @Bean
    open fun matchedKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Matched> {
        return createKafkaListenerContainerFactory(Matched::class.java)
    }

    @Bean
    open fun movedKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, PlayerMoved> {
        return createKafkaListenerContainerFactory(PlayerMoved::class.java)
    }
}