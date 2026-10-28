package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.Command
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ScriptValidatorTest {

    private val validator = ScriptValidator()

    @Test
    fun `deve aprovar script valido com passos de execucao e delay`() {
        val script = ScriptDefinition(
            id = "rotina-1",
            name = "Voltar e Home",
            aliases = listOf("reiniciar", "voltar inicio"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Delay(500),
                ScriptStep.Execute(Command.Home)
            )
        )

        val result = validator.validate(script)
        assertIs<ScriptValidationResult.Valid>(result)
    }

    @Test
    fun `deve rejeitar script com id ou nome vazio`() {
        val semId = ScriptDefinition(
            id = "",
            name = "Nome",
            aliases = listOf("alias"),
            steps = listOf(ScriptStep.Execute(Command.Back))
        )
        assertIs<ScriptValidationResult.Invalid>(validator.validate(semId))

        val semNome = ScriptDefinition(
            id = "id",
            name = "   ",
            aliases = listOf("alias"),
            steps = listOf(ScriptStep.Execute(Command.Back))
        )
        assertIs<ScriptValidationResult.Invalid>(validator.validate(semNome))
    }

    @Test
    fun `deve rejeitar script sem passos ou sem aliases validos`() {
        val semAliases = ScriptDefinition(
            id = "id-1",
            name = "Nome",
            aliases = emptyList(),
            steps = listOf(ScriptStep.Execute(Command.Back))
        )
        assertIs<ScriptValidationResult.Invalid>(validator.validate(semAliases))

        val semPassos = ScriptDefinition(
            id = "id-2",
            name = "Nome",
            aliases = listOf("alias"),
            steps = emptyList()
        )
        assertIs<ScriptValidationResult.Invalid>(validator.validate(semPassos))
    }

    @Test
    fun `deve rejeitar script que excede o numero maximo de passos`() {
        val passosExcessivos = (1..11).map { ScriptStep.Execute(Command.Back) }
        val script = ScriptDefinition(
            id = "id-grande",
            name = "Muitos passos",
            aliases = listOf("macro"),
            steps = passosExcessivos
        )

        val result = validator.validate(script)
        assertIs<ScriptValidationResult.Invalid>(result)
        assertTrue(result.reason.contains("excede o limite máximo permitido"))
    }

    @Test
    fun `deve rejeitar delay invalido ou excessivo`() {
        val delayNegativo = ScriptDefinition(
            id = "id-delay",
            name = "Delay negativo",
            aliases = listOf("macro"),
            steps = listOf(ScriptStep.Delay(-100))
        )
        assertIs<ScriptValidationResult.Invalid>(validator.validate(delayNegativo))

        val delayExcessivo = ScriptDefinition(
            id = "id-delay-2",
            name = "Delay excessivo",
            aliases = listOf("macro"),
            steps = listOf(ScriptStep.Delay(30000))
        )
        assertIs<ScriptValidationResult.Invalid>(validator.validate(delayExcessivo))
    }

    @Test
    fun `deve rejeitar passo com comando internamente invalido`() {
        val comandoInvalido = ScriptDefinition(
            id = "id-cmd-invalido",
            name = "App vazio",
            aliases = listOf("macro"),
            steps = listOf(ScriptStep.Execute(Command.OpenApp("   ")))
        )
        val result = validator.validate(comandoInvalido)
        assertIs<ScriptValidationResult.Invalid>(result)
        assertTrue(result.reason.contains("possui comando inválido"))
    }
}
