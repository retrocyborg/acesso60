package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.accessibility.AccessibilityGateway
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeCommandExecutorTest {

    @Test
    fun `home returns success when accessibility performs the action`() {
        val gateway = FakeAccessibility(returns = true)
        val executor = HomeCommandExecutor(gateway)

        assertEquals(
            ExecutionResult.Success("Voltei para a tela inicial."),
            executor.execute(Command.Home),
        )
        assertEquals(1, gateway.homeCalls)
    }

    @Test
    fun `home returns unavailable when accessibility fails`() {
        val gateway = FakeAccessibility(returns = false)
        val executor = HomeCommandExecutor(gateway)

        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.Home))
        assertEquals(1, gateway.homeCalls)
    }

    @Test
    fun `unsupported command is rejected`() {
        val gateway = FakeAccessibility(returns = true)
        val executor = HomeCommandExecutor(gateway)

        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.Back))
        assertEquals(0, gateway.homeCalls)
    }

    private class FakeAccessibility(private val returns: Boolean) : AccessibilityGateway {
        var homeCalls = 0
        override fun performBack(): Boolean = false
        override fun performHome(): Boolean {
            homeCalls++
            return returns
        }
    }
}
