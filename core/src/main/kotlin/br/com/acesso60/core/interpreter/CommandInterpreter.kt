package br.com.acesso60.core.interpreter

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.InterpretationResult

/**
 * Interpretador determinístico de linguagem natural para comandos tipados.
 * Normaliza caixa, acentuação e espaços em branco sem introduzir dependências externas.
 *
 * Suporta o catálogo modular através de [CommandRegistry] e o comando parametrizado
 * "abrir <app>" com as variações naturais da fala ("abre o youtube", "iniciar aplicativo whatsapp").
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

    private fun interpretOpenApp(normalized: String, rawText: String): InterpretationResult {
        val words = normalized.split(WHITESPACE).filter { it.isNotEmpty() }
        if (words.size < 2 || words.first() !in OPEN_VERBS) {
            return InterpretationResult.Unknown(rawText)
        }
        var appWords = words.drop(1).dropWhile { it in LEADING_FILLERS }
        if (appWords.size >= 2 && appWords.takeLast(2) == POLITE_SUFFIX) {
            appWords = appWords.dropLast(2)
        }
        val appName = appWords.joinToString(" ")
        return if (appName.isBlank()) {
            InterpretationResult.Unknown(rawText)
        } else {
            InterpretationResult.Success(Command.OpenApp(appName))
        }
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        val OPEN_VERBS = setOf("abrir", "abre", "abra", "iniciar", "inicia", "inicie", "executar", "executa", "execute")
        val LEADING_FILLERS = setOf("o", "a", "os", "as", "um", "uma", "aplicativo", "app", "aplicacao", "programa", "do", "da")
        val POLITE_SUFFIX = listOf("por", "favor")
    }
}
