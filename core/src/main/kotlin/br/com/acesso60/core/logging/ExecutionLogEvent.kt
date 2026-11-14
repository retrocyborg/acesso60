package br.com.acesso60.core.logging

import br.com.acesso60.core.model.ExecutionResult

/**
 * Data class representing a log event for a command execution.
 */
data class ExecutionLogEvent(
    val commandName: String,
    val durationMs: Long,
    val result: ExecutionResult
)
