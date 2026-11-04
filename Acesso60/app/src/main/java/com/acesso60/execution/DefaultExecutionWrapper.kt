package com.acesso60.execution

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/**
 * Ponto único de despacho da execução Android.
 * Recebe somente comandos já interpretados, validados e autorizados.
 */
class DefaultExecutionWrapper(
    private val delegates: List<SpecializedCommandExecutor>,
) : CommandExecutor {
    override fun execute(command: Command): ExecutionResult {
        val matches = delegates.filter { it.supports(command) }
        if (matches.isEmpty()) return ExecutionResult.Unavailable
        if (matches.size > 1) {
            return ExecutionResult.Failure("Configuração ambígua de executor.")
        }

        return try {
            matches.single().execute(command)
        } catch (_: Exception) {
            ExecutionResult.Failure("Não foi possível executar o comando.")
        }
    }
}
