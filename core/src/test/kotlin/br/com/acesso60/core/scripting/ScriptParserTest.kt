package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.Command
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ScriptParserTest {

    private val parser = ScriptParser()

    @Test
    fun `deve realizar o parsing com sucesso de manifesto estruturado valido`() {
        val yaml = """
            # Rotina de teste
            id: rotina-reset
            name: Reiniciar Interface
            aliases: reiniciar, resetar tela
            steps:
              - execute: voltar
              - delay: 500
              - execute: inicio
              - wait: home_visible
        """.trimIndent()

        val result = parser.parse(yaml)
        assertIs<ScriptParseResult.Success>(result)

        val script = result.script
        assertEquals("rotina-reset", script.id)
        assertEquals("Reiniciar Interface", script.name)
        assertEquals(listOf("reiniciar", "resetar tela"), script.aliases)
        assertEquals(4, script.steps.size)
        assertEquals(ScriptStep.Execute(Command.Back), script.steps[0])
        assertEquals(ScriptStep.Delay(500), script.steps[1])
        assertEquals(ScriptStep.Execute(Command.Home), script.steps[2])
        assertEquals(ScriptStep.WaitForState("home_visible"), script.steps[3])
    }

    @Test
    fun `deve retornar erro quando conteudo for vazio ou faltar id`() {
        assertIs<ScriptParseResult.Error>(parser.parse("   "))

        val semId = """
            name: Sem ID
            aliases: teste
            steps:
              - execute: voltar
        """.trimIndent()
        assertIs<ScriptParseResult.Error>(parser.parse(semId))
    }

    @Test
    fun `deve retornar erro quando um passo for desconhecido`() {
        val passoDesconhecido = """
            id: rotina-errada
            name: Passo Errado
            aliases: teste
            steps:
              - execute: comando_inexistente_xyz
        """.trimIndent()

        val result = parser.parse(passoDesconhecido)
        assertIs<ScriptParseResult.Error>(result)
    }
}
