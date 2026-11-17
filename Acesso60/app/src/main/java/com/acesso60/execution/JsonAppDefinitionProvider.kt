package com.acesso60.execution

import android.content.Context
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Provider que lê a lista de aplicativos a partir de um arquivo JSON.
 * O arquivo é buscado primeiro em `context.filesDir/app_definitions.json`
 * (persistência mutável). Caso não exista, ele é carregado dos assets
 * (`app/src/main/assets/app_definitions.json`).
 *
 * Implementa `MutableAppDefinitionProvider` para permitir que novos
 * aliases sejam adicionados e persistidos.
 *
 * Thread‑safety: todas as leituras e gravações do cache são protegidas por
 * um `ReentrantLock`. O provedor pode ser usado a partir de qualquer thread.
 */
class JsonAppDefinitionProvider(
    private val context: Context,
    private val assetPath: String = "app_definitions.json",
    val persistDelayMs: Long = 500L // debounce interval (default 500 ms)
) : MutableAppDefinitionProvider {

    private val json = Json { prettyPrint = true }
    private val lock = ReentrantLock()
    private var cache: MutableList<AppDefinition> = mutableListOf()
    private var persistJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Exposição pública para os testes
    val apps: List<AppDefinition>
        get() = lock.withLock { cache.toList() }

    companion object {
        /** Retorna o arquivo interno onde o JSON de definição de apps é armazenado. */
        fun storageFile(context: Context): File =
            File(context.filesDir, "app_definitions.json")

        /** Factory suspensa que cria a instância garantindo que o cache esteja carregado. */
        suspend fun create(context: Context, assetPath: String = "app_definitions.json"): JsonAppDefinitionProvider {
            return try {
                val provider = JsonAppDefinitionProvider(context, assetPath)
                try {
                    val loaded = provider.loadFromStorage()
                    provider.lock.withLock { provider.cache = loaded.toMutableList() }
                } catch (e: Exception) {
                    Timber.e(e, "[JsonAppDefinitionProvider] failed to initialise cache, falling back to empty list")
                    provider.lock.withLock { provider.cache = mutableListOf() }
                }
                provider
            } catch (e: Exception) {
                Timber.e(e, "[JsonAppDefinitionProvider] unexpected error during creation, returning empty provider")
                val fallback = JsonAppDefinitionProvider(context, assetPath)
                fallback.lock.withLock { fallback.cache = mutableListOf() }
                fallback
            }
        }
    }

    /** Carrega o JSON da pasta de arquivos ou do asset (fallback). */
    private suspend fun loadFromStorage(): List<AppDefinition> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "app_definitions.json")
            if (file.exists()) {
                val source = file.readText()
                if (source.isBlank()) {
                    Timber.w("[JsonAppDefinitionProvider] storage file is blank, falling back to assets")
                } else {
                    return@withContext try {
                        json.decodeFromString(source)
                    } catch (e: Exception) {
                        Timber.e("[JsonAppDefinitionProvider] Failed to parse app definitions from storage: ${e.message}", e)
                        emptyList()
                    }
                }
            }
            // Fallback to assets
            try {
                context.assets.open(assetPath).bufferedReader().use { reader ->
                    val assetSource = reader.readText()
                    if (assetSource.isBlank()) {
                        Timber.w("[JsonAppDefinitionProvider] asset file is blank")
                        return@withContext emptyList()
                    }
                    return@withContext try {
                        json.decodeFromString(assetSource)
                    } catch (e: Exception) {
                        Timber.e("[JsonAppDefinitionProvider] Failed to parse app definitions from assets: ${e.message}", e)
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "[JsonAppDefinitionProvider] error accessing asset file")
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "[JsonAppDefinitionProvider] error accessing storage file")
            emptyList()
        }
    }

    /** Persiste a lista inteira no arquivo interno. */
    private suspend fun persistNow() = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "app_definitions.json")
            // Ensure directory exists
            file.parentFile?.mkdirs()
            val snapshot = lock.withLock { cache.toList() }
            file.writeText(json.encodeToString(snapshot))
        } catch (e: Exception) {
            Timber.e("[JsonAppDefinitionProvider] erro ao gravar JSON: ${e.message}", e)
        }
    }

    /** Agenda a persistência usando debounce. */
    private fun schedulePersist() {
        lock.withLock {
            persistJob?.cancel()
            persistJob = scope.launch {
                delay(persistDelayMs)
                persistNow()
            }
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

    /** Close the provider and cancel any pending jobs. */
    fun close() {
        lock.withLock {
            persistJob?.cancel()
            scope.cancel()
        }
    }
}
