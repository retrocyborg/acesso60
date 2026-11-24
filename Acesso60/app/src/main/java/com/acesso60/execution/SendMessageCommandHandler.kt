package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.messaging.MessagingAppCatalog
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.messaging.MessageLauncher

/**
 * Abre o app de mensagens com o texto pronto. Não envia sozinho: o resultado é sempre "não verificado".
 * Só deve receber comandos já validados, classificados e confirmados pelo núcleo.
 */
class SendMessageCommandHandler(
    private val launcher: MessageLauncher,
) : CommandHandler {

    override fun supports(command: Command): Boolean = command is Command.SendMessage

    override fun handle(command: Command): ExecutionResult {
        val send = command as? Command.SendMessage ?: return ExecutionResult.Unavailable
        val appName = MessagingAppCatalog.findById(send.appId)?.displayName ?: return ExecutionResult.Unavailable
        return when (launcher.launch(send)) {
            LaunchResult.Started -> ExecutionResult.Success(
                "Abri o $appName com a mensagem pronta. Escolha ${send.recipient} e toque em enviar."
            )
            LaunchResult.NotInstalled -> ExecutionResult.Unavailable
            is LaunchResult.Failed -> ExecutionResult.Failure("Não foi possível abrir o $appName.")
        }
    }
}
