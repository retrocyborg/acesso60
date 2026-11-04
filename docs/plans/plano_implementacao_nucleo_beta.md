# Plano de Implementação — CONTRATO-01 e Núcleo Beta P0

## Objetivo
Implementar a camada de domínio puro (**Núcleo Beta**) do aplicativo Acesso60 de forma totalmente desacoplada de APIs Android e UI, conforme especificado em [`docs/engineering/nucleo.md`](file:///C:/Prog/acesso60/docs/engineering/nucleo.md) e [`BETA_NUCLEO_TODO.md`](file:///C:/Prog/acesso60/BETA_NUCLEO_TODO.md), acompanhado de sua suíte completa de testes unitários (`UT-01` a `UT-05`) e teste de integração com executor simulado (`IT-01`).

---

## User Review Required

> [!IMPORTANT]
> **Desacoplamento Rigoroso**: O núcleo opera exclusivamente com Kotlin puro na JVM. Ele não importará `android.*`, nem fará chamadas diretas a microfone, acessibilidade ou telas. A integração física será feita por adaptadores externos na etapa P1.

> [!NOTE]
> **Classificação de Risco**:
> - Comandos de navegação simples (`"voltar"`, `"início"`) são classificados como `LowRisk`.
> - Ações destrutivas ou com efeitos externos (ex.: envio de mensagens, exclusão de dados) exigem confirmação explícita (`RequiresConfirmation`).

---

## Open Questions

> [!NOTE]
> Há algum comando extra além de `"voltar"` e `"início"` / `"inicio"` que você deseja contemplar de imediato nesta entrega do Núcleo Beta, ou seguimos estritamente o escopo mínimo delimitado para a RT03?

---

## Proposed Changes

### Camada de Domínio (`src/main/kotlin/br/com/acesso60/core/`)

#### [NEW] `model/Command.kt`
Modelo tipado representando as intenções do usuário:
- `sealed interface Command`
  - `data object Back : Command`
  - `data object Home : Command`
  - `data class SensitiveAction(val actionName: String) : Command` (para simulação de fluxo com risco)

#### [NEW] `model/CoreResults.kt`
Resultados tipados para cada etapa do pipeline:
- `sealed interface InterpretationResult`:
  - `data class Success(val command: Command) : InterpretationResult`
  - `data class Unknown(val rawText: String) : InterpretationResult`
- `sealed interface ValidationResult`:
  - `data class Valid(val command: Command) : ValidationResult`
  - `data class Invalid(val reason: String) : ValidationResult`
- `sealed interface RiskAssessment`:
  - `data object LowRisk : RiskAssessment`
  - `data class RequiresConfirmation(val message: String) : RiskAssessment`
- `sealed interface ExecutionResult`:
  - `data class Success(val message: String) : ExecutionResult`
  - `data object Denied : ExecutionResult`
  - `data class Failure(val error: String) : ExecutionResult`

#### [NEW] `interpreter/CommandInterpreter.kt`
Normaliza o texto de entrada (caixa baixa, remoção de espaços extras e normalização de diacríticos) e mapeia determinística e estritamente para `Command`.

#### [NEW] `validator/CommandValidator.kt`
Valida entradas de texto (rejeitando nulos, em branco ou caracteres inválidos) e assegura que somente comandos válidos prossigam.

#### [NEW] `policy/RiskPolicy.kt`
Avalia o `Command` e retorna o `RiskAssessment` apropriado, impedindo que comandos sensíveis sejam executados diretamente sem autorização.

#### [NEW] `router/CommandRouter.kt` & `executor/CommandExecutor.kt`
- `interface CommandExecutor`: contrato que desacopla a execução.
- `class CommandRouter`: orquestra a validação, verificação de risco e delegação ao executor competente.

---

### Camada de Testes (`tests/br/com/acesso60/core/`)

#### [NEW] `InterpreterAndValidatorTest.kt`
- **`UT-01`**: `"voltar"` $\rightarrow$ `Command.Back`.
- **`UT-02`**: `"início"` e `"inicio"` $\rightarrow$ `Command.Home`.
- **`UT-03`**: Frase desconhecida $\rightarrow$ resultado `Unknown` sem comando executável.
- **`UT-04`**: Texto vazio / em branco $\rightarrow$ rejeição pelo validador.

#### [NEW] `RiskPolicyTest.kt`
- **`UT-05`**: Ação sensível $\rightarrow$ classificação `RequiresConfirmation` ou bloqueio de execução direta.

#### [NEW] `IntegrationPipelineTest.kt`
- **`IT-01`**: Pipeline completo `Interpreter → Validator → RiskPolicy → Router` operando com um `FakeExecutor` simulado em memória, gerando evidências verificáveis de execução e contenção de risco.

---

## Verification Plan

### Automated Tests
Execução da suíte de testes unitários e de integração via Gradle Wrapper:
```powershell
.\gradlew.bat test
```
Verificação dos relatórios de teste gerados em `build/reports/tests/test/index.html`.

### Manual Verification & Evidência
1. Registrar a execução em [`BETA_NUCLEO_TODO.md`](file:///C:/Prog/acesso60/BETA_NUCLEO_TODO.md) e [`docs/milestones/roadmap.md`](file:///C:/Prog/acesso60/docs/milestones/roadmap.md).
2. Confirmar que não há nenhum acoplamento com classes da plataforma Android.
