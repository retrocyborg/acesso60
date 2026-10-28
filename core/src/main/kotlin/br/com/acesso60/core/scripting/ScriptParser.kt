package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.Command

/**
 * Resultado do parsing de uma definição de script declarativo.
 */
sealed interface ScriptParseResult {
    data class Success(val script: ScriptDefinition) : ScriptParseResult
    data class Error(val message: String) : ScriptParseResult
}

/**
 * Parser determinístico para especificações declarativas de rotinas.
 * Permite carregar scripts a partir de formato estruturado de texto chave-valor:
 *
 * ```yaml
 * id: rotina_navegacao
 * name: Voltar e Home
 * aliases: reiniciar tela, voltar tudo
 * steps:
 *   - execute: voltar
 *   - delay: 1000
 *   - execute: inicio
 * ```
 */
class ScriptParser {

    /**
     * Realiza o parsing de uma string declarativa, gerando um [ScriptDefinition].
     */
    fun parse(content: String): ScriptParseResult {
        if (content.isBlank()) {
            return ScriptParseResult.Error("Conteúdo do script está vazio.")
        }

        var id = ""
        var name = ""
        val aliases = mutableListOf<String>()
        val steps = mutableListOf<ScriptStep>()

        var parsingSteps = false

        for (rawLine in content.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) {
                continue
            }

            if (line.startsWith("id:", ignoreCase = true)) {
                id = line.substringAfter(":").trim()
            } else if (line.startsWith("name:", ignoreCase = true)) {
                name = line.substringAfter(":").trim()
            } else if (line.startsWith("aliases:", ignoreCase = true)) {
                val aliasList = line.substringAfter(":").split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                aliases.addAll(aliasList)
            } else if (line.equals("steps:", ignoreCase = true)) {
                parsingSteps = true
            } else if (parsingSteps && line.startsWith("-")) {
                val stepContent = line.removePrefix("-").trim()
                val parsedStep = parseStep(stepContent) ?: return ScriptParseResult.Error(
                    "Não foi possível interpretar o passo: '$stepContent'"
                )
                steps.add(parsedStep)
            }
        }

        if (id.isEmpty()) {
            return ScriptParseResult.Error("Propriedade obrigatória 'id' ausente.")
        }
        if (name.isEmpty()) {
            return ScriptParseResult.Error("Propriedade obrigatória 'name' ausente.")
        }

        return ScriptParseResult.Success(
            ScriptDefinition(
                id = id,
                name = name,
                aliases = aliases,
                steps = steps
            )
        )
    }

    private fun parseStep(stepText: String): ScriptStep? {
        val key = stepText.substringAfter("").substringBefore(":").trim().lowercase()
        val value = stepText.substringAfter(":").trim()

        return when (key) {
            "execute" -> parseCommand(value)?.let { ScriptStep.Execute(it) }
            "delay" -> value.toLongOrNull()?.let { ScriptStep.Delay(it) }
            "wait" -> ScriptStep.WaitForState(value)
            else -> null
        }
    }

    private fun parseCommand(cmdText: String): Command? {
        val normalized = cmdText.trim().lowercase()
        return when {
            normalized == "voltar" -> Command.Back
            normalized == "inicio" || normalized == "início" -> Command.Home
            normalized == "ler tela" -> Command.ReadScreen
            normalized == "aumentar volume" -> Command.VolumeUp
            normalized == "diminuir volume" -> Command.VolumeDown
            normalized.startsWith("abrir ") -> {
                val app = cmdText.substring(6).trim()
                if (app.isNotEmpty()) Command.OpenApp(app) else null
            }
            normalized.startsWith("sensivel ") || normalized.startsWith("sensível ") -> {
                val desc = cmdText.substringAfter(" ").trim()
                if (desc.isNotEmpty()) Command.SensitiveAction(desc) else null
            }
            else -> null
        }
    }
}
