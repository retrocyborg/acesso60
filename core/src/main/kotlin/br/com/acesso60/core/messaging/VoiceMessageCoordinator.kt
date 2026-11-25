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
        /** Converte o passo final em um comando tipado, que ainda passa por validação e política de risco. */
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
 * Não envia nada: apenas conduz o diálogo e entrega um [Command.SendMessage] ao fluxo normal do núcleo
 * (validação -> política de risco -> executor). Não há etapa extra de "confirmar": o próprio diálogo é a autorização.
 *
 * Também entende o pedido numa frase só: "mandar mensagem para João pelo WhatsApp dizendo chego em 10 minutos".
 */
class VoiceMessageCoordinator {

    private val sendCommandRegex = Regex(
        """(?i)\b(?:mandar|manda|mande|enviar|envia|envie)\s+(?:uma\s+)?mensagem\s+(?:para|ao|pra|pro|a)\s+(.+)$"""
    )
    private val sendWithoutRecipientRegex = Regex(
        """(?i)^\s*(?:quero\s+)?(?:mandar|manda|mande|enviar|envia|envie)\s+(?:uma\s+)?mensagem\s*$"""
    )
    private val textMarkerRegex = Regex(
        """(?i)\s+(?:dizendo(?:\s+que)?|falando(?:\s+que)?|escrevendo|com\s+o\s+texto|com\s+a\s+mensagem|que\s+diz)\s+(.+)$"""
    )
    private val appMarkerRegex = Regex(
        """(?i)\s+(?:pelo|pela|no|na|via|usando)\s+(?:o\s+|a\s+)?(\S+(?:\s+\S+)?)$"""
    )
    private val leadingArticle = Regex("""(?i)^(?:o|a)\s+""")
    private val leadingPrepositionAndArticle = Regex("""(?i)^(?:para|ao|pra|pro)\s+(?:o\s+|a\s+)?""")
    private val cancelWords = setOf("cancelar", "cancela", "deixa pra la", "esquece", "parar")

    /** Pedido extraído de uma única frase. `app` e `text` só vêm preenchidos quando foram ditos. */
    data class MessageRequest(
        val recipient: String,
        val app: MessagingAppOption? = null,
        val text: String? = null,
    )

    fun parseInitialCommand(utterance: String): String? = parseRequest(utterance)?.recipient

    /**
     * Extrai destinatário e, se ditos na mesma frase, aplicativo e texto.
     * O app só é separado do destinatário quando a fala resolve para um app de mensagens da allowlist;
     * o texto só é usado quando o app também veio na frase.
     */
    fun parseRequest(utterance: String): MessageRequest? {
        val match = sendCommandRegex.find(utterance.trim()) ?: return null
        var tail = match.groupValues.getOrElse(1) { "" }.trim()

        var text: String? = null
        textMarkerRegex.find(tail)?.let { marker ->
            text = marker.groupValues[1].trim().ifBlank { null }
            tail = tail.substring(0, marker.range.first).trim()
        }

        var app: MessagingAppOption? = null
        appMarkerRegex.find(tail)?.let { marker ->
            val resolved = MessagingAppCatalog.resolveMessagingApp(marker.groupValues[1])
            if (resolved != null) {
                app = resolved
                tail = tail.substring(0, marker.range.first).trim()
            }
        }

        // "para o João" / "para a Maria" perdem o artigo.
        val recipient = tail.replace(leadingArticle, "").trim()
        if (recipient.isBlank()) return null
        return MessageRequest(recipient, app, if (app != null) text else null)
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
                val request = parseRequest(clean)
                if (request != null) {
                    advanceFromRequest(request)
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
        val request = parseRequest(clean)
        if (request != null) return advanceFromRequest(request)
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

    /** Pula as perguntas cujas respostas já vieram na frase inicial. */
    private fun advanceFromRequest(request: MessageRequest): MessageTurnResult {
        val app = request.app
        val text = request.text
        return when {
            app != null && text != null -> MessageTurnResult(
                MessageFlowStep.ReadyToDispatch(request.recipient, app, text),
                "Enviando mensagem para ${request.recipient} no ${app.displayName}.",
                dispatchReady = true,
            )
            app != null -> MessageTurnResult(
                MessageFlowStep.AwaitingMessageText(request.recipient, app),
                "Certo, usando ${app.displayName}. Qual mensagem você quer enviar para ${request.recipient}?",
            )
            else -> MessageTurnResult(
                MessageFlowStep.AwaitingMessagingApp(request.recipient),
                "Em qual aplicativo de mensagens você quer enviar para ${request.recipient}? " +
                    "Diga, por exemplo, WhatsApp, Telegram ou Mensagens.",
            )
        }
    }

    private fun captureRecipient(clean: String): MessageTurnResult {
        parseRequest(clean)?.let { return advanceFromRequest(it) }
        val recipient = clean
            .replace(leadingPrepositionAndArticle, "")
            .replace(leadingArticle, "")
            .trim()
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
            "Enviando mensagem para ${step.recipient} no ${step.app.displayName}.",
            dispatchReady = true,
        )
    }
}
