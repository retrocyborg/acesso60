package com.acesso60.execution

import br.com.acesso60.core.logging.ExecutionLogEvent
import br.com.acesso60.core.logging.ExecutionLogger
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggingExecutionMiddlewareTest {

    @Test
    fun `logs command execution and duration`() {
        val events = mutableListOf<ExecutionLogEvent>()
        val logger = object : ExecutionLogger {
            override fun log(event: ExecutionLogEvent) {
                events.add(event)
            }
        }
        var currentTime = 1000L
        val middleware = LoggingExecutionMiddleware(
            logger = logger,
            clock = { currentTime },
        )

        val result = middleware.intercept(Command.Back) {
            currentTime += 45L
            ExecutionResult.Success("Voltei para a tela anterior.")
        }

        assertTrue(result is ExecutionResult.Success)
        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("Back", event.commandName)
        assertEquals(45L, event.durationMs)
        assertEquals(result, event.result)
    }

    @Test
    fun `captures unhandled exception in downstream pipeline and reports failure safely`() {
        val events = mutableListOf<ExecutionLogEvent>()
        val logger = object : ExecutionLogger {
            override fun log(event: ExecutionLogEvent) {
                events.add(event)
            }
        }
        val middleware = LoggingExecutionMiddleware(logger = logger)

        val result = middleware.intercept(Command.Back) {
            throw IllegalStateException("API error")
        }

        assertEquals(ExecutionResult.Failure("Não foi possível executar o comando."), result)
        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("Back", event.commandName)
        assertTrue(event.result is ExecutionResult.Failure)
    }

    @Test
    fun `swallows logger failure without breaking execution result`() {
        val throwingLogger = object : ExecutionLogger {
            override fun log(event: ExecutionLogEvent) {
                throw RuntimeException("Falha no serviço de log")
            }
        }
        val middleware = LoggingExecutionMiddleware(logger = throwingLogger)

        val expected = ExecutionResult.Success("Ok")
        val result = middleware.intercept(Command.Back) { expected }

        assertEquals(expected, result)
    }
}
