package com.acesso60.execution

interface AppRegistry {
    fun resolve(alias: String): AppDefinition?
}

/** Registry estático e determinístico para a allowlist de aplicativos. */
class StaticAppRegistry(definitions: List<AppDefinition>) : AppRegistry {
    private val byAlias: Map<String, AppDefinition> = definitions
        .flatMap { definition ->
            definition.aliases.map { alias -> normalize(alias) to definition }
        }
        .also { entries ->
            require(entries.map { it.first }.toSet().size == entries.size) {
                "Aliases duplicados no registry de aplicativos."
            }
        }
        .toMap()

    override fun resolve(alias: String): AppDefinition? = byAlias[normalize(alias)]

    private fun normalize(value: String): String = value.trim().lowercase()
}
