package br.com.acesso60.core.interpreter

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.InterpretationResult

/**
 * Interpretador determinístico de linguagem natural para comandos tipados.
 * Normaliza caixa, acentuação e espaços em branco sem introduzir dependências externas.
 *
 * Suporta o catálogo modular através de [CommandRegistry].
 */
class CommandInterpreter(
    private val registry: CommandRegistry = CommandRegistry(),
) {

    /**
     * Interpreta um texto bruto e retorna o comando tipado correspondente ou Unknown.
     */
    fun interpret(rawText: String?): InterpretationResult {
        if (rawText.isNullOrBlank()) {
            return InterpretationResult.Unknown(rawText ?: "")
        }

        val normalized = CommandRegistry.normalize(rawText)

        // 1. Tenta resolver via catálogo modular registrado
        val resolved = registry.resolve(normalized)
        if (resolved != null) {
            return InterpretationResult.Success(resolved)
        }

        // 2. Se não estiver no catálogo direto, avalia comando parametrizado (ex: abrir <app>)
        return interpretOpenApp(normalized, rawText)
    }

    private fun interpretOpenApp(normalized: String, rawText: String): InterpretationResult =
        normalized.removePrefix("abrir ")
            .takeIf { normalized.startsWith("abrir ") && it.isNotBlank() }
            ?.let { InterpretationResult.Success(Command.OpenApp(it)) }
            ?: InterpretationResult.Unknown(rawText)
}
