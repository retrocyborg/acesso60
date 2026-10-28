package com.acesso60.core

import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.InterpretationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreContractTest {
    @Test
    fun `android test source resolves the shared core interpreter`() {
        val result = CommandInterpreter().interpret("  INÍCIO  ")

        assertTrue(result is InterpretationResult.Success)
        assertEquals(Command.Home, (result as InterpretationResult.Success).command)
    }
}
