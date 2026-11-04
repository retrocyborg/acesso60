package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.model.RiskAssessment
import br.com.acesso60.core.router.CommandRouter
import kotlinx.coroutines.delay

/**
 * Listener opcional para notificação do avanço de passos do script.
 */
fun interface ScriptProgressListener {
    fun onStepStarted(stepIndex: Int, totalSteps: Int, step: ScriptStep)
}

/**
 * Abstração de atraso para garantir testabilidade rápida e determinística sem bloqueio real de threads em testes.
 */
fun interface Sleeper {
    suspend fun sleep(milliseconds: Long)
}

/** Sem adaptador de estado, uma espera nominal falha antes de executar o script. */
fun interface ScriptStateWaiter {
    suspend fun await(condition: String, timeoutMs: Long): Boolean
}

/**
 * Motor de orquestração sequencial de scripts.
 * Garante validação prévia, verificação de risco fail-safe e interrupção graciosa no primeiro erro.
 */
class ScriptEngine(
    private val validator: ScriptValidator = ScriptValidator(),
    private val riskEvaluator: ScriptRiskEvaluator = ScriptRiskEvaluator(),
    private val router: CommandRouter,
    private val sleeper: Sleeper = Sleeper { delay(it) },
    private val stateWaiter: ScriptStateWaiter? = null,
) {

    /**
     * Executa o script sequencialmente.
     * @param script Definição do script.
     * @param isConfirmed Indica se houve confirmação prévia do usuário caso o script possua passos sensíveis.
     * @param listener Callback opcional de progresso para a UI ou logs.
     */
    suspend fun run(
        script: ScriptDefinition,
        isConfirmed: Boolean = false,
        listener: ScriptProgressListener? = null
    ): ExecutionResult {
        // 1. Validação estática prévia
        val validation = validator.validate(script)
        if (validation is ScriptValidationResult.Invalid) {
            return ExecutionResult.Failure("Script inválido: ${validation.reason}")
        }

        if (script.steps.any { it is ScriptStep.WaitForState } && stateWaiter == null) {
            return ExecutionResult.Unavailable
        }

        // 2. Avaliação de risco agregado (fail-safe)
        val risk = riskEvaluator.evaluate(script)
        if (risk is RiskAssessment.RequiresConfirmation && !isConfirmed) {
            return ExecutionResult.Denied
        }

        // 3. Execução sequencial dos passos com fail-fast
        val total = script.steps.size
        for ((index, step) in script.steps.withIndex()) {
            listener?.onStepStarted(index, total, step)
            when (step) {
                is ScriptStep.Execute -> {
                    val result = router.route(step.command, isConfirmed)
                    if (result !is ExecutionResult.Success) {
                        return result
                    }
                }
                is ScriptStep.Delay -> {
                    sleeper.sleep(step.milliseconds)
                }
                is ScriptStep.WaitForState -> {
                    val reached = stateWaiter?.await(step.condition, step.timeoutMs) == true
                    if (!reached) {
                        return ExecutionResult.Failure("A condição esperada da rotina não foi confirmada.")
                    }
                }
            }
        }

        return ExecutionResult.Success("Rotina '${script.name}' enviada para execução ($total passos).")
    }
}
