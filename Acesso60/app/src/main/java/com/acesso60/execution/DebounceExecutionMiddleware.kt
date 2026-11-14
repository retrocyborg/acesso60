package com.acesso60.execution

import java.util.concurrent.ConcurrentHashMap
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.middleware.ExecutionMiddleware

/**
 * Middleware para prevenção de disparos duplicados ou acidentais em comandos não repetíveis.
 *
 * Distingue ações naturalmente repetíveis (como navegação e ajuste de volume)
 * de ações que abrem aplicações ou iniciam tarefas custosas/irreversíveis.
 *
 * @param debounceWindowMs Janela temporal mínima em milissegundos entre execuções do mesmo comando não repetível.
 * @param isRepeatable Predicado que determina se o comando é isento de debounce.
 * @param clock Fornecedor de tempo injetável para testes determinísticos.
 */
class DebounceExecutionMiddleware(
    private val debounceWindowMs: Long = 1200L,
    private val isRepeatable: (Command) -> Boolean = ::defaultIsRepeatable,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ExecutionMiddleware {

    private val lastExecutionMap = ConcurrentHashMap<String, Long>()

    override fun intercept(
        command: Command,
        next: (Command) -> ExecutionResult,
    ): ExecutionResult {
        if (isRepeatable(command)) {
            return next(command)
        }

        val key = commandKey(command)
        val now = clock()

        synchronized(lastExecutionMap) {
            val lastTime = lastExecutionMap[key]
            if (lastTime != null && (now - lastTime.toLong()) < debounceWindowMs) {
                return ExecutionResult.Failure("Comando já acionado recentemente. Aguarde um instante.")
            }
            lastExecutionMap[key] = now
        }

        return next(command)
    }

    private fun commandKey(command: Command): String = when (command) {
        is Command.OpenApp -> "OpenApp:${command.appName.lowercase()}"
        else -> command::class.qualifiedName ?: command.toString()
    }

    companion object {
        fun defaultIsRepeatable(command: Command): Boolean = when (command) {
            is Command.Back,
            is Command.Home,
            is Command.VolumeUp,
            is Command.VolumeDown -> true
            else -> false
        }
    }
}
