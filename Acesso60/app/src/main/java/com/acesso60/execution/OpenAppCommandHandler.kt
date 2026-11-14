package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.execution.AppRegistry
import com.acesso60.execution.AppLauncher
import com.acesso60.execution.AppDefinition
import com.acesso60.execution.LaunchResult

/** Handler para abrir aplicativos usando o registro e launcher. */
class OpenAppCommandHandler(
    private val registry: AppRegistry,
    private val launcher: AppLauncher
) : CommandHandler {

    override fun supports(command: Command): Boolean = command is Command.OpenApp

    override fun handle(command: Command): ExecutionResult {
        val openApp = command as? Command.OpenApp ?: return ExecutionResult.Unavailable
        val app = registry.resolve(openApp.appName) ?: return ExecutionResult.Unavailable
        return when (launcher.launch(app.packageName)) {
            LaunchResult.Started -> ExecutionResult.Success("Solicitei a abertura de ${app.id}.")
            LaunchResult.NotInstalled -> ExecutionResult.Unavailable
            is LaunchResult.Failed -> ExecutionResult.Failure("Não foi possível abrir ${app.id}.")
        }
    }
}
