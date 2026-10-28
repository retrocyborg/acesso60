package br.com.acesso60.core.interpreter

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.InterpretationResult
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Interpretador determinístico de linguagem natural para comandos tipados.
 * Normaliza caixa, acentuação e espaços em branco sem introduzir dependências externas.
 */
class CommandInterpreter {

    /**
     * Interpreta um texto bruto e retorna o comando tipado correspondente ou Unknown.
     */
    fun interpret(rawText: String?): InterpretationResult {
        if (rawText.isNullOrBlank()) {
            return InterpretationResult.Unknown(rawText ?: "")
        }

        val normalized = normalize(rawText)

        return when (normalized) {
            "voltar" -> InterpretationResult.Success(Command.Back)
            "inicio" -> InterpretationResult.Success(Command.Home)
            "ler a tela" -> InterpretationResult.Success(Command.ReadScreen)
            "aumentar volume" -> InterpretationResult.Success(Command.VolumeUp)
            "diminuir volume" -> InterpretationResult.Success(Command.VolumeDown)
            "enviar mensagem" -> InterpretationResult.Success(Command.SensitiveAction("enviar mensagem"))
            else -> interpretOpenApp(normalized, rawText)
        }
    }

    private fun interpretOpenApp(normalized: String, rawText: String): InterpretationResult =
        normalized.removePrefix("abrir ")
            .takeIf { normalized.startsWith("abrir ") && it.isNotBlank() }
            ?.let { InterpretationResult.Success(Command.OpenApp(it)) }
            ?: InterpretationResult.Unknown(rawText)

    private fun normalize(input: String): String {
        val trimmed = input.trim().lowercase()
        val nfd = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfd).replaceAll("")
    }
}
