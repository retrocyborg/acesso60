package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

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
) : CommandHandler {

    override fun supports(command: Command): Boolean = command is Command.AddAlias

    override fun handle(command: Command): ExecutionResult {
        val add = command as Command.AddAlias
        val updated = provider.addAlias(add.appId, add.newAlias)
        if (updated == null) {
            return ExecutionResult.Failure("Aplicativo '${add.appId}' não encontrado.")
        }
        return ExecutionResult.Success(
            "Alias '${add.newAlias}' disponível para ${updated.id} nesta sessão; salvamento em andamento.",
        )
    }
}
