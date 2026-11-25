package br.com.acesso60.core.policy

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RiskAssessment

/**
 * Avaliador de política de segurança e risco.
 * Separa comandos diretos (navegação, volume, abrir app, mensagem ditada pelo usuário) de comandos
 * sensíveis que exigem confirmação expressa do usuário antes de qualquer execução.
 *
 * Decisão de produto: [Command.OpenApp] e [Command.SendMessage] são de baixo risco. Abrir um app é
 * reversível, e a mensagem só chega aqui depois de o usuário ditar destinatário, aplicativo (da
 * allowlist de mensagens) e texto; pedir "confirmar" de novo apenas atrapalha quem usa o assistente
 * por voz. A leitura de tela e as ações sensíveis continuam exigindo confirmação.
 */
class RiskPolicy {

    /**
     * Avalia o nível de risco associado ao comando informado.
     */
    fun evaluate(command: Command): RiskAssessment {
        return when (command) {
            is Command.Back,
            is Command.Home,
            is Command.VolumeUp,
            is Command.VolumeDown,
            is Command.OpenApp,
            is Command.SendMessage,
            is Command.AddAlias -> RiskAssessment.LowRisk
            is Command.ReadScreen -> RiskAssessment.RequiresConfirmation(
                "Confirmação explícita obrigatória para ler a tela."
            )
            is Command.SensitiveAction -> RiskAssessment.RequiresConfirmation(
                "Ação sensível detectada (${command.description}). Confirmação explícita obrigatória."
            )
        }
    }
}
