package game.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import com.game.dto.v1.maching.Matched
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
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

@Profile("consumer-local")
@Configuration
@EnableKafka
@Import(ObjectConfig::class)
open class KafkaConsumerConfig(
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.ip}") private val kafkaIp: String,
    @Value("\${kafka.port}") private val kafkaPort: String,
) {
    @Bean
    open fun consumerFactory(): ConsumerFactory<String, Matched> {
        val jsonDeserializer = JsonDeserializer(Matched::class.java, objectMapper).apply {
            addTrustedPackages("*")
        }

        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "$kafkaIp:$kafkaPort",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to jsonDeserializer::class.java
        )
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), jsonDeserializer)
    }

    @Bean
    open fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Matched> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Matched>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}