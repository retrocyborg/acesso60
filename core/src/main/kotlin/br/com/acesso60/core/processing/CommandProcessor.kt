package br.com.acesso60.core.processing

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.middleware.ExecutionMiddleware

/**
 * Orquestrador que delega a execução de um [Command] ao handler adequado
 * e aplica a lista de middlewares configurados.
 */
class CommandProcessor(
    private val handlers: List<CommandHandler>,
    private val middlewares: List<ExecutionMiddleware> = emptyList()
) {
    /** Processa o comando e devolve o resultado final. */
    fun process(command: Command): ExecutionResult {
        // Seleciona o handler que suporta o comando
        val handler = handlers.firstOrNull { it.supports(command) }
            ?: return ExecutionResult.Unavailable

                // Construção da cadeia de interceptação
        var next: (Command) -> ExecutionResult = { cmd -> handler.handle(cmd) }
        for (mw in middlewares.reversed()) {
            val previous = next
            next = { cmd -> mw.intercept(cmd, previous) }
        }
        return next(command)
    }
}
