package com.acesso60.execution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppRegistryTest {
    private val whatsapp = AppDefinition("whatsapp", "com.whatsapp", setOf("whatsapp", "zap"))

    @Test
    fun `resolves aliases ignoring case and trimming surrounding spaces`() {
        val registry = StaticAppRegistry(listOf(whatsapp))

        assertEquals(whatsapp, registry.resolve("  WhatsApp "))
        assertEquals(whatsapp, registry.resolve("ZAP"))
    }

    @Test
    fun `unknown alias is not resolved`() {
        assertNull(StaticAppRegistry(listOf(whatsapp)).resolve("telegram"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate aliases are rejected`() {
        StaticAppRegistry(
            listOf(
                whatsapp,
                AppDefinition("other", "com.example.other", setOf("WhatsApp")),
            ),
        )
    }

    @Test
    fun `provider registry sees aliases added after construction`() {
        val provider = MutableMemoryProvider(listOf(whatsapp))
        val registry = ProviderAppRegistry(provider)

        assertNull(registry.resolve("new name"))
        provider.addOrUpdate(whatsapp.copy(aliases = whatsapp.aliases + "new name"))

        assertEquals(whatsapp.copy(aliases = whatsapp.aliases + "new name"), registry.resolve("new name"))
    }

    private class MutableMemoryProvider(initial: List<AppDefinition>) : MutableAppDefinitionProvider {
        private val definitions = initial.toMutableList()

        override fun provide(): List<AppDefinition> = definitions.toList()

        override fun addOrUpdate(app: AppDefinition) {
            val index = definitions.indexOfFirst { it.id == app.id }
            if (index == -1) definitions += app
            else definitions[index] = definitions[index].copy(aliases = definitions[index].aliases + app.aliases)
        }

        override fun addAlias(appId: String, alias: String): AppDefinition? {
            val index = definitions.indexOfFirst { it.id == appId }
            if (index == -1) return null
            definitions[index] = definitions[index].copy(aliases = definitions[index].aliases + alias)
            return definitions[index]
        }
    }
}
