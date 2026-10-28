package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.Command
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ScriptRegistryTest {

    @Test
    fun `deve registrar, listar, desregistrar e buscar por alias normalizado`() {
        val registry = ScriptRegistry()

        val script = ScriptDefinition(
            id = "script-1",
            name = "Rotina Teste",
            aliases = listOf("Ativar Rotina", "Modo Noturno"),
            steps = listOf(ScriptStep.Execute(Command.Back))
        )

        registry.register(script)
        assertEquals(1, registry.getAll().size)

        val encontrado = registry.findByAlias("  ativar rotina  ")
        assertNotNull(encontrado)
        assertEquals("script-1", encontrado?.id)

        val naoEncontrado = registry.findByAlias("comando desconhecido")
        assertNull(naoEncontrado)

        registry.unregister("script-1")
        assertEquals(0, registry.getAll().size)
        assertNull(registry.findByAlias("ativar rotina"))
    }
}
