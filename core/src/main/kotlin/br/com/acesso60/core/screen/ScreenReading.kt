package br.com.acesso60.core.screen

import br.com.acesso60.core.policy.ScreenPrivacyPolicy

/** Abstração mínima de um nó da árvore de acessibilidade (o núcleo não conhece APIs Android). */
interface ScreenNode {
    val text: String?
    val contentDescription: String?
    val viewId: String?
    val isPassword: Boolean
    val isVisible: Boolean

    /** Filhos em ordem de leitura; avaliados sob demanda. */
    val children: Sequence<ScreenNode>
}

/** Resultado de uma tentativa de leitura de tela. */
sealed interface ScreenReadOutcome {
    data class Text(val lines: List<String>) : ScreenReadOutcome
    data class Blocked(val reason: String) : ScreenReadOutcome
    data object Unavailable : ScreenReadOutcome
}

/**
 * Extrai o texto visível de uma árvore de nós, ignorando campos de senha e limitando
 * tamanho/profundidade para não travar a UI nem falar textos enormes.
 */
class ScreenTextCollector(
    private val maxNodes: Int = 400,
    private val maxChars: Int = 3_000,
    private val maxDepth: Int = 30,
) {
    fun collect(root: ScreenNode): List<String> {
        val lines = mutableListOf<String>()
        var visited = 0
        var chars = 0

        fun walk(node: ScreenNode, depth: Int) {
            if (depth > maxDepth || visited >= maxNodes || chars >= maxChars) return
            visited++
            if (!node.isVisible) return

            val skip = ScreenPrivacyPolicy.isPasswordNode(
                isPassword = node.isPassword,
                viewIdResourceName = node.viewId,
                contentDescription = node.contentDescription,
                text = node.text,
            )
            if (!skip) {
                val value = (node.text?.takeIf { it.isNotBlank() } ?: node.contentDescription)?.trim()
                if (!value.isNullOrEmpty() && value != lines.lastOrNull()) {
                    val room = maxChars - chars
                    lines += if (value.length > room) value.take(room) else value
                    chars += value.length
                }
            }
            for (child in node.children) walk(child, depth + 1)
        }

        walk(root, 0)
        return lines
    }
}

/** Caso de uso: aplica a política de privacidade ANTES de ler qualquer texto da tela. */
class ScreenReader(
    private val collector: ScreenTextCollector = ScreenTextCollector(),
) {
    fun read(packageName: String?, windowTitle: String?, root: ScreenNode?): ScreenReadOutcome {
        if (root == null) return ScreenReadOutcome.Unavailable
        val decision = ScreenPrivacyPolicy.evaluate(packageName, windowTitle)
        if (!decision.allowed) return ScreenReadOutcome.Blocked(decision.reason)
        return ScreenReadOutcome.Text(collector.collect(root))
    }
}
