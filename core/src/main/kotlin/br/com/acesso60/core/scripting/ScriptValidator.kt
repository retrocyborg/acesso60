package br.com.acesso60.core.scripting

import br.com.acesso60.core.model.ValidationResult
import br.com.acesso60.core.validator.CommandValidator

/**
 * Resultado da validação estática de um script.
 */
sealed interface ScriptValidationResult {
    data object Valid : ScriptValidationResult
    data class Invalid(val reason: String) : ScriptValidationResult
}

/**
 * Validador estático de scripts do Acesso60.
 * Assegura integridade e impõe limites rígidos de segurança para evitar negação de serviço.
 */
class ScriptValidator(
    private val commandValidator: CommandValidator = CommandValidator(),
    private val maxSteps: Int = 10,
    private val maxDelayMs: Long = 10000L
) {
    /**
     * Valida os metadados e cada passo do script informado.
     */
    fun validate(script: ScriptDefinition): ScriptValidationResult {
        if (script.id.isBlank()) {
            return ScriptValidationResult.Invalid("O identificador do script não pode ser vazio.")
        }
        if (script.name.isBlank()) {
            return ScriptValidationResult.Invalid("O nome do script não pode ser vazio.")
        }
        if (script.aliases.isEmpty() || script.aliases.all { it.isBlank() }) {
            return ScriptValidationResult.Invalid("O script deve possuir ao menos um alias de ativação válido.")
        }
        if (script.steps.isEmpty()) {
            return ScriptValidationResult.Invalid("O script não contém passos para execução.")
        }
        if (script.steps.size > maxSteps) {
            return ScriptValidationResult.Invalid("O script excede o limite máximo permitido de $maxSteps passos (possui ${script.steps.size}).")
        }

        for ((index, step) in script.steps.withIndex()) {
            val stepNumber = index + 1
            when (step) {
                is ScriptStep.Execute -> {
                    val cmdValidation = commandValidator.validate(step.command)
                    if (cmdValidation is ValidationResult.Invalid) {
                        return ScriptValidationResult.Invalid("Passo $stepNumber possui comando inválido: ${cmdValidation.reason}")
                    }
                }
                is ScriptStep.Delay -> {
                    if (step.milliseconds <= 0) {
                        return ScriptValidationResult.Invalid("Passo $stepNumber possui delay inválido (deve ser maior que zero).")
                    }
                    if (step.milliseconds > maxDelayMs) {
                        return ScriptValidationResult.Invalid("Passo $stepNumber excede o delay máximo permitido de ${maxDelayMs}ms.")
                    }
                }
                is ScriptStep.WaitForState -> {
                    if (step.condition.isBlank()) {
                        return ScriptValidationResult.Invalid("Passo $stepNumber possui condição de espera vazia.")
                    }
                    if (step.timeoutMs <= 0 || step.timeoutMs > maxDelayMs) {
                        return ScriptValidationResult.Invalid("Passo $stepNumber possui timeout inválido.")
                    }
                }
            }
        }

        return ScriptValidationResult.Valid
    }
}
