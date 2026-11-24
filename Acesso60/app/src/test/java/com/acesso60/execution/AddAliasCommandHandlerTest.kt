package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddAliasCommandHandlerTest {
    @Test
    fun `new alias is visible immediately and result says persistence is pending`() {
        val initial = AppDefinition("calendar", "com.example.calendar", setOf("calendar"))
        val provider = MemoryProvider(initial)
        val registry = ProviderAppRegistry(provider)
        val handler = AddAliasCommandHandler(provider)

        val result = handler.handle(Command.AddAlias("calendar", "agenda"))

        assertEquals(initial.copy(aliases = initial.aliases + "agenda"), registry.resolve("agenda"))
        assertTrue(result is ExecutionResult.Success)
        assertTrue((result as ExecutionResult.Success).message.contains("salvamento em andamento"))
    }

    private class MemoryProvider(initial: AppDefinition) : MutableAppDefinitionProvider {
        private var definition = initial

        override fun provide(): List<AppDefinition> = listOf(definition)

        override fun addOrUpdate(app: AppDefinition) {
            definition = definition.copy(aliases = definition.aliases + app.aliases)
        }

        override fun addAlias(appId: String, alias: String): AppDefinition? {
            if (definition.id != appId) return null
            definition = definition.copy(aliases = definition.aliases + alias)
            return definition
        }
    }
}
