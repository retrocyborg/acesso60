package com.acesso60.messaging

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.screen.ScreenReadOutcome
import com.acesso60.accessibility.AccessibilityGateway
import com.acesso60.execution.LaunchResult
import com.acesso60.execution.ReadScreenCommandHandler
import com.acesso60.execution.SendMessageCommandHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SendMessageHandlerTest {

    private val command = Command.SendMessage("João", "whatsapp", "Chego em 10 minutos")

    private class FakeLauncher(private val result: LaunchResult) : MessageLauncher {
        val launched = mutableListOf<Command.SendMessage>()
        override fun launch(command: Command.SendMessage): LaunchResult {
            launched += command
            return result
        }
    }

    @Test
    fun `abre o app e informa que o envio final e manual`() {
        val launcher = FakeLauncher(LaunchResult.Started)
        val result = SendMessageCommandHandler(launcher).handle(command)

        assertTrue(result is ExecutionResult.Success)
        assertTrue((result as ExecutionResult.Success).message.contains("toque em enviar"))
        assertEquals(listOf(command), launcher.launched)
    }

    @Test
    fun `app nao instalado vira Unavailable e falha vira Failure sem expor detalhes`() {
        assertEquals(
            ExecutionResult.Unavailable,
            SendMessageCommandHandler(FakeLauncher(LaunchResult.NotInstalled)).handle(command),
        )
        val failure = SendMessageCommandHandler(FakeLauncher(LaunchResult.Failed("detalhe interno"))).handle(command)
        assertTrue(failure is ExecutionResult.Failure)
        assertFalse((failure as ExecutionResult.Failure).error.contains("detalhe interno"))
    }

    @Test
    fun `app fora da allowlist nunca chega ao launcher`() {
        val launcher = FakeLauncher(LaunchResult.Started)
        val result = SendMessageCommandHandler(launcher).handle(Command.SendMessage("João", "youtube", "oi"))
        assertEquals(ExecutionResult.Unavailable, result)
        assertTrue(launcher.launched.isEmpty())
    }

    @Test
    fun `leitura de tela bloqueada vira Denied e texto vazio e informado`() {
        fun gateway(outcome: ScreenReadOutcome) = object : AccessibilityGateway {
            override fun performBack() = false
            override fun performHome() = false
            override fun readScreen() = outcome
        }
        assertEquals(
            ExecutionResult.Denied,
            ReadScreenCommandHandler(gateway(ScreenReadOutcome.Blocked("APP_SENSIVEL_BLOQUEADO"))).handle(Command.ReadScreen),
        )
        assertEquals(
            ExecutionResult.Unavailable,
            ReadScreenCommandHandler(gateway(ScreenReadOutcome.Unavailable)).handle(Command.ReadScreen),
        )
        val text = ReadScreenCommandHandler(gateway(ScreenReadOutcome.Text(listOf("Maria", "Oi")))).handle(Command.ReadScreen)
        assertEquals(ExecutionResult.Success("Maria. Oi"), text)
    }
}
