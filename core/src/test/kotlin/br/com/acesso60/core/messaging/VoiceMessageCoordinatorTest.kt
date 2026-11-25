package br.com.acesso60.core.messaging

import br.com.acesso60.core.model.Command
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Fluxo de mensagem por voz")
class VoiceMessageCoordinatorTest {

    private val coordinator = VoiceMessageCoordinator()

    @Test
    fun `rejeita apps que nao sao de mensagem e avanca so com app valido`() {
        val turn1 = coordinator.handleUtterance(MessageFlowStep.Idle, "mandar mensagem para João")
        assertTrue(turn1.nextStep is MessageFlowStep.AwaitingMessagingApp)

        val invalid = coordinator.handleUtterance(turn1.nextStep, "YouTube")
        assertTrue(invalid.nextStep is MessageFlowStep.AwaitingMessagingApp)
        assertFalse(invalid.dispatchReady)

        val valid = coordinator.handleUtterance(turn1.nextStep, "WhatsApp")
        assertTrue(valid.nextStep is MessageFlowStep.AwaitingMessageText)

        val final = coordinator.handleUtterance(valid.nextStep, "Chego em 10 minutos")
        assertTrue(final.dispatchReady)
        val ready = final.nextStep as MessageFlowStep.ReadyToDispatch
        assertEquals("João", ready.recipient)
        assertEquals("com.whatsapp", ready.app.packageName)
        assertEquals("Chego em 10 minutos", ready.messageText)
        assertEquals(Command.SendMessage("João", "whatsapp", "Chego em 10 minutos"), ready.toCommand())
    }

    @Test
    fun `artigo antes do nome e removido`() {
        assertEquals("Maria", coordinator.parseInitialCommand("enviar mensagem para a Maria"))
        assertEquals("João", coordinator.parseInitialCommand("mandar uma mensagem pra o João"))
        assertEquals("minha mãe", coordinator.parseInitialCommand("mandar mensagem para minha mãe"))
        assertNull(coordinator.parseInitialCommand("mandar mensagem"))
    }

    @Test
    fun `whatsapp business nao e confundido com whatsapp`() {
        assertEquals("whatsapp_business", MessagingAppCatalog.resolveMessagingApp("WhatsApp Business")?.id)
        assertEquals("whatsapp", MessagingAppCatalog.resolveMessagingApp("pelo zap")?.id)
        assertNull(MessagingAppCatalog.resolveMessagingApp("youtube"))
        assertNull(MessagingAppCatalog.resolveMessagingApp("  "))
    }

    @Test
    fun `alias curto nao casa dentro de outra palavra`() {
        assertNull(MessagingAppCatalog.resolveMessagingApp("televisão"))
    }

    @Test
    fun `cancelar em qualquer etapa volta ao inicio`() {
        val step = MessageFlowStep.AwaitingMessagingApp("João")
        val result = coordinator.handleUtterance(step, "Cancelar")
        assertEquals(MessageFlowStep.Idle, result.nextStep)
        assertFalse(result.dispatchReady)
    }

    @Test
    fun `texto em branco pede a mensagem de novo`() {
        val step = MessageFlowStep.AwaitingMessageText("João", MessagingAppCatalog.findById("telegram")!!)
        val result = coordinator.handleUtterance(step, "   ")
        assertEquals(step, result.nextStep)
        assertFalse(result.dispatchReady)
    }

    @Test
    fun `frase fora do fluxo em Idle nao inicia dialogo`() {
        val result = coordinator.handleUtterance(MessageFlowStep.Idle, "voltar")
        assertEquals(MessageFlowStep.Idle, result.nextStep)
    }

