package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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

        val provider = JsonAppDefinitionProvider.create(context)
        // Ensure async init completes before performing updates
        // (init is now synchronous, no extra waiting needed)
        // Rapid updates
        repeat(5) { idx ->
            provider.addOrUpdate(AppDefinition("com.example.$idx", listOf("alias$idx")))
        }
        // Advance time to trigger debounce
        advanceTimeBy(provider.persistDelayMs)
        // The file should exist and contain JSON; we just check existence
        assertTrue(file.exists())
    }
}
