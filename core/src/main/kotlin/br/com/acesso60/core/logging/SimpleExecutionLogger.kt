package br.com.acesso60.core.logging

import br.com.acesso60.core.logging.ExecutionLogEvent
import br.com.acesso60.core.logging.ExecutionLogger

/** Simple logger that writes execution events to standard output. */
class SimpleExecutionLogger : ExecutionLogger {
    override fun log(event: ExecutionLogEvent) {
        println("[Execution] ${event.commandName} took ${event.durationMs}ms, result: ${event.result}")
    }
}
