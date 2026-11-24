package com.acesso60.execution

import br.com.acesso60.core.logging.ExecutionLogEvent
import br.com.acesso60.core.logging.ExecutionLogger
import br.com.acesso60.core.middleware.ExecutionMiddleware
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/**
 * Middleware de auditoria e observabilidade da execução.
 * Registra o início da execução, a duração decorrida e o resultado tipado.
 *
 * Em caso de falha no coletor de log, a execução não é abortada e nenhuma
 * informação sensível ou stack trace é exposta ao usuário.
 */
class LoggingExecutionMiddleware(
    override val logger: ExecutionLogger,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ExecutionMiddleware {

    override fun intercept(
        command: Command,
        next: (Command) -> ExecutionResult,
    ): ExecutionResult {
        val start = clock()
        val result = try {
            next(command)
        } catch (ex: Exception) {
            ExecutionResult.Failure("Não foi possível executar o comando.")
        }
        val duration = clock() - start

        try {
            logger.log(
                ExecutionLogEvent(
                    commandName = command::class.simpleName ?: "UnknownCommand",
                    durationMs = duration.coerceAtLeast(0L),
                    result = result,
                )
            )
        } catch (_: Exception) {
            // Falha no mecanismo de log não deve quebrar o fluxo principal da aplicação.
        }

        return result
    }
}
