package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.accessibility.AccessibilityGateway
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidCommandExecutorTest {
    @Test
    fun `back returns success when accessibility performs the action`() {
        val executor = AndroidCommandExecutor(FakeAccessibility(returns = true))

        assertEquals(
            ExecutionResult.Success("Voltei para a tela anterior."),
            executor.execute(Command.Back),
        )
    }

    @Test
    fun `back returns unavailable when accessibility is not active`() {
        val executor = AndroidCommandExecutor(FakeAccessibility(returns = false))

        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.Back))
    }

    @Test
    fun `unsupported command never reaches accessibility`() {
        val gateway = FakeAccessibility(returns = true)
        val executor = AndroidCommandExecutor(gateway)

        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.Home))
        assertEquals(0, gateway.calls)
    }

    private class FakeAccessibility(private val returns: Boolean) : AccessibilityGateway {
        var calls = 0
        override fun performBack(): Boolean {
            calls++
            return returns
        }
    }
}
