package br.com.acesso60.core.scripting

/**
 * Catálogo e repositório em memória para scripts e rotinas declarativas registradas.
 */
class ScriptRegistry {
    private val scripts = mutableMapOf<String, ScriptDefinition>()

    /**
     * Registra ou atualiza um script no catálogo.
     */
    fun register(script: ScriptDefinition) {
        val id = normalize(script.id)
        require(id.isNotEmpty()) { "O identificador do script não pode ser vazio." }
        val aliases = script.aliases.map(::normalize)
        require(aliases.isNotEmpty() && aliases.all(String::isNotEmpty)) {
            "O script deve possuir aliases de ativação válidos."
        }
        require(aliases.distinct().size == aliases.size) { "O script contém aliases duplicados." }

        val otherScripts = scripts.filterKeys { it != id }.values
        val occupiedAliases = otherScripts.flatMap { it.aliases }.map(::normalize).toSet()
        require(aliases.none { it in occupiedAliases }) { "Alias já registrado para outro script." }
        scripts[id] = script
    }

    /**
     * Remove um script do catálogo a partir de seu identificador.
     */
    fun unregister(scriptId: String) {
        scripts.remove(normalize(scriptId))
    }

    /**
     * Retorna a listagem imutável de todos os scripts atualmente registrados.
     */
    fun getAll(): List<ScriptDefinition> = scripts.values.toList()

    /**
     * Localiza um script registrado que possua o alias informado (busca normalizada, case-insensitive).
     */
    fun findByAlias(alias: String): ScriptDefinition? {
        val normalized = normalize(alias)
        if (normalized.isEmpty()) return null
        return scripts.values.singleOrNull { script -> script.aliases.any { normalize(it) == normalized } }
    }

    private fun normalize(value: String): String = value.trim().lowercase()
}
