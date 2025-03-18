package game.server.game.session


import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionManager {

    private val sessionMap = ConcurrentHashMap<String, WebSocketSession>()

    fun register(sessionKey: String, session: WebSocketSession) {
        sessionMap[sessionKey] = session
    }

    fun remove(sessionKey: String) {
        sessionMap.remove(sessionKey)
    }

    fun getSession(sessionKey: String): WebSocketSession? {
        return sessionMap[sessionKey]
    }
}