package com.acesso60.execution

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Testes unitários para [StaticAppRegistry] usando um mock de [AppDefinitionProvider].
 */
class StaticAppRegistryTest {

    private val whatsapp = AppDefinition(
        id = "WhatsApp",
        packageName = "com.whatsapp",
        aliases = setOf("whatsapp", "zap")
    )

    private val telegram = AppDefinition(
        id = "Telegram",
        packageName = "org.telegram.messenger",
        aliases = setOf("telegram")
    )

    @Test
    @DisplayName("resolve devolve definição existente para alias conhecido")
    fun resolveExistingAlias() = runBlocking {
        val provider = mockk<AppDefinitionProvider> {
            every { provide() } returns listOf(whatsapp, telegram)
        }
        val registry = StaticAppRegistry(provider)
        val result = registry.resolve("zap")
        assertNotNull(result)
        assertEquals("WhatsApp", result?.id)
    }

    @Test
    @DisplayName("resolve retorna null para alias desconhecido")
    fun resolveUnknownAliasReturnsNull() = runBlocking {
        val provider = mockk<AppDefinitionProvider> {
            every { provide() } returns listOf(whatsapp)
        }
        val registry = StaticAppRegistry(provider)
        val result = registry.resolve("unknown")
        assertNull(result)
    }

    @Test
    @DisplayName("registry reflete alterações do provider em tempo de execução")
    fun registryReflectsProviderChanges() = runBlocking {
        // Provider com estado mutável simples
        val mutableProvider = object : MutableAppDefinitionProvider {
            private var defs = mutableListOf(whatsapp)
            override fun provide(): List<AppDefinition> = defs.toList()
            override suspend fun addOrUpdate(app: AppDefinition) {
                defs.removeIf { it.id == app.id }
                defs.add(app)
            }
        }
        val registry = StaticAppRegistry(mutableProvider)
        // Verifica alias original
        assertNotNull(registry.resolve("zap"))
        // Atualiza provider com novo alias
        mutableProvider.addOrUpdate(
            whatsapp.copy(aliases = whatsapp.aliases + "whatsapp")
        )
        // Alias adicionado deve ser resolvido
        assertNotNull(registry.resolve("whatsapp"))
    }
}
