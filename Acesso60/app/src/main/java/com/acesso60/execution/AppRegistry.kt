package com.acesso60.execution

interface AppRegistry {
    fun resolve(alias: String): AppDefinition?
}

/** Registry estático e determinístico para a allowlist de aplicativos. */
class StaticAppRegistry(
    private val definitions: List<AppDefinition>
) : AppRegistry {

    constructor(provider: AppDefinitionProvider) : this(provider.provide())

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

    private fun normalize(value: String): String = value.trim().lowercase()

    override fun resolve(alias: String): AppDefinition? = byAlias[normalize(alias)]
}

/** Registry que consulta o provider em cada resolução e observa atualizações em memória. */
class ProviderAppRegistry(
    private val provider: AppDefinitionProvider,
) : AppRegistry {
    override fun resolve(alias: String): AppDefinition? =
        StaticAppRegistry(provider).resolve(alias)
}
