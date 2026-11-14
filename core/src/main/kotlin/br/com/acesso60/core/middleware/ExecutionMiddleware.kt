package br.com.acesso60.core.middleware

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/** Core interface for execution middleware. */
interface ExecutionMiddleware {
    fun before(command: Command) {}
    fun after(command: Command, result: ExecutionResult) {}
    /**
     * Intercept the execution of a command, allowing the middleware to run code before and after.
     * The default implementation calls [before], then invokes the next handler, then calls [after].
     */
    fun intercept(command: Command, next: (Command) -> ExecutionResult): ExecutionResult {
        before(command)
        val result = next(command)
        after(command, result)
        return result
    }
}
