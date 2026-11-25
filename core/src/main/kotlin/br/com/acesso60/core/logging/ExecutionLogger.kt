package br.com.acesso60.core.logging

import br.com.acesso60.core.logging.ExecutionLogEvent

interface ExecutionLogger {
    fun log(event: ExecutionLogEvent)
}
