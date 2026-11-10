package br.com.acesso60.core.executor

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/**
 * Handler responsável por tratar um único tipo de [Command].
 * Implementações residem nos módulos de plataforma (ex.: Android) e
 * podem depender de APIs específicas desse ambiente.
 */
interface CommandHandler {
    /** Returns true when this handler can process the given command. */
    fun supports(command: Command): Boolean

    /** Executes the command and returns an [ExecutionResult]. */
    fun handle(command: Command): ExecutionResult
}
