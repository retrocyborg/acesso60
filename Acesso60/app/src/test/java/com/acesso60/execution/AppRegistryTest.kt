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
    fun `registry reflects provider changes dynamically at runtime without restart`() {
        val mutableProvider = object : MutableAppDefinitionProvider {
            private val defs = mutableListOf(whatsapp)
            override fun provide(): List<AppDefinition> = defs.toList()
            override suspend fun addOrUpdate(app: AppDefinition) {
                defs.removeIf { it.id == app.id }
                defs.add(app)
            }
        }
        val registry = StaticAppRegistry(mutableProvider)
        assertEquals(whatsapp, registry.resolve("zap"))
        assertNull(registry.resolve("novozap"))

        // Simula adição de alias em tempo de execução (como feito por voz)
        kotlinx.coroutines.runBlocking {
            mutableProvider.addOrUpdate(whatsapp.copy(aliases = whatsapp.aliases + "novozap"))
        }

        // Deve resolver imediatamente sem precisar recriar o registry ou reiniciar o app
        val resolved = registry.resolve("novozap")
        org.junit.Assert.assertNotNull(resolved)
        assertEquals("whatsapp", resolved?.id)
    }
}
