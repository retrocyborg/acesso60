package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.screen.ScreenReadOutcome
import com.acesso60.accessibility.AccessibilityGateway
import com.acesso60.accessibility.AndroidAccessibilityGateway

/** Lê a tela ativa em voz alta, respeitando a [br.com.acesso60.core.policy.ScreenPrivacyPolicy]. */
class ReadScreenCommandHandler(
    private val accessibility: AccessibilityGateway = AndroidAccessibilityGateway(),
) : CommandHandler {

    override fun supports(command: Command): Boolean = command == Command.ReadScreen

    override fun handle(command: Command): ExecutionResult {
        if (command != Command.ReadScreen) return ExecutionResult.Unavailable
        return when (val outcome = accessibility.readScreen()) {
            is ScreenReadOutcome.Text ->
                if (outcome.lines.isEmpty()) {
                    ExecutionResult.Success("Não encontrei texto para ler nesta tela.")
                } else {
                    ExecutionResult.Success(outcome.lines.joinToString(". "))
                }
            is ScreenReadOutcome.Blocked -> ExecutionResult.Denied
            ScreenReadOutcome.Unavailable -> ExecutionResult.Unavailable
        }
    }
}
