package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.RiskAssessment
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
        for (step in script.steps) {
            if (step is ScriptStep.Execute) {
                val assessment = riskPolicy.evaluate(step.command)
                if (assessment is RiskAssessment.RequiresConfirmation) {
                    return assessment
                }
            }
        }
        return RiskAssessment.LowRisk
    }
}
