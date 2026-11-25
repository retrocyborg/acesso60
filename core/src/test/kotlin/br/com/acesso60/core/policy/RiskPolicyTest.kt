package br.com.acesso60.core.policy

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RiskAssessment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UT-05: Testes Unitários da RiskPolicy")
class RiskPolicyTest {

    private val policy = RiskPolicy()

    @Test
    @DisplayName("UT-05a: Comandos de navegação simples (Back e Home) são classificados como LowRisk")
    fun testSimpleNavigationIsLowRisk() {
        assertEquals(RiskAssessment.LowRisk, policy.evaluate(Command.Back))
        assertEquals(RiskAssessment.LowRisk, policy.evaluate(Command.Home))
    }

    @Test
    @DisplayName("UT-05b: Ações sensíveis exigem confirmação explícita prévia")
    fun testSensitiveActionRequiresConfirmation() {
        val sensitiveCommand = Command.SensitiveAction("enviar mensagem")
        val assessment = policy.evaluate(sensitiveCommand)

        assertTrue(assessment is RiskAssessment.RequiresConfirmation)
        assertTrue(
            (assessment as RiskAssessment.RequiresConfirmation).message.contains("Confirmação explícita obrigatória")
        )
    }

    @Test
    @DisplayName("UT-05c: Abrir app e enviar mensagem ditada são diretos (LowRisk); ler a tela ainda exige confirmação")
    fun testOpenAppAndSendMessageAreDirect() {
        assertEquals(RiskAssessment.LowRisk, policy.evaluate(Command.OpenApp("youtube")))
        assertEquals(RiskAssessment.LowRisk, policy.evaluate(Command.SendMessage("João", "whatsapp", "oi")))
        assertTrue(policy.evaluate(Command.ReadScreen) is RiskAssessment.RequiresConfirmation)
    }
}
