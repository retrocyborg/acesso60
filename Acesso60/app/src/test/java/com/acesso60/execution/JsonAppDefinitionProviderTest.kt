package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes básicos para [JsonAppDefinitionProvider] usando a nova factory suspensa.
 */
class JsonAppDefinitionProviderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `provider loads empty cache when no file exists` = runTest {
        // Ensure no previous storage file
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.exists()) file.delete()

        val provider = JsonAppDefinitionProvider.create(context)
        // The cache should be empty
        assertTrue(provider.apps.isEmpty())
    }

    @Test
    fun `addOrUpdate persists data after debounce delay` = runTest {
        val provider = JsonAppDefinitionProvider.create(context)
        provider.addOrUpdate(com.acesso60.execution.AppDefinition("com.example", listOf("example")))
        // Fast‑forward virtual time to trigger debounce (default 500 ms)
        advanceTimeBy(provider.persistDelayMs)
        // Verify file was written
        assertTrue(JsonAppDefinitionProvider.storageFile(context).exists())
    }
}
