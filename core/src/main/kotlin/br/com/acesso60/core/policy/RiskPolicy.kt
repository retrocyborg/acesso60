package br.com.acesso60.core.policy

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RiskAssessment

/**
 * Avaliador de política de segurança e risco.
 * Separa comandos de baixo risco (navegação padrão) de comandos sensíveis que exigem
 * confirmação expressa do usuário ou autorização prévia antes de qualquer execução.
 */
class RiskPolicy {

    /**
     * Avalia o nível de risco associado ao comando informado.
     */
    fun evaluate(command: Command): RiskAssessment {
        return when (command) {
            is Command.Back -> RiskAssessment.LowRisk
            is Command.Home -> RiskAssessment.LowRisk
            is Command.VolumeUp -> RiskAssessment.LowRisk
            is Command.VolumeDown -> RiskAssessment.LowRisk
            is Command.ReadScreen,
            is Command.OpenApp -> RiskAssessment.RequiresConfirmation(
                "Confirmação explícita obrigatória para ${command.describe()}."
            )
            is Command.SensitiveAction -> RiskAssessment.RequiresConfirmation(
                "Ação sensível detectada (${command.description}). Confirmação explícita obrigatória."
            )
        }
    }

    private fun Command.describe(): String = when (this) {
        Command.ReadScreen -> "ler a tela"
        is Command.OpenApp -> "abrir ${appName}"
        else -> toString()
    }
}
