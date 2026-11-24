package com.acesso60.execution

interface AppRegistry {
    fun resolve(alias: String): AppDefinition?
}

/**
 * Registry para a allowlist de aplicativos.
 * Quando instanciado com um [AppDefinitionProvider], resolve consultas dinamicamente
 * para refletir novos aliases adicionados em tempo de execução sem necessitar reiniciar o app.
 */
class StaticAppRegistry(
    private val provider: AppDefinitionProvider?,
    private val staticDefinitions: List<AppDefinition> = emptyList(),
) : AppRegistry {

    constructor(definitions: List<AppDefinition>) : this(
        provider = null,
        staticDefinitions = definitions,
    ) {
        validate(definitions)
    }

    constructor(provider: AppDefinitionProvider) : this(
        provider = provider,
        staticDefinitions = emptyList(),
    ) {
        validate(provider.provide())
    }

    private fun validate(defs: List<AppDefinition>) {
        val entries = defs.flatMap { definition ->
            definition.aliases.map { alias -> normalize(alias) to definition }
        }
        require(entries.map { it.first }.toSet().size == entries.size) {
            "Aliases duplicados no registry de aplicativos."
        }
    }

    private fun normalize(value: String): String = value.trim().lowercase()

    override fun resolve(alias: String): AppDefinition? {
        val normalized = normalize(alias)
        val defs = provider?.provide() ?: staticDefinitions
        return defs.firstOrNull { definition ->
            definition.aliases.any { normalize(it) == normalized }
        }
    }
}
