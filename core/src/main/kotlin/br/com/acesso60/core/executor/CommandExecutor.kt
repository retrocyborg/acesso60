package br.com.acesso60.core.executor

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/**
 * Contrato de abstração para execução de comandos (DIP do SOLID).
 * Permite desacoplar a lógica de domínio de chamadas diretas a APIs Android ou hardware.
 */
interface CommandExecutor {
    /**
     * Executa o comando autorizado e retorna o resultado estruturado.
     */
    fun execute(command: Command): ExecutionResult
}
