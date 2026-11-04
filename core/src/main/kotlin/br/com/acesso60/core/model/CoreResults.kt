package br.com.acesso60.core.model

/**
 * Resultado da etapa de interpretação de texto natural.
 */
sealed interface InterpretationResult {
    data class Success(val command: Command) : InterpretationResult
    data class Unknown(val rawText: String) : InterpretationResult
}

/**
 * Resultado da etapa de validação sintática e semântica de comandos.
 */
sealed interface ValidationResult {
    data class Valid(val command: Command) : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}

/** Resultado da validação estrutural do texto antes da interpretação. */
sealed interface RawInputValidationResult {
    data class Valid(val rawText: String) : RawInputValidationResult
    data class Invalid(val reason: String) : RawInputValidationResult
}

/**
 * Avaliação da política de segurança e risco para a execução de um comando.
 */
sealed interface RiskAssessment {
    data object LowRisk : RiskAssessment
    data class RequiresConfirmation(val message: String) : RiskAssessment
}

/**
 * Resultado da tentativa de execução de um comando por um executor autorizado.
 */
sealed interface ExecutionResult {
    data class Success(val message: String) : ExecutionResult
    data object Denied : ExecutionResult
    data object Unavailable : ExecutionResult
    data class Failure(val error: String) : ExecutionResult
}
