package game.server.lobby.config

import com.game.dto.v1.UserDto
import com.game.service.v1.SessionManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.random.Random

@Aspect
@Component
@Profile("performance-test")
class AopConfig(
    @Value("\${server.ip}") private val serverIp: String,
    @Value("\${server.port}") private val serverPort: String,
    private val redisSessionManagement: SessionManagement
) {

    @Pointcut("execution(* game.server.lobby.controller.v1.MultiMatchingControllerV1.requestMultiMatch(..)) && args(sessionId, ..)")
    fun requestMatchMethod(sessionId: String) {}

    @Before("requestMatchMethod(sessionId)")
    fun beforeRequestMatch(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dummyUserDto = UserDto(
                id = Random.nextLong(),
                providerId = "dummy-test-provider-id-$sessionId",
                provider = "test",
                email = "test@test.com",
                name = "test-name"
            )

            redisSessionManagement.handleUserSession(
                userDto = dummyUserDto,
                sessionKey = sessionId,
                duration = Duration.ofMinutes(30),
                serverIp = serverIp,
                serverPort = serverPort
            )
        }
    }
}