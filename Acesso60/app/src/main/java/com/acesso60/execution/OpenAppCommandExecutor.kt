package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

class OpenAppCommandExecutor(
    private val registry: AppRegistry,
    private val launcher: AppLauncher,
) : SpecializedCommandExecutor {
    override fun supports(command: Command): Boolean = command is Command.OpenApp

    override fun execute(command: Command): ExecutionResult {
        val openApp = command as? Command.OpenApp
            ?: return ExecutionResult.Unavailable
        val app = registry.resolve(openApp.appName)
            ?: return ExecutionResult.Unavailable

        return when (launcher.launch(app.packageName)) {
            LaunchResult.Started -> ExecutionResult.Success("Solicitei a abertura de ${app.id}.")
            LaunchResult.NotInstalled -> ExecutionResult.Unavailable
            is LaunchResult.Failed -> ExecutionResult.Failure("Não foi possível abrir ${app.id}.")
        }
    }
}
