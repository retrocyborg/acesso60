package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifica a segurança de concorrência de [JsonAppDefinitionProvider]
 * ao chamar `addOrUpdate` simultaneamente a partir de múltiplas corrotinas.
 */
class JsonAppDefinitionProviderConcurrencyTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `concurrent addOrUpdate does not lose entries` = runTest {
        // Clean storage
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.exists()) file.delete()

        val provider = JsonAppDefinitionProvider.create(context)

        val apps = listOf(
            AppDefinition("com.example.one", listOf("one")),
            AppDefinition("com.example.two", listOf("two")),
            AppDefinition("com.example.three", listOf("three")),
            AppDefinition("com.example.four", listOf("four")),
            AppDefinition("com.example.five", listOf("five"))
        )

        // Launch concurrent updates
        val jobs = apps.map { appDef ->
            async {
                provider.addOrUpdate(appDef)
            }
        }
        // Wait for all to finish
        jobs.awaitAll()

        // Fast‑forward debounce to ensure persistence
        advanceTimeBy(provider.persistDelayMs)

        // Verify all apps are present in cache
        assertEquals(apps.size, provider.apps.size)
        apps.forEach { app ->
            assertEquals(true, provider.apps.any { it.id == app.id })
        }
    }
}
