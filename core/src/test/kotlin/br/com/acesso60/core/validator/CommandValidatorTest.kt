package br.com.acesso60.core.validator

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RawInputValidationResult
import br.com.acesso60.core.model.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UT-04: Testes Unitários do CommandValidator")
class CommandValidatorTest {

    private val validator = CommandValidator()

    @Test
    @DisplayName("UT-04a: Texto nulo ou em branco deve ser rejeitado sem provocar execução")
    fun testBlankOrNullInputIsRejected() {
        val invalidInputs = listOf(null, "", "   ", "\t", "\n")
        for (input in invalidInputs) {
            val result = validator.validateRawInput(input)
            assertTrue(result is RawInputValidationResult.Invalid, "Entrada inválida deveria retornar Invalid")
        }
    }

    @Test
    @DisplayName("UT-04e: Texto válido é preservado sem fabricar um comando")
    fun testValidRawInputDoesNotCreateCommand() {
        val result = validator.validateRawInput("  voltar  ")

        assertEquals(RawInputValidationResult.Valid("voltar"), result)
    }

    @Test
    @DisplayName("UT-04b: Comando nulo deve ser rejeitado")
    fun testNullCommandIsRejected() {
        val result = validator.validate(null)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Comando inexistente ou nulo", (result as ValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("UT-04c: Ação sensível com parâmetros incompletos/vazios deve ser rejeitada")
    fun testSensitiveActionWithoutDescriptionIsRejected() {
        val incompleteCommand = Command.SensitiveAction("   ")
        val result = validator.validate(incompleteCommand)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Ação sensível sem descrição de operação", (result as ValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("UT-04d: Comandos bem formados são aceitos como válidos")
    fun testValidCommandsAreAccepted() {
        val validCommands = listOf(
            Command.Back,
            Command.Home,
            Command.SensitiveAction("enviar mensagem de teste")
        )
        for (cmd in validCommands) {
            val result = validator.validate(cmd)
            assertTrue(result is ValidationResult.Valid)
            assertEquals(cmd, (result as ValidationResult.Valid).command)
        }
    }
}
