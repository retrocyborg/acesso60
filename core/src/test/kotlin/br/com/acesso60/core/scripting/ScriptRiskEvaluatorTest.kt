package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RiskAssessment
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class ScriptRiskEvaluatorTest {

    private val riskEvaluator = ScriptRiskEvaluator()

    @Test
    fun `deve classificar como baixo risco rotina contendo apenas comandos seguros`() {
        val script = ScriptDefinition(
            id = "script-seguro",
            name = "Navegacao Simples",
            aliases = listOf("navegar"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Delay(300),
                ScriptStep.Execute(Command.Home)
            )
        )

        val risk = riskEvaluator.evaluate(script)
        assertIs<RiskAssessment.LowRisk>(risk)
    }

    @Test
    fun `deve exigir confirmacao se qualquer passo contiver acao sensivel`() {
        val scriptComSensivel = ScriptDefinition(
            id = "script-sensivel",
            name = "Rotina com acao sensivel",
            aliases = listOf("limpeza"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Execute(Command.SensitiveAction("Limpar histórico")),
                ScriptStep.Execute(Command.Home)
            )
        )

        val risk = riskEvaluator.evaluate(scriptComSensivel)
        assertIs<RiskAssessment.RequiresConfirmation>(risk)
    }
}
