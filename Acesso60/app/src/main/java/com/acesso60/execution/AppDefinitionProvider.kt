package com.acesso60.execution

/**
 * Interface para prover a lista de definições de aplicativos.
 * Permite que diferentes fontes (hard‑coded, JSON, banco de dados, etc.)
 * sejam injetadas no {@link StaticAppRegistry}. Isso facilita testes unitários
 * e mantém o registro desacoplado da origem dos dados.
 */
interface AppDefinitionProvider {
    /**
     * Retorna a lista de {@link AppDefinition} que deverão ser registradas.
     */
    fun provide(): List<AppDefinition>
}

/**
 * Extensão mutável que permite persistir alterações (inserir/atualizar).
 * Ideal quando o usuário pode adicionar aliases em tempo de execução.
 */
interface MutableAppDefinitionProvider : AppDefinitionProvider {
    /**
     * Persistir a definição (inserir ou atualizar). Deve ser thread‑safe.
     */
    fun addOrUpdate(app: AppDefinition)

    /** Adiciona um alias à definição existente como uma única atualização atômica. */
    fun addAlias(appId: String, alias: String): AppDefinition?

    val aliasPersistenceEvents: kotlinx.coroutines.flow.Flow<AliasPersistenceEvent>?
        get() = null

    fun retryAliasPersistence(alias: String): Boolean = false
}

enum class AliasPersistenceStatus { SAVING, SAVED, FAILED }

data class AliasPersistenceEvent(val alias: String, val status: AliasPersistenceStatus)

/** Implementação simples que recebe as definições via construtor. */
class StaticAppDefinitionProvider(
    private val definitions: List<AppDefinition>
) : AppDefinitionProvider {
    override fun provide(): List<AppDefinition> = definitions
}
