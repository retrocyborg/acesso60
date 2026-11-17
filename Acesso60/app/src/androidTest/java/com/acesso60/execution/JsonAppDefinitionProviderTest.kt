package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes básicos para [JsonAppDefinitionProvider] usando a nova factory suspensa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JsonAppDefinitionProviderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun providerLoadsEmptyCacheWhenNoFileExists() = runBlocking {
        // Ensure no previous storage file
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.exists()) file.delete()

        val provider = JsonAppDefinitionProvider.create(context)
        // The cache should be empty
        assertTrue(provider.apps.isEmpty())
    }

    @Test
    fun addOrUpdatePersistsDataAfterDebounceDelay() = runBlocking {
        val provider = JsonAppDefinitionProvider.create(context)
        provider.addOrUpdate(com.acesso60.execution.AppDefinition("com.example", listOf("example")))
        // Wait for debounce period
        delay(provider.persistDelayMs)
        // Verify file was written
        assertTrue(JsonAppDefinitionProvider.storageFile(context).exists())
    }
}
