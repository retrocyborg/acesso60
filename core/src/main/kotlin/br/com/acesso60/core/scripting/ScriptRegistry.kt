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
        scripts[script.id] = script
    }

    /**
     * Remove um script do catálogo a partir de seu identificador.
     */
    fun unregister(scriptId: String) {
        scripts.remove(scriptId)
    }

    /**
     * Retorna a listagem imutável de todos os scripts atualmente registrados.
     */
    fun getAll(): List<ScriptDefinition> = scripts.values.toList()

    /**
     * Localiza um script registrado que possua o alias informado (busca normalizada, case-insensitive).
     */
    fun findByAlias(alias: String): ScriptDefinition? {
        val normalized = alias.trim().lowercase()
        if (normalized.isEmpty()) return null
        return scripts.values.firstOrNull { script ->
            script.aliases.any { it.trim().lowercase() == normalized }
        }
    }
}
