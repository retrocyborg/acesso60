package br.com.acesso60.core.logging

/** Evento de log "seguro": nunca deve carregar conteúdo de tela, mensagem ou consulta do usuário. */
data class SafeLogEvent(
    val category: String,
    val action: String,
    val status: String,
    val metadata: Map<String, String> = emptyMap(),
)

interface SafeExecutionLogger {
    fun log(event: SafeLogEvent)
}

/**
 * Logger que usa *allowlist* de metadados: só chaves conhecidas como não sensíveis saem em claro.
 * Qualquer outra chave é descartada do valor (apenas `[REDACTED]`), sem sequer revelar o tamanho
 * do conteúdo.
 *
 * @param sink destino da linha final (padrão: saída padrão; no Android, passe `Log.i`).
 */
class RedactingConsoleLogger(
    private val sink: (String) -> Unit = ::println,
    private val safeKeys: Set<String> = DEFAULT_SAFE_KEYS,
) : SafeExecutionLogger {

    override fun log(event: SafeLogEvent) {
        val meta = event.metadata.entries
            .sortedBy { it.key }
            .joinToString(prefix = "{", postfix = "}") { (key, value) ->
                val normalized = key.lowercase()
                if (normalized in safeKeys) "$key=${value.take(MAX_VALUE)}" else "$key=[REDACTED]"
            }
        sink("Acesso60Log | category=${event.category} | action=${event.action} | status=${event.status} | meta=$meta")
    }

    companion object {
        private const val MAX_VALUE = 64
        val DEFAULT_SAFE_KEYS: Set<String> = setOf(
            "package", "reason", "command", "result", "duration_ms", "app_id", "step",
        )
    }
}
