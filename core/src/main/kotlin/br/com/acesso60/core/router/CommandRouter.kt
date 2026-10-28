package br.com.acesso60.core.router

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.model.RiskAssessment
import br.com.acesso60.core.model.ValidationResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.validator.CommandValidator

/**
 * Roteador de comandos do domínio Acesso60.
 * Assegura que apenas comandos validados e autorizados segundo a política de risco
 * sejam encaminhados ao executor responsável.
 */
class CommandRouter(
    private val validator: CommandValidator,
    private val riskPolicy: RiskPolicy,
    private val executor: CommandExecutor
) {

    /**
     * Roteia e despacha o comando após validação e verificação de risco.
     * @param command Comando tipado a ser processado.
     * @param isConfirmed Indica se houve confirmação explícita do usuário para ações sensíveis.
     */
    fun route(command: Command, isConfirmed: Boolean = false): ExecutionResult {
        // 1. Validação estrutural do comando
        val validation = validator.validate(command)
        if (validation is ValidationResult.Invalid) {
            return ExecutionResult.Failure("Comando inválido: ${validation.reason}")
        }

        // 2. Avaliação de risco e autorização
        val risk = riskPolicy.evaluate(command)
        if (risk is RiskAssessment.RequiresConfirmation && !isConfirmed) {
            return ExecutionResult.Denied
        }

        // 3. Despacho seguro ao executor
        return executor.execute(command)
    }
}
