package br.com.acesso60.core.interpreter

import br.com.acesso60.core.model.Command

/**
 * Definição modular de um comando com seus aliases reconhecíveis.
 */
data class CommandDefinition(
    val command: Command,
    val aliases: Set<String>,
)

/**
 * Registro dinâmico de definições e aliases de comando.
 * Permite adicionar novos comandos e variações linguísticas sem acoplamento rígido.
 */
class CommandRegistry(
    initialDefinitions: List<CommandDefinition> = defaultDefinitions(),
) {
    private val aliasMap = mutableMapOf<String, Command>()

    init {
        for (def in initialDefinitions) {
            register(def)
        }
    }

    fun register(definition: CommandDefinition) {
        for (alias in definition.aliases) {
            val normalizedAlias = normalize(alias)
            val existing = aliasMap[normalizedAlias]
            if (existing != null && existing != definition.command) {
                throw IllegalArgumentException(
                    "Conflito de alias: '$normalizedAlias' já registrado para $existing."
                )
            }
            aliasMap[normalizedAlias] = definition.command
        }
    }

    fun resolve(normalizedText: String): Command? = aliasMap[normalizedText]

    companion object {
        fun defaultDefinitions(): List<CommandDefinition> = listOf(
            CommandDefinition(Command.Back, setOf("voltar", "retornar", "volta")),
            CommandDefinition(Command.Home, setOf("inicio", "tela inicial", "home")),
            CommandDefinition(Command.ReadScreen, setOf("ler a tela", "ler tela", "leia a tela")),
            CommandDefinition(Command.VolumeUp, setOf("aumentar volume", "aumenta o volume", "mais volume")),
            CommandDefinition(Command.VolumeDown, setOf("diminuir volume", "diminui o volume", "menos volume", "abaixar volume")),
        )

        fun normalize(input: String): String {
            val trimmed = input.trim().lowercase()
            val nfd = java.text.Normalizer.normalize(trimmed, java.text.Normalizer.Form.NFD)
            val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(nfd).replaceAll("")
        }
    }
}
