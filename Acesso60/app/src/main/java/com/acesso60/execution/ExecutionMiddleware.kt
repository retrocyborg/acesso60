package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/**
 * Middleware para interceptação ordenada de comandos durante o pipeline de execução.
 *
 * Invariantes arquiteturais:
 * 1. Um middleware não pode autorizar comandos rejeitados pelo núcleo.
 * 2. Um middleware não pode transformar um resultado de falha/indisponibilidade em sucesso.
 * 3. Qualquer exceção disparada em um middleware deve ser contida com falha segura.
 */
fun interface ExecutionMiddleware {
    fun intercept(command: Command, next: (Command) -> ExecutionResult): ExecutionResult
}
