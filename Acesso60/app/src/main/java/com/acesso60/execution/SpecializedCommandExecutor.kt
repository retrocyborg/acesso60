package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/** Executor de uma capacidade específica, selecionado pelo wrapper. */
interface SpecializedCommandExecutor {
    fun supports(command: Command): Boolean

    fun execute(command: Command): ExecutionResult
}
