package br.com.acesso60.core.messaging

import br.com.acesso60.core.interpreter.CommandRegistry

data class MessagingAppOption(
    val id: String,
    val displayName: String,
    val packageName: String,
    val aliases: Set<String>,
)

/** Allowlist de aplicativos de mensagem aceitos no fluxo por voz. */
object MessagingAppCatalog {
    const val WHATSAPP = "whatsapp"
    const val WHATSAPP_BUSINESS = "whatsapp_business"
    const val TELEGRAM = "telegram"
    const val SMS = "sms"
    const val SIGNAL = "signal"

    val allowedApps: List<MessagingAppOption> = listOf(
        MessagingAppOption(WHATSAPP, "WhatsApp", "com.whatsapp", setOf("whatsapp", "zap", "wpp", "whats")),
        MessagingAppOption(
            WHATSAPP_BUSINESS, "WhatsApp Business", "com.whatsapp.w4b",
            setOf("whatsapp business", "whats business", "zap business"),
        ),
        MessagingAppOption(TELEGRAM, "Telegram", "org.telegram.messenger", setOf("telegram", "tele")),
        MessagingAppOption(
            SMS, "Mensagens", "com.google.android.apps.messaging",
            setOf("mensagens", "sms", "mensagem de texto", "messages"),
        ),
        MessagingAppOption(SIGNAL, "Signal", "org.thoughtcrime.securesms", setOf("signal")),
    )

    fun findById(id: String): MessagingAppOption? = allowedApps.firstOrNull { it.id == id }

    /**
     * Resolve a fala para um app permitido. Correspondência exata tem prioridade; depois, o alias mais
     * longo contido na frase vence (assim "whatsapp business" não vira "whatsapp").
     */
    fun resolveMessagingApp(spokenInput: String): MessagingAppOption? {
        val normalized = CommandRegistry.normalize(spokenInput)
        if (normalized.isBlank()) return null

        allowedApps.firstOrNull { app ->
            CommandRegistry.normalize(app.displayName) == normalized ||
                app.aliases.any { CommandRegistry.normalize(it) == normalized }
        }?.let { return it }

        val words = normalized.split(Regex("\\s+"))
        return allowedApps
            .mapNotNull { app ->
                val best = app.aliases
                    .map { CommandRegistry.normalize(it) }
                    .filter { alias -> containsAsWords(words, alias.split(" ")) }
                    .maxByOrNull { it.length }
                best?.let { app to it.length }
            }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun containsAsWords(words: List<String>, alias: List<String>): Boolean {
        if (alias.isEmpty() || alias.size > words.size) return false
        return (0..words.size - alias.size).any { start ->
            alias.indices.all { words[start + it] == alias[it] }
        }
    }
}
