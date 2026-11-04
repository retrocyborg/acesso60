package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.Command

/**
 * Passo atômico de uma rotina ou script declarativo.
 */
sealed interface ScriptStep {
    /**
     * Executa um comando tipado do domínio Acesso60.
     */
    data class Execute(val command: Command) : ScriptStep

    /**
     * Pausa cooperativa em milissegundos para permitir estabilização da interface.
     */
    data class Delay(val milliseconds: Long) : ScriptStep

    /**
     * Aguarda por uma condição nominal antes de prosseguir.
     */
    data class WaitForState(val condition: String, val timeoutMs: Long = 5000L) : ScriptStep
}

/**
 * Definição tipada de uma rotina ou script de comandos externos.
 * Totalmente desacoplada de código executável arbitrário e de APIs de sistema operacional.
 */
data class ScriptDefinition(
    val id: String,
    val name: String,
    val aliases: List<String>,
    val steps: List<ScriptStep>
)
