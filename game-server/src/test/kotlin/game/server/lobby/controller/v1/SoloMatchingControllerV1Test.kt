package game.server.lobby.controller.v1

import com.game.dto.v1.maching.MatchType
import com.game.dto.v1.maching.Matched
import game.server.lobby.config.TestSecurityConfig
import game.server.lobby.service.v1.matching.SoloMatchingServiceV1
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.mockito.BDDMockito.given
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(SoloMatchingControllerV1::class)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestSecurityConfig::class])
class SoloMatchingControllerV1Test {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockBean
    lateinit var soloMatchingService: SoloMatchingServiceV1

    @Test
    fun `요청 헤더의 Session-Id를 기반으로 솔로매칭 응답을 확인한다`() {
        val sessionId = "test-user"
        val matchResultDto = Matched(
            matchId = "test-match",
            sessionIds = listOf(sessionId),
            matchType = MatchType.SOLO
        )

        runBlocking {
            given(soloMatchingService.requestMatch(sessionId)).willReturn(matchResultDto)
        }

        webTestClient.post()
            .uri("/api/v1/match/solo")
            .header("X-Session-Id", sessionId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.matchId").isEqualTo("test-match")
            .jsonPath("$.sessionIds[0]").isEqualTo(sessionId)
            .jsonPath("$.matchType").isEqualTo("SOLO")
    }
}
