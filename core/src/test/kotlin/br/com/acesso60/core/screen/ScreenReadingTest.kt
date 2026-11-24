package br.com.acesso60.core.screen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Leitura de tela com filtro de privacidade")
class ScreenReadingTest {

    private class Node(
        override val text: String? = null,
        override val contentDescription: String? = null,
        override val viewId: String? = null,
        override val isPassword: Boolean = false,
        override val isVisible: Boolean = true,
        private val kids: List<Node> = emptyList(),
    ) : ScreenNode {
        override val children: Sequence<ScreenNode> get() = kids.asSequence()
    }

    private val reader = ScreenReader()

    @Test
    fun `le textos visiveis do whatsapp em ordem e ignora campo de senha e invisiveis`() {
        val root = Node(
            kids = listOf(
                Node(text = "Maria"),
                Node(text = "Chego em 10 minutos"),
                Node(text = "segredo", isPassword = true),
                Node(text = "oculto", isVisible = false),
                Node(contentDescription = "Botão enviar"),
            )
        )
        val outcome = reader.read("com.whatsapp", "Maria", root)
        assertEquals(
            ScreenReadOutcome.Text(listOf("Maria", "Chego em 10 minutos", "Botão enviar")),
            outcome,
        )
    }

    @Test
    fun `app bancario e bloqueado sem ler nenhum texto`() {
        var touched = false
        val root = object : ScreenNode {
            override val text: String? get() { touched = true; return "saldo 1000" }
            override val contentDescription: String? = null
            override val viewId: String? = null
            override val isPassword = false
            override val isVisible = true
            override val children: Sequence<ScreenNode> = emptySequence()
        }
        val outcome = reader.read("com.nu.production", null, root)
        assertTrue(outcome is ScreenReadOutcome.Blocked)
        assertFalse(touched)
    }

    @Test
    fun `sem janela ativa a leitura e indisponivel`() {
        assertEquals(ScreenReadOutcome.Unavailable, reader.read("com.whatsapp", null, null))
    }

    @Test
    fun `limita caracteres e remove linhas repetidas consecutivas`() {
        val root = Node(kids = listOf(Node(text = "a"), Node(text = "a"), Node(text = "b".repeat(50))))
        val outcome = ScreenReader(ScreenTextCollector(maxChars = 10)).read("com.whatsapp", null, root)
        val lines = (outcome as ScreenReadOutcome.Text).lines
        assertEquals("a", lines.first())
        assertEquals(10, lines.sumOf { it.length })
    }
}
