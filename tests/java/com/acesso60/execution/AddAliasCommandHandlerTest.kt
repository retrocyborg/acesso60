package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Testes para o [AddAliasCommandHandler].
 */
class AddAliasCommandHandlerTest {

    private val whatsapp = AppDefinition(
        id = "WhatsApp",
        packageName = "com.whatsapp",
        aliases = setOf("whatsapp", "zap")
    )

    @Test
    @DisplayName("handle adiciona novo alias quando app existe")
    fun handleAddsAliasWhenAppExists() = runBlocking {
        val provider = mockk<MutableAppDefinitionProvider>(relaxed = true) {
            every { provide() } returns listOf(whatsapp)
        }
        val handler = AddAliasCommandHandler(provider)
        val command = Command.AddAlias(appId = "WhatsApp", newAlias = "wpp")
        val result = handler.handle(command)
        assertTrue(result is ExecutionResult.Success)
        verify { provider.addOrUpdate(match { it.id == "WhatsApp" && it.aliases.contains("wpp") }) }
    }

    @Test
    @DisplayName("handle falha quando app não encontrado")
    fun handleFailsWhenAppNotFound() = runBlocking {
        val provider = mockk<MutableAppDefinitionProvider>(relaxed = true) {
            every { provide() } returns emptyList()
        }
        val handler = AddAliasCommandHandler(provider)
        val command = Command.AddAlias(appId = "NonExisting", newAlias = "alias")
        val result = handler.handle(command)
        assertTrue(result is ExecutionResult.Failure)
        verify(exactly = 0) { provider.addOrUpdate(any()) }
    }
}
