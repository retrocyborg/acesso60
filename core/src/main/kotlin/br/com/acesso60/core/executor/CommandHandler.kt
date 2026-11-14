package br.com.acesso60.core.executor

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/** Core interface for handling commands. */
interface CommandHandler {
    fun supports(command: Command): Boolean
    fun handle(command: Command): ExecutionResult
}
