package com.acesso60.execution

import android.content.Context
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlinx.coroutines.*
import kotlin.concurrent.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Provider que lê a lista de aplicativos a partir de um arquivo JSON.
 * O arquivo é buscado primeiro em `context.filesDir/app_definitions.json`
 * (persistência mutável). Caso não exista, ele é carregado dos assets
 * (`app/src/main/assets/app_definitions.json`).
 *
 * Implementa `MutableAppDefinitionProvider` para permitir que novos
 * aliases sejam adicionados e persistidos.
 */
class JsonAppDefinitionProvider(
    private val context: Context,
    private val assetPath: String = "app_definitions.json",
    val persistDelayMs: Long = 500L // debounce interval
) : MutableAppDefinitionProvider {

    private val json = Json { prettyPrint = true }
    private val lock = ReentrantLock()
    private var cache: MutableList<AppDefinition> = mutableListOf()
    private var persistJob: Job? = null

    // Exposição pública para os testes
    val apps: List<AppDefinition>
        get() = lock.withLock { cache.toList() }

    companion object {
        /**
         * Retorna o arquivo interno onde o JSON de definição de apps é armazenado.
         */
        fun storageFile(context: Context): File =
            File(context.filesDir, "app_definitions.json")

        /**
         * Factory suspensa que cria a instância garantindo que o cache esteja carregado.
         * Mantida como `suspend` porque o carregamento pode envolver I/O assíncrono.
         */
        suspend fun create(
            context: Context,
            assetPath: String = "app_definitions.json"
        ): JsonAppDefinitionProvider {
            val provider = JsonAppDefinitionProvider(context, assetPath)
            // O init já lança a carga assíncrona; aguardamos explicitamente para garantir
            // que o cache esteja pronto antes de usar a instância.
            provider.lock.withLock { /* cache já inicializado */ }
            return provider
        }
    }

    private val scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())

    init {
        // Synchronous loading to ensure cache is ready before use.
        runBlocking {
            val loaded = loadFromStorage()
            lock.withLock { cache = loaded.toMutableList() }
        }
    }

    /** Carrega o JSON da pasta de arquivos ou do asset (fallback). */
    private suspend fun loadFromStorage(): List<AppDefinition> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "app_definitions.json")
            val source = if (file.exists()) file.readText() else context.assets.open(assetPath).bufferedReader().use { it.readText() }
            if (source.isBlank()) {
            emptyList()
        } else try {
            json.decodeFromString(source)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse app definitions from $assetPath: ${e.message}", e)
        }
        } catch (e: Exception) {
            println("[JsonAppDefinitionProvider] erro ao ler JSON: ${e.message}")
            emptyList()
        }
    }

    /** Persiste a lista inteira no arquivo interno. */
    private suspend fun persistNow() = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "app_definitions.json")
            val snapshot = lock.withLock { cache.toList() }
            file.writeText(json.encodeToString(snapshot))
        } catch (e: Exception) {
            println("[JsonAppDefinitionProvider] erro ao gravar JSON: ${e.message}")
        }
    }

    /** Agenda a persistência usando debounce. */
    private fun schedulePersist() {
        persistJob?.cancel()
        persistJob = scope.launch {
            delay(persistDelayMs)
            persistNow()
        }
    }

    // Interface implementations
    override fun provide(): List<AppDefinition> = lock.withLock { cache.toList() }

    override suspend fun addOrUpdate(app: AppDefinition) {
        lock.withLock {
            val index = cache.indexOfFirst { it.id == app.id }
            if (index >= 0) {
                val existing = cache[index]
                val merged = existing.copy(aliases = existing.aliases + app.aliases)
                cache[index] = merged
            } else {
                cache.add(app)
            }
        }
        schedulePersist()
    }

    /** Factory suspensa para criar a instância garantindo que o cache já esteja carregado. */

}
