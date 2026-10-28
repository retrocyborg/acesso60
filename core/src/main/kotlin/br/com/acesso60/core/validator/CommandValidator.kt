package br.com.acesso60.core.validator

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.RawInputValidationResult
import br.com.acesso60.core.model.ValidationResult

/**
 * Validador de entradas e comandos do domínio Acesso60.
 * Garante que entradas nulas, em branco ou inconsistentes sejam rejeitadas sem provocar execução.
 */
class CommandValidator {

    /**
     * Valida o texto bruto recebido do reconhecimento de voz.
     */
    fun validateRawInput(rawText: String?): RawInputValidationResult {
        if (rawText == null) {
            return RawInputValidationResult.Invalid("Entrada nula")
        }
        if (rawText.isBlank()) {
            return RawInputValidationResult.Invalid("Entrada vazia ou contendo apenas espaços")
        }
        return RawInputValidationResult.Valid(rawText.trim())
    }

    /**
     * Valida um comando já interpretado antes da análise de risco e roteamento.
     */
    fun validate(command: Command?): ValidationResult {
        if (command == null) {
            return ValidationResult.Invalid("Comando inexistente ou nulo")
        }

        return when (command) {
            is Command.Back -> ValidationResult.Valid(command)
            is Command.Home -> ValidationResult.Valid(command)
            is Command.ReadScreen -> ValidationResult.Valid(command)
            is Command.VolumeUp -> ValidationResult.Valid(command)
            is Command.VolumeDown -> ValidationResult.Valid(command)
            is Command.OpenApp -> if (command.appName.isBlank()) {
                ValidationResult.Invalid("Aplicativo sem nome")
            } else {
                ValidationResult.Valid(command)
            }
            is Command.SensitiveAction -> {
                if (command.description.isBlank()) {
                    ValidationResult.Invalid("Ação sensível sem descrição de operação")
                } else {
                    ValidationResult.Valid(command)
                }
            }
        }
    }
}
