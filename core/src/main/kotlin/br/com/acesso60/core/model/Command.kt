package br.com.acesso60.core.model

/**
 * Representa os comandos tipados do domínio Acesso60.
 * Desacoplado de quaisquer APIs do sistema operacional ou frameworks de interface.
 */
sealed interface Command {
    /**
     * Comando de navegação para voltar à tela anterior.
     */
    data object Back : Command

    /**
     * Comando de navegação para retornar à tela inicial (Home).
     */
    data object Home : Command
    data object ReadScreen : Command
    data object VolumeUp : Command
    data object VolumeDown : Command
    data class OpenApp(val appName: String) : Command

    /**
     * Ação classificada como sensível (ex.: envio de dados ou exclusão),
     * utilizada para demonstrar e validar a política de risco e autorização.
     */
    data class SensitiveAction(val description: String) : Command
}
