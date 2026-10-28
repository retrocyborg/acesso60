package br.com.acesso60.core.scripting

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.validator.CommandValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class ScriptEngineTest {

    private lateinit var fakeExecutor: FakeCommandExecutor
    private lateinit var router: CommandRouter
    private lateinit var engine: ScriptEngine
    private val recordedSleeps = mutableListOf<Long>()

    class FakeCommandExecutor : CommandExecutor {
        val executedCommands = mutableListOf<Command>()
        var nextResult: ExecutionResult? = null

        override fun execute(command: Command): ExecutionResult {
            executedCommands.add(command)
            return nextResult ?: ExecutionResult.Success("OK: $command")
        }
    }

    @BeforeEach
    fun setUp() {
        fakeExecutor = FakeCommandExecutor()
        router = CommandRouter(CommandValidator(), RiskPolicy(), fakeExecutor)
        recordedSleeps.clear()
        engine = ScriptEngine(
            validator = ScriptValidator(),
            riskEvaluator = ScriptRiskEvaluator(),
            router = router,
            sleeper = { recordedSleeps.add(it) }
        )
    }

    @Test
    fun `deve executar com sucesso rotina com passos de comando e pausa sem bloqueio de thread real`() {
        val script = ScriptDefinition(
            id = "rotina-sucesso",
            name = "Voltar e Home",
            aliases = listOf("macro 1"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Delay(300),
                ScriptStep.Execute(Command.Home)
            )
        )

        val progressNotifications = mutableListOf<Int>()
        val result = engine.run(script) { stepIndex, _, _ ->
            progressNotifications.add(stepIndex)
        }

        assertIs<ExecutionResult.Success>(result)
        assertEquals(2, fakeExecutor.executedCommands.size)
        assertEquals(Command.Back, fakeExecutor.executedCommands[0])
        assertEquals(Command.Home, fakeExecutor.executedCommands[1])
        assertEquals(listOf(300L), recordedSleeps)
        assertEquals(listOf(0, 1, 2), progressNotifications)
    }

    @Test
    fun `deve recusar execucao de rotina contendo comando sensivel se nao houver confirmacao`() {
        val scriptSensivel = ScriptDefinition(
            id = "rotina-sensivel",
            name = "Exclusão segura",
            aliases = listOf("limpar"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Execute(Command.SensitiveAction("apagar dados"))
            )
        )

        val result = engine.run(scriptSensivel, isConfirmed = false)

        assertEquals(ExecutionResult.Denied, result)
        assertEquals(0, fakeExecutor.executedCommands.size, "Nenhum comando deve ser executado se o script for negado")
    }

    @Test
    fun `deve autorizar e executar rotina sensivel quando usuario confirmar previamente`() {
        val scriptSensivel = ScriptDefinition(
            id = "rotina-sensivel",
            name = "Exclusão segura",
            aliases = listOf("limpar"),
            steps = listOf(
                ScriptStep.Execute(Command.SensitiveAction("apagar dados"))
            )
        )

        val result = engine.run(scriptSensivel, isConfirmed = true)

        assertIs<ExecutionResult.Success>(result)
        assertEquals(1, fakeExecutor.executedCommands.size)
    }

    @Test
    fun `deve interromper a rotina imediatamente caso um comando intermediario falhe`() {
        val script = ScriptDefinition(
            id = "rotina-falha",
            name = "Falha no meio",
            aliases = listOf("falhar"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Execute(Command.Home)
            )
        )

        fakeExecutor.nextResult = ExecutionResult.Unavailable

        val result = engine.run(script)

        assertEquals(ExecutionResult.Unavailable, result)
        assertEquals(1, fakeExecutor.executedCommands.size, "O segundo comando não deve ser executado após falha do primeiro")
    }
}
