package com.acesso60.command

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandInterpreterTest {
    private val interpreter = CommandInterpreter()

    @Test
    fun `interprets back command`() {
        assertEquals(Command.Back, interpreter.interpret("voltar"))
    }

    @Test
    fun `interprets open app command`() {
        assertEquals(Command.OpenApp("whatsapp"), interpreter.interpret("abrir whatsapp"))
    }
}
