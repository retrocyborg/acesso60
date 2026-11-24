package br.com.acesso60.core.policy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Política de privacidade de leitura de tela")
class ScreenPrivacyPolicyTest {

    @Test
    fun `whatsapp e whatsapp business sao permitidos`() {
        assertTrue(ScreenPrivacyPolicy.evaluate("com.whatsapp").allowed)
        assertTrue(ScreenPrivacyPolicy.evaluate("com.whatsapp.w4b").allowed)
        assertTrue(ScreenPrivacyPolicy.evaluate("com.whatsapp", "Maria").allowed)
    }

    @Test
    fun `bancos gerenciadores de senha e autenticadores sao bloqueados`() {
        listOf(
            "com.nu.production", "com.x8bit.bitwarden", "com.agilebits.onepassword",
            "com.azure.authenticator", "com.google.android.apps.authenticator2",
        ).forEach { pkg ->
            val decision = ScreenPrivacyPolicy.evaluate(pkg)
            assertFalse(decision.allowed, pkg)
            assertEquals("APP_SENSIVEL_BLOQUEADO", decision.reason)
        }
    }

    @Test
    fun `pacote vazio ou nulo e bloqueado`() {
        assertFalse(ScreenPrivacyPolicy.evaluate(null).allowed)
        assertFalse(ScreenPrivacyPolicy.evaluate("  ").allowed)
    }

    @Test
    fun `contexto sensivel por titulo ou pacote e bloqueado`() {
        assertFalse(ScreenPrivacyPolicy.evaluate("com.example.app", "Extrato da conta").allowed)
        assertFalse(ScreenPrivacyPolicy.evaluate("com.example.app", "Cartão de crédito").allowed)
        assertFalse(ScreenPrivacyPolicy.evaluate("com.example.app", "Digite seu PIN").allowed)
        assertFalse(ScreenPrivacyPolicy.evaluate("com.minhabank.app").allowed)
    }

    @Test
    fun `termos curtos nao geram falso positivo em palavras maiores`() {
        assertTrue(ScreenPrivacyPolicy.evaluate("com.pinterest", "Pinterest").allowed)
        assertTrue(ScreenPrivacyPolicy.evaluate("com.google.android.apps.pixel.support", "Pixel").allowed)
    }

    @Test
    fun `whatsapp com tela de senha ou pagamento e bloqueado`() {
        assertFalse(ScreenPrivacyPolicy.evaluate("com.whatsapp", "Verificação em duas etapas - PIN").allowed)
        assertFalse(ScreenPrivacyPolicy.evaluate("com.whatsapp", "Pagamentos").allowed)
    }

    @Test
    fun `campos de senha sao identificados`() {
        assertTrue(ScreenPrivacyPolicy.isPasswordNode(true, null, null, null))
        assertTrue(ScreenPrivacyPolicy.isPasswordNode(false, "com.x:id/password_input", null, null))
        assertTrue(ScreenPrivacyPolicy.isPasswordNode(false, null, "Campo de senha", null))
        assertFalse(ScreenPrivacyPolicy.isPasswordNode(false, "com.whatsapp:id/message_text", null, "Oi"))
    }
}
