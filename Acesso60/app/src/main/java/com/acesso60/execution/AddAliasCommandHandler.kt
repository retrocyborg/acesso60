package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handler responsável por tratar o comando [Command.AddAlias] que permite ao
 * usuário adicionar ou atualizar um alias para um aplicativo existente.
 *
 * O handler recebe um [MutableAppDefinitionProvider] para que a alteração seja
 * persistida (no caso, via JSON). A operação de escrita é feita em uma
 * corrotina separada para não bloquear a thread de UI.
 */
class AddAliasCommandHandler(
    private val provider: MutableAppDefinitionProvider,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CommandHandler {

    override fun supports(command: Command): Boolean = command is Command.AddAlias

    override fun handle(command: Command): ExecutionResult {
        val add = command as Command.AddAlias
        // Busca a definição atual
        val existing = provider.provide().find { it.id == add.appId }
        if (existing == null) {
            return ExecutionResult.Failure("Aplicativo '${add.appId}' não encontrado.")
        }
        // Constrói nova definição mesclando aliases (evita duplicatas)
        val merged = existing.copy(aliases = existing.aliases + add.newAlias)
        // Persiste de forma assíncrona
        scope.launch {
            provider.addOrUpdate(merged)
        }
        return ExecutionResult.Success("Alias '${add.newAlias}' adicionado ao app '${add.appId}'.")
    }
}
