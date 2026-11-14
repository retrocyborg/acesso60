package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.execution.VolumeController

/** Handler para controle de volume (subir / descer). */
class VolumeCommandHandler(
    private val controller: VolumeController
) : CommandHandler {

    override fun supports(command: Command): Boolean =
        command == Command.VolumeUp || command == Command.VolumeDown

    override fun handle(command: Command): ExecutionResult = when (command) {
        Command.VolumeUp -> if (controller.raiseVolume()) {
            ExecutionResult.Success("Aumentei o volume.")
        } else {
            ExecutionResult.Unavailable
        }
        Command.VolumeDown -> if (controller.lowerVolume()) {
            ExecutionResult.Success("Diminui o volume.")
        } else {
            ExecutionResult.Unavailable
        }
        else -> ExecutionResult.Unavailable
    }
}
