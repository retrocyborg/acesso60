package br.com.acesso60.core.middleware

import br.com.acesso60.core.logging.ExecutionLogger
import br.com.acesso60.core.logging.SimpleExecutionLogger
import br.com.acesso60.core.logging.ExecutionLogEvent
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/** Core interface for execution middleware. */
interface ExecutionMiddleware {
    // Default simple logger implementation
    val logger: ExecutionLogger get() = SimpleExecutionLogger()

    /**
     * Intercept the execution of a command, measuring duration and logging the event.
     */
    fun intercept(command: Command, next: (Command) -> ExecutionResult): ExecutionResult {
        val start = System.currentTimeMillis()
        val result = next(command)
        val duration = System.currentTimeMillis() - start
        val event = ExecutionLogEvent(command::class.simpleName ?: "Unknown", duration, result)
        logger.log(event)
        return result
    }
}
