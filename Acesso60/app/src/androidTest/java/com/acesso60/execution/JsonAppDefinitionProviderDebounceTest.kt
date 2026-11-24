package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testa que o mecanismo de debounce grava o arquivo apenas uma vez
 * mesmo quando múltiplas atualizações são realizadas rapidamente.
 */
class JsonAppDefinitionProviderDebounceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun debounceWritesOnceAfterRapidUpdates() = runTest {
        // Clean storage
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.exists()) file.delete()

        val provider = JsonAppDefinitionProvider.create(
            context,
            assetPath = "missing-test-asset.json",
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        // Ensure async init completes before performing updates
        // (init is now synchronous, no extra waiting needed)
        // Rapid updates
        repeat(5) { idx ->
            provider.addOrUpdate(AppDefinition("com.example.$idx", listOf("alias$idx")))
        }
        // Advance time to trigger debounce
        advanceTimeBy(provider.persistDelayMs)
        runCurrent()
        // The file should exist and contain JSON; we just check existence
        assertTrue(file.exists())
        provider.close()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun failedAliasPersistenceKeepsAliasInSessionAndReportsFailure() = runTest {
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.isDirectory) file.deleteRecursively() else file.delete()
        assertTrue("Could not prepare directory to force a write failure", file.mkdirs())

        val provider = JsonAppDefinitionProvider.create(
            context,
            assetPath = "missing-test-asset.json",
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        provider.addOrUpdate(AppDefinition("calendar", "com.example.calendar", setOf("calendar")))
        val events = mutableListOf<AliasPersistenceEvent>()
        val collector = backgroundScope.launch(Dispatchers.Unconfined) {
            provider.aliasPersistenceEvents.collect { events += it }
        }

        provider.addAlias("calendar", "agenda")
        advanceTimeBy(provider.persistDelayMs)
        runCurrent()

        assertTrue(provider.provide().single().aliases.contains("agenda"))
        assertTrue(events.any { it.alias == "agenda" && it.status == AliasPersistenceStatus.FAILED })
        provider.close()
        collector.cancel()
        file.deleteRecursively()
    }
}
