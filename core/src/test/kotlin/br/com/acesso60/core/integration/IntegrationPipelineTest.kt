package br.com.acesso60.core.integration

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.model.InterpretationResult
import br.com.acesso60.core.model.ValidationResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.validator.CommandValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("IT-01: Teste de Integração do Pipeline Completo com Fake Executor")
class IntegrationPipelineTest {

    private lateinit var interpreter: CommandInterpreter
    private lateinit var validator: CommandValidator
    private lateinit var riskPolicy: RiskPolicy
    private lateinit var fakeExecutor: FakeCommandExecutor
    private lateinit var router: CommandRouter

    /**
     * Dublê de teste (Fake) para simular a camada de execução Android sem SDK.
     */
    class FakeCommandExecutor : CommandExecutor {
        val executedCommands = mutableListOf<Command>()

        override fun execute(command: Command): ExecutionResult {
            executedCommands.add(command)
            return ExecutionResult.Success("Comando executado com sucesso: $command")
        }
    }

    @BeforeEach
    fun setUp() {
        interpreter = CommandInterpreter()
        validator = CommandValidator()
        riskPolicy = RiskPolicy()
        fakeExecutor = FakeCommandExecutor()
        router = CommandRouter(validator, riskPolicy, fakeExecutor)
    }

    @Test
    @DisplayName("IT-01a: Fluxo autorizado completo para comando simples ('voltar')")
    fun testCompleteAuthorizedFlowForSimpleCommand() {
        val input = "voltar"

        // 1. Interpretação
        val interpretation = interpreter.interpret(input)
        assertTrue(interpretation is InterpretationResult.Success)
        val command = (interpretation as InterpretationResult.Success).command

        // 2. Roteamento e Execução
        val executionResult = router.route(command)

        assertTrue(executionResult is ExecutionResult.Success)
        assertEquals(1, fakeExecutor.executedCommands.size)
        assertEquals(Command.Back, fakeExecutor.executedCommands.first())
    }

    @Test
    @DisplayName("IT-01b: Entrada desconhecida é contida na interpretação e não chega ao executor")
    fun testUnknownInputDoesNotTriggerExecution() {
        val input = "ação desconhecida inexistente"

        val interpretation = interpreter.interpret(input)
        assertTrue(interpretation is InterpretationResult.Unknown)

        // Não há comando a despachar para o router
        assertEquals(0, fakeExecutor.executedCommands.size)
    }

    @Test
    @DisplayName("IT-01c: Ação sensível sem confirmação explícita é negada e NÃO é executada")
    fun testSensitiveActionWithoutConfirmationIsDenied() {
        val sensitiveCommand = Command.SensitiveAction("enviar mensagem")

        // Roteamento sem flag de confirmação (isConfirmed = false por padrão)
        val result = router.route(sensitiveCommand, isConfirmed = false)

        assertEquals(ExecutionResult.Denied, result)
        assertEquals(0, fakeExecutor.executedCommands.size, "O executor nunca deve receber ação sensível não autorizada")
    }

    @Test
    @DisplayName("IT-01d: Ação sensível COM confirmação explícita é autorizada e executada")
    fun testSensitiveActionWithConfirmationIsExecuted() {
        val sensitiveCommand = Command.SensitiveAction("enviar mensagem")

        // Roteamento com confirmação explícita do usuário
        val result = router.route(sensitiveCommand, isConfirmed = true)

        assertTrue(result is ExecutionResult.Success)
        assertEquals(1, fakeExecutor.executedCommands.size)
        assertEquals(sensitiveCommand, fakeExecutor.executedCommands.first())
    }
}
