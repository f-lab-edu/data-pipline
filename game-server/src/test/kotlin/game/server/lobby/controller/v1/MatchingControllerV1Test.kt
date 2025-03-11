package game.server.lobby.controller.v1

import game.server.lobby.config.TestSecurityConfig
import game.server.lobby.domain.match.MatchType
import game.server.lobby.dto.v1.response.MatchResultDto
import game.server.lobby.service.MatchingService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.mockito.BDDMockito.given
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest
@ActiveProfiles("test-lobby")
@Import(TestSecurityConfig::class)
class MatchingControllerV1Test {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockBean
    lateinit var soloMatchingService: MatchingService

    @Test
    fun `요청 헤더의 Session-Id를 기반으로 솔로매칭 응답을 확인한다`() {
        val sessionId = "test-user"
        val mockResult = MatchResultDto(
            matchId = "test-match",
            sessionIds = listOf(sessionId),
            matchType = MatchType.SOLO
        )

        given(soloMatchingService.requestMatch(sessionId)).willReturn(Mono.just(mockResult))

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
