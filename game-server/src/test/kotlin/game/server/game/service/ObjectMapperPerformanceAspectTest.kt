package game.server.game.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import com.game.config.ObjectMapperPerformanceAspect
import game.server.lobby.config.TestAopConfig
import game.server.lobby.config.TestSecurityConfig
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("test")
@WebFluxTest(ObjectMapperPerformanceAspectTest::class)
@Import(
    TestAopConfig::class,
    TestSecurityConfig::class,
    ObjectConfig::class,
    ObjectMapperPerformanceAspect::class
)
class ObjectMapperPerformanceAspectTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val logger = LoggerFactory.getLogger(javaClass)

    data class SampleData(val id: Int, val name: String)

    @Test
    fun serializationAopTest() {
        val data = SampleData(1, "Test")
        logger.info(">> Serialization test")
        val jsonString = objectMapper.writeValueAsString(data)
        logger.info("Serialized JSON: {}", jsonString)
    }

    @Test
    fun deserializationAopTest() {
        val json = """{"id":1,"name":"Test"}"""
        logger.info(">> Deserialization test")
        val result = objectMapper.readValue(json, SampleData::class.java)
        logger.info("Deserialized Object: {}", result)
    }
}
