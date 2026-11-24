package com.acesso60.execution

import android.content.Context
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val persistDelayMs: Long = 500L, // debounce interval (default 500 ms)
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MutableAppDefinitionProvider {

    private val json = Json { prettyPrint = true }
    private val lock = ReentrantLock()
    private var cache: MutableList<AppDefinition> = mutableListOf()
    private var persistJob: Job? = null
    private val pendingAliases = linkedSetOf<String>()
    private val _aliasPersistenceEvents = MutableSharedFlow<AliasPersistenceEvent>(extraBufferCapacity = 32)
    override val aliasPersistenceEvents = _aliasPersistenceEvents.asSharedFlow()
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Exposição pública para os testes
    val apps: List<AppDefinition>
        get() = lock.withLock { cache.toList() }

    companion object {
        /** Retorna o arquivo interno onde o JSON de definição de apps é armazenado. */
        fun storageFile(context: Context): File =
            File(context.filesDir, "app_definitions.json")

        /** Factory suspensa que cria a instância garantindo que o cache esteja carregado. */
        suspend fun create(
            context: Context,
            assetPath: String = "app_definitions.json",
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): JsonAppDefinitionProvider {
            return try {
                val provider = JsonAppDefinitionProvider(context, assetPath, ioDispatcher = ioDispatcher)
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
                val fallback = JsonAppDefinitionProvider(context, assetPath, ioDispatcher = ioDispatcher)
                fallback.lock.withLock { fallback.cache = mutableListOf() }
                fallback
            }
        }
    }

    /** Carrega o JSON da pasta de arquivos ou do asset (fallback). */
    private suspend fun loadFromStorage(): List<AppDefinition> = withContext(ioDispatcher) {
        try {
            val file = File(context.filesDir, "app_definitions.json")
            if (file.exists()) {
                val source = file.readText()
                if (source.isBlank()) {
                    Timber.w("[JsonAppDefinitionProvider] storage file is blank, falling back to assets")
                } else {
                    val decoded: List<AppDefinition>? = try {
                        json.decodeFromString(source)
                    } catch (e: Exception) {
                        Timber.e(e, "[JsonAppDefinitionProvider] invalid local catalog; falling back to bundled asset")
                        null
                    }
                    if (decoded != null) return@withContext decoded
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
    private suspend fun persistSnapshot(snapshot: List<AppDefinition>): Boolean = withContext(ioDispatcher) {
        try {
            val file = File(context.filesDir, "app_definitions.json")
            // Ensure directory exists
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(snapshot))
            true
        } catch (e: Exception) {
            Timber.e("[JsonAppDefinitionProvider] erro ao gravar JSON: ${e.message}", e)
            false
        }
    }

    /** Agenda a persistência usando debounce. */
    private fun schedulePersist() {
        lock.withLock {
            persistJob?.cancel()
            persistJob = scope.launch {
                delay(persistDelayMs)
                val (snapshot, aliases) = lock.withLock { cache.toList() to pendingAliases.toList() }
                val saved = persistSnapshot(snapshot)
                lock.withLock { pendingAliases.removeAll(aliases.toSet()) }
                aliases.forEach { alias ->
                    _aliasPersistenceEvents.emit(
                        AliasPersistenceEvent(alias, if (saved) AliasPersistenceStatus.SAVED else AliasPersistenceStatus.FAILED),
                    )
                }
            }
        }
    }

    // Interface implementations
    override fun provide(): List<AppDefinition> = lock.withLock { cache.toList() }

    override fun addOrUpdate(app: AppDefinition) {
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

    override fun addAlias(appId: String, alias: String): AppDefinition? {
        val updated = lock.withLock {
            val index = cache.indexOfFirst { it.id == appId }
            if (index < 0) return null
            val current = cache[index]
            val next = current.copy(aliases = current.aliases + alias)
            cache[index] = next
            next
        }
        lock.withLock { pendingAliases += alias }
        _aliasPersistenceEvents.tryEmit(AliasPersistenceEvent(alias, AliasPersistenceStatus.SAVING))
        schedulePersist()
        return updated
    }

    override fun retryAliasPersistence(alias: String): Boolean {
        val exists = lock.withLock { cache.any { alias in it.aliases } }
        if (!exists) return false
        lock.withLock { pendingAliases += alias }
        _aliasPersistenceEvents.tryEmit(AliasPersistenceEvent(alias, AliasPersistenceStatus.SAVING))
        schedulePersist()
        return true
    }

    /** Close the provider and cancel any pending jobs. */
    fun close() {
        lock.withLock {
            persistJob?.cancel()
            scope.cancel()
        }
    }
}
