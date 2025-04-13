package com.game.util.coroutine

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.Closeable

class WebSocketSessionContext(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Closeable {

    private val logger = LoggerFactory.getLogger(WebSocketSessionContext::class.java)

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.error("Unhandled exception in coroutine context: $context", throwable)
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcher + exceptionHandler)

    fun <T> launch(block: suspend CoroutineScope.() -> T): Job =
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                logger.info("Coroutine job was cancelled: ${e.message}")
            } catch (e: Exception) {
                logger.error("Unhandled exception caught within coroutine block", e)
            }
        }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> =
        scope.async { block() }

    override fun close() {
        scope.cancel()
    }
}