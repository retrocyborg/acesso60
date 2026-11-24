package br.com.acesso60.core.messaging

import br.com.acesso60.core.interpreter.CommandRegistry
import br.com.acesso60.core.model.Command

sealed interface MessageFlowStep {
    data object Idle : MessageFlowStep
    data object AwaitingRecipient : MessageFlowStep
    data class AwaitingMessagingApp(val recipient: String) : MessageFlowStep
    data class AwaitingMessageText(val recipient: String, val app: MessagingAppOption) : MessageFlowStep
    data class ReadyToDispatch(
        val recipient: String,
        val app: MessagingAppOption,
        val messageText: String,
    ) : MessageFlowStep {
        /** Converte o passo final em um comando tipado, que ainda passa por validação, risco e confirmação. */
        fun toCommand(): Command.SendMessage = Command.SendMessage(recipient, app.id, messageText)
    }
}

data class MessageTurnResult(
    val nextStep: MessageFlowStep,
    val promptToSpeak: String,
    val dispatchReady: Boolean = false,
)

/**
 * Máquina de estados do diálogo "mandar mensagem para <pessoa>" (puro Kotlin, sem Android).
 * Não envia nada: apenas conduz o diálogo e entrega um [Command.SendMessage] ao fluxo normal do núcleo.
 */
class VoiceMessageCoordinator {

    private val sendCommandRegex = Regex(
        """(?i)\b(?:mandar|enviar)\s+(?:uma\s+)?mensagem\s+(?:para|ao|pra|pro)\s+(.+)$"""
    )
    private val sendWithoutRecipientRegex = Regex(
        """(?i)^\s*(?:quero\s+)?(?:mandar|enviar)\s+(?:uma\s+)?mensagem\s*$"""
    )
    private val leadingArticle = Regex("""(?i)^(?:o|a)\s+""")
    private val leadingPrepositionAndArticle = Regex("""(?i)^(?:para|ao|pra|pro)\s+(?:o\s+|a\s+)?""")
    private val cancelWords = setOf("cancelar", "cancela", "deixa pra la", "esquece", "parar")

    fun parseInitialCommand(utterance: String): String? {
        val match = sendCommandRegex.find(utterance.trim()) ?: return null
        val raw = match.groupValues.getOrElse(1) { "" }.trim()
        // "pro João" já carrega o artigo; "para o João" / "para a Maria" perdem o artigo.
        val recipient = raw.replace(leadingArticle, "").trim()
        return recipient.ifBlank { null }
    }

    fun isSendWithoutRecipient(utterance: String): Boolean {
        val norm = CommandRegistry.normalize(utterance)
        return sendWithoutRecipientRegex.matches(norm)
    }

    fun isInitialCommand(utterance: String): Boolean =
        parseInitialCommand(utterance) != null || isSendWithoutRecipient(utterance)

    fun handleUtterance(currentStep: MessageFlowStep, utterance: String): MessageTurnResult {
        val clean = utterance.trim()

        if (currentStep !is MessageFlowStep.Idle && CommandRegistry.normalize(clean) in cancelWords) {
            return MessageTurnResult(MessageFlowStep.Idle, "Tudo bem, cancelei o envio da mensagem.")
        }

        return when (currentStep) {
            is MessageFlowStep.Idle -> startFlow(clean)
            is MessageFlowStep.AwaitingRecipient -> captureRecipient(clean)
            is MessageFlowStep.AwaitingMessagingApp -> chooseApp(currentStep, clean)
            is MessageFlowStep.AwaitingMessageText -> captureText(currentStep, clean)
            is MessageFlowStep.ReadyToDispatch -> {
                val recipient = parseInitialCommand(clean)
                if (recipient != null) {
                    MessageTurnResult(
                        MessageFlowStep.AwaitingMessagingApp(recipient),
                        "Em qual aplicativo de mensagens você quer enviar para $recipient?",
                    )
                } else if (isSendWithoutRecipient(clean)) {
                    MessageTurnResult(
                        MessageFlowStep.AwaitingRecipient,
                        "Para quem você quer enviar a mensagem?",
                    )
                } else {
                    MessageTurnResult(MessageFlowStep.Idle, "Pronto. Como posso ajudar agora?")
                }
            }
        }
    }

    private fun startFlow(clean: String): MessageTurnResult {
        val recipient = parseInitialCommand(clean)
        if (recipient != null) {
            return MessageTurnResult(
                MessageFlowStep.AwaitingMessagingApp(recipient),
                "Em qual aplicativo de mensagens você quer enviar para $recipient? " +
                    "Diga, por exemplo, WhatsApp, Telegram ou Mensagens.",
            )
        }
        if (isSendWithoutRecipient(clean)) {
            return MessageTurnResult(
                MessageFlowStep.AwaitingRecipient,
                "Para quem você quer enviar a mensagem?",
            )
        }
        return MessageTurnResult(
            MessageFlowStep.Idle,
            "Não entendi o destinatário. Diga, por exemplo: mandar mensagem para Maria.",
        )
    }

    private fun captureRecipient(clean: String): MessageTurnResult {
        val fromInitial = parseInitialCommand(clean)
        val candidate = fromInitial ?: clean
            .replace(leadingPrepositionAndArticle, "")
            .replace(leadingArticle, "")
            .trim()
        val recipient = candidate.trim()
        if (recipient.isBlank()) {
            return MessageTurnResult(
                MessageFlowStep.AwaitingRecipient,
                "Não entendi o destinatário. Para quem você quer enviar a mensagem?",
            )
        }
        return MessageTurnResult(
            MessageFlowStep.AwaitingMessagingApp(recipient),
            "Em qual aplicativo de mensagens você quer enviar para $recipient? " +
                "Diga, por exemplo, WhatsApp, Telegram ou Mensagens.",
        )
    }

    private fun chooseApp(step: MessageFlowStep.AwaitingMessagingApp, clean: String): MessageTurnResult {
        val app = MessagingAppCatalog.resolveMessagingApp(clean)
            ?: return MessageTurnResult(
                step,
                "Esse não é um aplicativo de mensagens válido. " +
                    "Escolha apenas um aplicativo de mensagens, como WhatsApp, Telegram ou Mensagens.",
            )
        return MessageTurnResult(
            MessageFlowStep.AwaitingMessageText(step.recipient, app),
            "Certo, usando ${app.displayName}. Qual mensagem você quer enviar para ${step.recipient}?",
        )
    }

    private fun captureText(step: MessageFlowStep.AwaitingMessageText, clean: String): MessageTurnResult {
        if (clean.isBlank()) {
            return MessageTurnResult(step, "Não ouvi a mensagem. O que você quer dizer para ${step.recipient}?")
        }
        return MessageTurnResult(
            MessageFlowStep.ReadyToDispatch(step.recipient, step.app, clean),
            "Vou preparar a mensagem para ${step.recipient} no ${step.app.displayName}.",
            dispatchReady = true,
        )
    }
}
