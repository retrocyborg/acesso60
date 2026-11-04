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
}
