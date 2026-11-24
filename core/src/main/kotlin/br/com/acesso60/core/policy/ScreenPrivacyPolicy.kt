package br.com.acesso60.core.policy

import br.com.acesso60.core.interpreter.CommandRegistry

/** Decisão da política de privacidade para leitura de tela. */
data class ScreenReadDecision(
    val allowed: Boolean,
    val reason: String,
)

/**
 * Política de leitura de tela e privacidade (falha segura).
 *
 * - Libera explicitamente o WhatsApp (`com.whatsapp` e `com.whatsapp.w4b`).
 * - Bloqueia bancos, carteiras, gerenciadores de senha, autenticadores e campos de senha.
 * - Pacote desconhecido é bloqueado.
 *
 * Os critérios de contexto usam *tokens* (palavras inteiras) para termos curtos como
 * "pin" e "pix", evitando falsos positivos como "Pinterest" ou "pixel".
 */
object ScreenPrivacyPolicy {

    private val whatsappPackages = setOf("com.whatsapp", "com.whatsapp.w4b")

    private val blockedPackages = setOf(
        // Bancos e carteiras
        "com.nu.production",
        "com.itau",
        "com.bradesco",
        "br.com.bb.android",
        "br.com.intermedium",
        "br.com.caixa.fgts",
        "br.com.caixa.tem",
        "com.santander.app",
        "com.btg.pactual",
        "com.picpay",
        "com.mercadopago.wallet",
        // Gerenciadores de senha e autenticadores
        "com.lastpass.lpandroid",
        "com.agilebits.onepassword",
        "com.onepassword.android",
        "com.x8bit.bitwarden",
        "com.dashlane",
        "com.google.android.apps.authenticator2",
        "com.azure.authenticator",
        "com.microsoft.authenticator",
    )

    /** Termos longos o bastante para serem procurados como trecho do pacote/título. */
    private val sensitiveSubstrings = setOf(
        "banco", "bank", "senha", "password", "cartao", "token", "autenticador",
        "authenticator", "cofre", "wallet", "fatura", "extrato", "investimento",
        "saldo", "bitwarden", "lastpass", "dashlane", "onepassword", "nubank",
    )

    /** Termos curtos: só valem como palavra inteira. */
    private val sensitiveTokens = setOf("pin", "pix", "cvv", "cvc")

    /** Telas sensíveis que podem aparecer até dentro do WhatsApp (verificação em duas etapas, pagamentos). */
    private val whatsappSensitiveTokens = setOf(
        "senha", "password", "pin", "pix", "cvv", "pagamento", "pagamentos", "payment", "payments",
        "verificacao", "verification",
    )

    private val passwordNodeHints = listOf("password", "senha", "passcode", "pin_entry", "cvv")

    fun evaluate(packageName: String?, windowTitle: String? = null): ScreenReadDecision {
        val pkg = packageName?.trim()?.lowercase().orEmpty()
        if (pkg.isBlank()) return ScreenReadDecision(false, "PACOTE_DESCONHECIDO")

        val titleTokens = tokens(windowTitle.orEmpty())

        if (pkg in whatsappPackages) {
            return if (titleTokens.any { it in whatsappSensitiveTokens }) {
                ScreenReadDecision(false, "CONTEXTO_SENSIVEL_BLOQUEADO")
            } else {
                ScreenReadDecision(true, "WHATSAPP_PERMITIDO")
            }
        }

        if (pkg in blockedPackages) return ScreenReadDecision(false, "APP_SENSIVEL_BLOQUEADO")

        val combined = CommandRegistry.normalize("$pkg ${windowTitle.orEmpty()}")
        val sensitive = sensitiveSubstrings.any { combined.contains(it) } ||
            (tokens(pkg) + titleTokens).any { it in sensitiveTokens }
        if (sensitive) return ScreenReadDecision(false, "CONTEXTO_SENSIVEL_BLOQUEADO")

        return ScreenReadDecision(true, "PERMITIDO")
    }

    /** `true` se o nó de acessibilidade deve ser ignorado na leitura (campo de senha ou semelhante). */
    fun isPasswordNode(
        isPassword: Boolean,
        viewIdResourceName: String?,
        contentDescription: String?,
        text: String?,
    ): Boolean {
        if (isPassword) return true
        val merged = "${viewIdResourceName.orEmpty()} ${contentDescription.orEmpty()} ${text.orEmpty()}".lowercase()
        return passwordNodeHints.any { merged.contains(it) }
    }

    private fun tokens(value: String): Set<String> =
        CommandRegistry.normalize(value)
            .split(Regex("[^a-z0-9]+"))
            .filter { it.isNotEmpty() }
            .toSet()
}
