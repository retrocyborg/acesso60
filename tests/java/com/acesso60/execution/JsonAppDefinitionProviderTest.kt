package com.acesso60.execution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Testes unitários para [JsonAppDefinitionProvider].
 * Usa Robolectric para prover um Context Android e corrotinas de teste.
 */
class JsonAppDefinitionProviderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @DisplayName("Carrega do asset quando o arquivo interno não existe")
    fun loadFromAssetsWhenFileAbsent() = runTest {
        // Garante que o arquivo interno não exista
        val internal = File(context.filesDir, "app_definitions.json")
        if (internal.exists()) internal.delete()

        val provider = JsonAppDefinitionProvider(context)
        val defs = provider.provide()
        assertEquals(1, defs.size)
        assertEquals("WhatsApp", defs[0].id)
    }

    @Test
    @DisplayName("Persistir nova definição adiciona ao arquivo interno")
    fun persistNewDefinitionAddsToFile() = runTest {
        // Inicia com asset vazio (ou já existente) e garante um estado limpo
        val internal = File(context.filesDir, "app_definitions.json")
        internal.delete()
        val provider = JsonAppDefinitionProvider(context)
        val newApp = AppDefinition(
            id = "Telegram",
            packageName = "org.telegram.messenger",
            aliases = setOf("telegram", "tg")
        )
        provider.addOrUpdate(newApp)
        // Recarrega o provider para garantir que a persistência ocorreu
        val reloaded = JsonAppDefinitionProvider(context)
        val defs = reloaded.provide()
        assertTrue(defs.any { it.id == "Telegram" })
    }

    @Test
    @DisplayName("addOrUpdate mescla aliases sem duplicar")
    fun addOrUpdateMergesAliases() = runTest {
        val provider = JsonAppDefinitionProvider(context)
        // Primeiro adiciona um app com um alias
        val base = AppDefinition(
            id = "Zoom",
            packageName = "us.zoom.videomeetings",
            aliases = setOf("zoom")
        )
        provider.addOrUpdate(base)
        // Depois adiciona o mesmo app com um novo alias
        val updated = AppDefinition(
            id = "Zoom",
            packageName = "us.zoom.videomeetings",
            aliases = setOf("meeting")
        )
        provider.addOrUpdate(updated)
        val result = provider.provide().first { it.id == "Zoom" }
        assertEquals(setOf("zoom", "meeting"), result.aliases)
    }
}
