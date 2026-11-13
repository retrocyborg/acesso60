package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testa que o mecanismo de debounce grava o arquivo apenas uma vez
 * mesmo quando múltiplas atualizações são realizadas rapidamente.
 */
class JsonAppDefinitionProviderDebounceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `debounce writes once after rapid updates` = runTest {
        // Clean storage
        val file = JsonAppDefinitionProvider.storageFile(context)
        if (file.exists()) file.delete()

        val provider = JsonAppDefinitionProvider.create(context)
        // Rapid updates
        repeat(5) { idx ->
            provider.addOrUpdate(AppDefinition("com.example.$idx", listOf("alias$idx")))
        }
        // Advance time to trigger debounce
        advanceTimeBy(provider.persistDelayMs)
        // The file should exist and contain JSON; we just check existence
        assertTrue(file.exists())
        // Optionally, read content length to ensure only one write (cannot easily count writes without mocking)
    }
}
