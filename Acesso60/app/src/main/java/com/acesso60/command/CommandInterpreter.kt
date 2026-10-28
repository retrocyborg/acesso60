package com.acesso60.command

class CommandInterpreter {
    fun interpret(text: String): Command? {
        val normalized = text.trim().lowercase()

        return when {
            normalized == "voltar" -> Command.Back
            normalized == "início" || normalized == "inicio" -> Command.Home
            normalized == "ler a tela" -> Command.ReadScreen
            normalized == "aumentar volume" -> Command.VolumeUp
            normalized == "diminuir volume" -> Command.VolumeDown
            normalized.startsWith("abrir ") ->
                normalized.removePrefix("abrir ").trim()
                    .takeIf { it.isNotEmpty() }
                    ?.let(Command::OpenApp)
            else -> null
        }
    }
}
