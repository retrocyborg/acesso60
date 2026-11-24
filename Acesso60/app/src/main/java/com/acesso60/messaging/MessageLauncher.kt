package com.acesso60.messaging

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import br.com.acesso60.core.messaging.MessagingAppCatalog
import br.com.acesso60.core.messaging.MessagingAppOption
import br.com.acesso60.core.model.Command
import com.acesso60.execution.LaunchResult

/**
 * Monta o Intent de envio. Importante: o Android não permite escolher o contato pelo nome sem
 * permissão de contatos; por isso o app abre com o TEXTO pronto e a pessoa escolhe o contato e toca
 * em enviar. O nome do destinatário NÃO é incluído no corpo da mensagem.
 */
object MessageIntentFactory {
    fun build(command: Command.SendMessage, app: MessagingAppOption): Intent =
        if (app.id == MessagingAppCatalog.SMS) {
            // Sem setPackage: o app de SMS padrão varia por fabricante (Google, Samsung etc.).
            Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:")).apply {
                putExtra("sms_body", command.messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, command.messageText)
                setPackage(app.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
}

interface MessageLauncher {
    fun launch(command: Command.SendMessage): LaunchResult
}

class AndroidMessageLauncher(private val context: Context) : MessageLauncher {
    override fun launch(command: Command.SendMessage): LaunchResult {
        val app = MessagingAppCatalog.findById(command.appId)
            ?: return LaunchResult.Failed("Aplicativo de mensagens não permitido.")
        if (app.id != MessagingAppCatalog.SMS &&
            context.packageManager.getLaunchIntentForPackage(app.packageName) == null
        ) {
            return LaunchResult.NotInstalled
        }
        return try {
            context.startActivity(MessageIntentFactory.build(command, app))
            LaunchResult.Started
        } catch (_: ActivityNotFoundException) {
            LaunchResult.NotInstalled
        } catch (_: SecurityException) {
            LaunchResult.Failed("Permissão insuficiente para abrir o aplicativo.")
        }
    }
}
