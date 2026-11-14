package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebounceExecutionMiddlewareTest {

    @Test
    fun `allows repeatable commands in rapid succession`() {
        var currentTime = 1000L
        val middleware = DebounceExecutionMiddleware(
            debounceWindowMs = 1000L,
            clock = { currentTime },
        )

        var executions = 0
        val next = { _: Command ->
            executions++
            ExecutionResult.Success("ok")
        }

        // Primeiro disparo
        val first = middleware.intercept(Command.Back, next)
        assertEquals(ExecutionResult.Success("ok"), first)
        assertEquals(1, executions)

        // Segundo disparo imediatamente a seguir (100ms depois)
        currentTime += 100L
        val second = middleware.intercept(Command.Back, next)
        assertEquals(ExecutionResult.Success("ok"), second)
        assertEquals(2, executions)
    }

    @Test
    fun `blocks non-repeatable command within debounce window`() {
        var currentTime = 1000L
        val middleware = DebounceExecutionMiddleware(
            debounceWindowMs = 1200L,
            clock = { currentTime },
        )

        var executions = 0
        val next = { _: Command ->
            executions++
            ExecutionResult.Success("app iniciado")
        }

        val openWhatsApp = Command.OpenApp("whatsapp")

        // Primeiro disparo: permitido
        val first = middleware.intercept(openWhatsApp, next)
        assertEquals(ExecutionResult.Success("app iniciado"), first)
        assertEquals(1, executions)

        // Segundo disparo em 500ms (dentro da janela de 1200ms): bloqueado
        currentTime += 500L
        val second = middleware.intercept(openWhatsApp, next)
        assertTrue(second is ExecutionResult.Failure)
        assertEquals(
            ExecutionResult.Failure("Comando já acionado recentemente. Aguarde um instante."),
            second,
        )
        assertEquals(1, executions) // Não chamou downstream
    }

    @Test
    fun `allows non-repeatable command after debounce window expires`() {
        var currentTime = 1000L
        val middleware = DebounceExecutionMiddleware(
            debounceWindowMs = 1000L,
            clock = { currentTime },
        )

        var executions = 0
        val next = { _: Command ->
            executions++
            ExecutionResult.Success("app iniciado")
        }

        val openWhatsApp = Command.OpenApp("whatsapp")

        // Primeiro disparo
        middleware.intercept(openWhatsApp, next)
        assertEquals(1, executions)

        // Passou a janela (1100ms)
        currentTime += 1100L
        val second = middleware.intercept(openWhatsApp, next)
        assertEquals(ExecutionResult.Success("app iniciado"), second)
        assertEquals(2, executions)
    }

    @Test
    fun `does not block different non-repeatable commands`() {
        var currentTime = 1000L
        val middleware = DebounceExecutionMiddleware(
            debounceWindowMs = 1000L,
            clock = { currentTime },
        )

        var executions = 0
        val next = { _: Command ->
            executions++
            ExecutionResult.Success("ok")
        }

        middleware.intercept(Command.OpenApp("whatsapp"), next)
        assertEquals(1, executions)

        // Outro app acionado no mesmo instante: permitido
        val resultOther = middleware.intercept(Command.OpenApp("telefone"), next)
        assertEquals(ExecutionResult.Success("ok"), resultOther)
        assertEquals(2, executions)
    }
}
