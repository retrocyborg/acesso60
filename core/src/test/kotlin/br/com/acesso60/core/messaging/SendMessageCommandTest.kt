package br.com.acesso60.core.messaging

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RiskAssessment
import br.com.acesso60.core.model.ValidationResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.validator.CommandValidator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command.SendMessage: validação e risco")
class SendMessageCommandTest {

    @Test
    fun `envio de mensagem sempre exige confirmacao e descreve o conteudo`() {
        val risk = RiskPolicy().evaluate(Command.SendMessage("João", "whatsapp", "Chego em 10 minutos"))
        assertTrue(risk is RiskAssessment.RequiresConfirmation)
        val message = (risk as RiskAssessment.RequiresConfirmation).message
        assertTrue(message.contains("Chego em 10 minutos"))
        assertTrue(message.contains("João"))
        assertTrue(message.contains("WhatsApp"))
    }

    @Test
    fun `validador rejeita campos vazios e app fora da allowlist`() {
        val validator = CommandValidator()
        assertTrue(validator.validate(Command.SendMessage("", "whatsapp", "oi")) is ValidationResult.Invalid)
        assertTrue(validator.validate(Command.SendMessage("João", "whatsapp", " ")) is ValidationResult.Invalid)
        assertTrue(validator.validate(Command.SendMessage("João", "youtube", "oi")) is ValidationResult.Invalid)
        assertTrue(validator.validate(Command.SendMessage("João", "telegram", "oi")) is ValidationResult.Valid)
    }
}
