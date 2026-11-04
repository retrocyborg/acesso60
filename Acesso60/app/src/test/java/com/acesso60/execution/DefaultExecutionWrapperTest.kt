package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultExecutionWrapperTest {
    @Test
    fun `dispatches supported command exactly once`() {
        val delegate = FakeExecutor(Command.Back, ExecutionResult.Success("ok"))

        assertEquals(ExecutionResult.Success("ok"), wrapper(delegate).execute(Command.Back))
        assertEquals(1, delegate.calls)
    }

    @Test
    fun `returns unavailable when no delegate supports command`() {
        val delegate = FakeExecutor(Command.Back, ExecutionResult.Success("ok"))

        assertEquals(ExecutionResult.Unavailable, wrapper(delegate).execute(Command.Home))
        assertEquals(0, delegate.calls)
    }

    @Test
    fun `rejects ambiguous delegates`() {
        val first = FakeExecutor(Command.Back, ExecutionResult.Success("first"))
        val second = FakeExecutor(Command.Back, ExecutionResult.Success("second"))

        val result = wrapper(first, second).execute(Command.Back)

        assertTrue(result is ExecutionResult.Failure)
        assertEquals(0, first.calls + second.calls)
    }

    @Test
    fun `converts delegate exception to controlled failure`() {
        val delegate = object : SpecializedCommandExecutor {
            override fun supports(command: Command) = command == Command.Back
            override fun execute(command: Command): ExecutionResult = error("boom")
        }

        assertEquals(
            ExecutionResult.Failure("Não foi possível executar o comando."),
            wrapper(delegate).execute(Command.Back),
        )
    }

    private fun wrapper(vararg delegates: SpecializedCommandExecutor) =
        DefaultExecutionWrapper(delegates.toList())

    private class FakeExecutor(
        private val supported: Command,
        private val result: ExecutionResult,
    ) : SpecializedCommandExecutor {
        var calls = 0

        override fun supports(command: Command) = command == supported

        override fun execute(command: Command): ExecutionResult {
            calls++
            return result
        }
    }
}
