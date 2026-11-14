package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult

/** Executor especializado para ajuste de volume do dispositivo. */
class VolumeCommandExecutor(
    private val controller: VolumeController,
) : SpecializedCommandExecutor {

    override fun supports(command: Command): Boolean =
        command == Command.VolumeUp || command == Command.VolumeDown

    override fun execute(command: Command): ExecutionResult = when (command) {
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
