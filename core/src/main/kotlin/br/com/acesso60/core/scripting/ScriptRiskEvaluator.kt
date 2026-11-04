package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.RiskAssessment
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.policy.RiskPolicy

/**
 * Avaliador de risco para scripts e rotinas.
 * Aplica o princípio fail-safe: o risco agregado da rotina equivale ao nível de risco mais elevado
 * de qualquer um dos seus passos individuais.
 */
class ScriptRiskEvaluator(
    private val riskPolicy: RiskPolicy = RiskPolicy()
) {
    /**
     * Avalia o risco global da execução do script.
     * Retorna [RiskAssessment.RequiresConfirmation] se qualquer comando contido exigir confirmação prévia.
     */
    fun evaluate(script: ScriptDefinition): RiskAssessment {
        val commandsRequiringConfirmation = script.steps
            .filterIsInstance<ScriptStep.Execute>()
            .map { it.command }
            .filter { riskPolicy.evaluate(it) is RiskAssessment.RequiresConfirmation }

        if (commandsRequiringConfirmation.isNotEmpty()) {
            val descriptions = commandsRequiringConfirmation.joinToString { it.describe() }
            return RiskAssessment.RequiresConfirmation(
                "A rotina '${script.name}' inclui ações que exigem confirmação: $descriptions.",
            )
        }
        return RiskAssessment.LowRisk
    }

    private fun Command.describe(): String = when (this) {
        Command.ReadScreen -> "ler a tela"
        is Command.OpenApp -> "abrir $appName"
        is Command.SensitiveAction -> description
        else -> toString()
    }
}
