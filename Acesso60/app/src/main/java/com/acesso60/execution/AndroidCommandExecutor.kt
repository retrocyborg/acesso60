package com.acesso60.execution

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.accessibility.AccessibilityGateway
import com.acesso60.accessibility.AndroidAccessibilityGateway

/** Executor mínimo da primeira fatia Beta: navegação BACK via serviço ativo. */
class AndroidCommandExecutor(
    private val accessibility: AccessibilityGateway = AndroidAccessibilityGateway(),
) : CommandExecutor {
    override fun execute(command: Command): ExecutionResult = when (command) {
        Command.Back -> if (accessibility.performBack()) {
            ExecutionResult.Success("Voltei para a tela anterior.")
        } else {
            ExecutionResult.Unavailable
        }
        else -> ExecutionResult.Unavailable
    }
}
