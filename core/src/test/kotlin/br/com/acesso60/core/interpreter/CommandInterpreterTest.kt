package br.com.acesso60.core.interpreter

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.InterpretationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UT-01 a UT-03: Testes Unitários do CommandInterpreter")
class CommandInterpreterTest {

    private val interpreter = CommandInterpreter()

    @Test
    @DisplayName("UT-01: 'voltar' deve ser interpretado como Command.Back (com tolerância a maiúsculas e espaços)")
    fun testVoltarResultsInBackCommand() {
        val variations = listOf("voltar", "Voltar", "  VOLTAR  ")
        for (input in variations) {
            val result = interpreter.interpret(input)
            assertTrue(result is InterpretationResult.Success, "Deveria interpretar '$input' com sucesso")
            assertEquals(Command.Back, (result as InterpretationResult.Success).command)
        }
    }

    @Test
    @DisplayName("UT-02: 'início' e 'inicio' devem ser interpretados como Command.Home")
    fun testInicioResultsInHomeCommand() {
        val variations = listOf("início", "inicio", "INÍCIO", "Inicio  ")
        for (input in variations) {
            val result = interpreter.interpret(input)
            assertTrue(result is InterpretationResult.Success, "Deveria interpretar '$input' com sucesso")
            assertEquals(Command.Home, (result as InterpretationResult.Success).command)
        }
    }

    @Test
    @DisplayName("UT-03: Frase desconhecida não deve gerar comando executável (retorna InterpretationResult.Unknown)")
    fun testUnknownPhraseReturnsUnknownResult() {
        val unknownInputs = listOf(
            "tocar uma música aleatória",
            "xyz123456",
            "comando que nao existe"
        )
        for (input in unknownInputs) {
            val result = interpreter.interpret(input)
            assertTrue(result is InterpretationResult.Unknown, "Entrada desconhecida '$input' deveria retornar Unknown")
            assertEquals(input, (result as InterpretationResult.Unknown).rawText)
        }
    }

    @Test
    @DisplayName("UT-03b: Comando abrir deve preservar o nome do aplicativo")
    fun testOpenAppCommandPreservesAppName() {
        val result = interpreter.interpret("  abrir whatsapp  ")

        assertEquals(
            InterpretationResult.Success(Command.OpenApp("whatsapp")),
            result
        )
    }
}
