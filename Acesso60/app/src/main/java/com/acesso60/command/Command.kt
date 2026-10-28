package com.acesso60.command

sealed interface Command {
    data object Back : Command
    data object Home : Command
    data object ReadScreen : Command
    data object VolumeUp : Command
    data object VolumeDown : Command
    data class OpenApp(val appName: String) : Command
}
