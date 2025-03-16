package game.server.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@Import(ObjectConfig::class)
open class RedisConfig(
    private val objectMapper: ObjectMapper
) {
    @Bean
    open fun reactiveRedisUserTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)

        val context = RedisSerializationContext.newSerializationContext<String, Any>(StringRedisSerializer())
            .value(jsonSerializer)
            .hashValue(jsonSerializer)
            .build()

        return ReactiveRedisTemplate(factory, context)
    }
}