package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes básicos para [JsonAppDefinitionProvider] usando a nova factory suspensa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JsonAppDefinitionProviderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun providerLoadsEmptyCacheWhenAssetDoesNotExist() = runTest {
        // Ensure no previous storage file
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.exists()) file.delete()

        val provider = JsonAppDefinitionProvider.create(
            context,
            assetPath = "missing-test-asset.json",
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        // The cache should be empty
        assertTrue(provider.apps.isEmpty())
        provider.close()
    }

    @Test
    fun addOrUpdatePersistsDataAfterDebounceDelay() = runTest {
        val provider = JsonAppDefinitionProvider.create(
            context,
            assetPath = "missing-test-asset.json",
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        provider.addOrUpdate(com.acesso60.execution.AppDefinition("com.example", listOf("example")))
        advanceTimeBy(provider.persistDelayMs)
        runCurrent()
        // Verify file was written
        assertTrue(JsonAppDefinitionProvider.storageFile(context).exists())
        provider.close()
    }

    @Test
    fun malformedLocalFileFallsBackToBundledAsset() = runTest {
        val file = JsonAppDefinitionProvider.storageFile(context)
        file.writeText("not valid json")

        val provider = JsonAppDefinitionProvider.create(
            context,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        assertTrue(provider.apps.any { "whatsapp" in it.aliases })
        provider.close()
    }
}