    @Test
    fun `falar apenas enviar mensagem pergunta o destinatario e avanca`() {
        assertTrue(coordinator.isInitialCommand("enviar mensagem"))
        assertTrue(coordinator.isSendWithoutRecipient("enviar mensagem"))
        assertTrue(coordinator.isSendWithoutRecipient("mandar uma mensagem"))

        val turn1 = coordinator.handleUtterance(MessageFlowStep.Idle, "enviar mensagem")
        assertEquals(MessageFlowStep.AwaitingRecipient, turn1.nextStep)
        assertEquals("Para quem você quer enviar a mensagem?", turn1.promptToSpeak)

        val turn2 = coordinator.handleUtterance(turn1.nextStep, "para a Maria")
        assertTrue(turn2.nextStep is MessageFlowStep.AwaitingMessagingApp)
        assertEquals("Maria", (turn2.nextStep as MessageFlowStep.AwaitingMessagingApp).recipient)

        val turn3 = coordinator.handleUtterance(turn2.nextStep, "WhatsApp")
        assertTrue(turn3.nextStep is MessageFlowStep.AwaitingMessageText)

        val turn4 = coordinator.handleUtterance(turn3.nextStep, "Olá Maria")
        assertTrue(turn4.dispatchReady)
        val ready = turn4.nextStep as MessageFlowStep.ReadyToDispatch
        assertEquals("Maria", ready.recipient)
        assertEquals("Olá Maria", ready.messageText)
    }

    @Test
    fun `cancelar enquanto aguarda destinatario cancela fluxo`() {
        val result = coordinator.handleUtterance(MessageFlowStep.AwaitingRecipient, "cancelar")
        assertEquals(MessageFlowStep.Idle, result.nextStep)
    }

    @Test
    fun `destinatario em branco pede novamente`() {
        val result = coordinator.handleUtterance(MessageFlowStep.AwaitingRecipient, "   ")
        assertEquals(MessageFlowStep.AwaitingRecipient, result.nextStep)
        assertEquals("Não entendi o destinatário. Para quem você quer enviar a mensagem?", result.promptToSpeak)
    }

    @Test
    fun `frase unica com app e texto vai direto para o envio sem perguntas`() {
        val turn = coordinator.handleUtterance(
            MessageFlowStep.Idle,
            "mandar mensagem para a Maria pelo WhatsApp dizendo chego em 10 minutos",
        )
        assertTrue(turn.dispatchReady)
        val ready = turn.nextStep as MessageFlowStep.ReadyToDispatch
        assertEquals("Maria", ready.recipient)
        assertEquals("whatsapp", ready.app.id)
        assertEquals("chego em 10 minutos", ready.messageText)
    }

    @Test
    fun `frase com destinatario e app pula direto para o texto`() {
        val turn = coordinator.handleUtterance(MessageFlowStep.Idle, "manda mensagem pro João no Telegram")
        assertFalse(turn.dispatchReady)
        val step = turn.nextStep as MessageFlowStep.AwaitingMessageText
        assertEquals("João", step.recipient)
        assertEquals("telegram", step.app.id)
    }

    @Test
    fun `texto sem app na mesma frase nao e usado e o app e perguntado`() {
        val turn = coordinator.handleUtterance(MessageFlowStep.Idle, "enviar mensagem para Maria dizendo oi")
        val step = turn.nextStep as MessageFlowStep.AwaitingMessagingApp
        assertEquals("Maria", step.recipient)
    }

    @Test
    fun `pelo app so separa do nome quando resolve para app de mensagens`() {
        assertEquals("Maria na padaria", coordinator.parseInitialCommand("mandar mensagem para Maria na padaria"))
        assertEquals("Maria na padaria", coordinator.parseRequest("mandar mensagem para Maria na padaria pelo zap")?.recipient)
        assertEquals("whatsapp", coordinator.parseRequest("mandar mensagem para Maria na padaria pelo zap")?.app?.id)
    }

    @Test
    fun `imperativo manda e mande tambem iniciam o fluxo`() {
        assertEquals("Maria", coordinator.parseInitialCommand("manda uma mensagem pra Maria"))
        assertEquals("Maria", coordinator.parseInitialCommand("mande mensagem para a Maria"))
        assertTrue(coordinator.isSendWithoutRecipient("manda mensagem"))
    }

    @Test
    fun `ultimo passo avisa que esta enviando e nao pede confirmacao`() {
        val step = MessageFlowStep.AwaitingMessageText("João", MessagingAppCatalog.findById("whatsapp")!!)
        val result = coordinator.handleUtterance(step, "Oi")
        assertTrue(result.dispatchReady)
        assertEquals("Enviando mensagem para João no WhatsApp.", result.promptToSpeak)
    }
}
