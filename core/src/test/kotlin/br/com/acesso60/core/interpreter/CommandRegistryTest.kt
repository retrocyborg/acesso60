package br.com.acesso60.core.interpreter

import br.com.acesso60.core.model.Command
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CommandRegistryTest {

    @Test
    fun resolvesDefaultCommandsWithDifferentAliasesAndAccents() {
        val registry = CommandRegistry()

        assertEquals(Command.Back, registry.resolve("voltar"))
        assertEquals(Command.Back, registry.resolve("retornar"))
        assertEquals(Command.Home, registry.resolve("inicio"))
        assertEquals(Command.Home, registry.resolve("tela inicial"))
        assertEquals(Command.VolumeUp, registry.resolve("aumentar volume"))
        assertEquals(Command.VolumeDown, registry.resolve("diminuir volume"))
        assertEquals(Command.ReadScreen, registry.resolve("ler a tela"))
    }

    @Test
    fun allowsRegisteringNewCustomCommandWithoutModifyingSealedHierarchy() {
        val registry = CommandRegistry()
        val customCommand = Command.SensitiveAction("apagar dados")
        registry.register(CommandDefinition(customCommand, setOf("apagar dados", "limpar tudo")))

        assertEquals(customCommand, registry.resolve("apagar dados"))
        assertEquals(customCommand, registry.resolve("limpar tudo"))
    }

    @Test
    fun rejectsConflictingAliasAcrossCommands() {
        val registry = CommandRegistry()
        val conflicting = CommandDefinition(Command.Home, setOf("voltar"))

        assertThrows(IllegalArgumentException::class.java) {
            registry.register(conflicting)
        }
    }

    @Test
    fun returnsNullForUnknownCommand() {
        val registry = CommandRegistry()
        assertNull(registry.resolve("comando inexistente"))
    }
}
