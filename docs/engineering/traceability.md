# Matriz de rastreabilidade — Acesso60

**Data de consolidação:** 28/10/2026  
**Branch:** `RT03`  
**Escopo:** núcleo compartilhado `:core` e aplicação Android `:app`.

## Estados permitidos

- **Implementado:** código ou teste localizado no repositório.
- **Verificado:** execução reproduzida com evidência registrada.
- **Bloqueado:** execução impedida por ambiente, dispositivo ou dependência.
- **A validar:** ainda não há evidência suficiente.

## Matriz

| ID | História/objetivo | Implementação | Teste/evidência | Estado |
|---|---|---|---|---|
| A60-RF-01 | Iniciar escuta mediante ação do usuário | `AssistantPresenter`, `RealAssistantController` | `AssistantPresenterTest`, smoke no emulador | Verificado parcialmente: escuta iniciou |
| A60-RF-02 | Reconhecer fala | `AndroidSpeechToTextEngine`, `RealAssistantController` | teste do controlador com fake; smoke sem transcrição | Implementado; fala real A validar |
| A60-RF-03 | Interpretar comandos apoiados | `core/.../CommandInterpreter.kt` | `CommandInterpreterTest` | Verificado no núcleo |
| A60-RF-04 | Validar comandos | `core/.../CommandValidator.kt` | testes de validador | Verificado no núcleo |
| A60-RF-05 | Classificar risco | `core/.../RiskPolicy.kt` | `RiskPolicyTest` | Verificado no núcleo |
| A60-RF-06 | Confirmar ação sensível | `core/.../CommandRouter.kt`, `AssistantPresenter` | `IntegrationPipelineTest`, `AssistantPresenterTest` | Verificado em testes isolados |
| A60-RF-07 | Rotear comando permitido | `core/.../CommandRouter.kt`, `RealAssistantController` | integração do núcleo e teste do controlador | Implementado; verificação Android pendente |
| A60-RF-08 | Navegar HOME/BACK | `AndroidCommandExecutor`, `Acesso60AccessibilityService` | serviço ativo no emulador; comando não transcrito | BACK implementado; execução real A validar |
| A60-RF-09 | Abrir aplicativo | `Command.OpenApp` no núcleo | nenhum executor Android | Não implementado |
| A60-RF-10 | Alterar volume | `Command.VolumeUp/Down` no núcleo | nenhum executor Android | Não implementado |
| A60-RF-11 | Ler conteúdo acessível | configuração do serviço, sem fluxo de leitura | nenhum teste | Não implementado |
| A60-RF-12 | Informar resultado | `AssistantReducer`, telas Compose, eventos reais | testes unitários/UI e smoke de erro | Verificado parcialmente |
| A60-RF-13 | Recuperar falha de interpretação | `RealAssistantController`, `ResultScreen` | testes de reducer/controlador | Implementado; A validar no dispositivo |

## Contratos dos módulos Android

- `RecognizerClient` isola `SpeechRecognizer` e possui testes JVM para callbacks,
  normalização, erros e ciclo de vida.
- `AccessibilityGateway` isola o serviço do executor e possui testes JVM para
  sucesso, indisponibilidade e comandos não suportados.
- O ciclo de vida real do `AccessibilityService` permanece `A validar` por exigir
  teste instrumentado/manual com o serviço ativado.

## Auditoria de 28/10/2026

`RealAssistantControllerTest` cobre sucesso de `voltar` com executor fake,
comando desconhecido, erro de reconhecimento, cancelamento tardio e exigencia
de confirmacao para comando sensivel. A execucao foi reproduzida com
`:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`,
`:app:assembleDebug` e `:app:connectedDebugAndroidTest` (5/5). Isso verifica
os contratos modulares, mas nao substitui STT real, audio pre-gravado ou
validacao fisica do servico Android.

## Regras de rastreabilidade

1. Uma história só pode ser declarada **Verificada** quando houver execução
   reproduzível e evidência identificável.
2. Código existente sem teste ou execução permanece **Implementado** ou
   **A validar**.
3. Falha de ambiente deve ser registrada como **Bloqueado**, sem ser convertida
   em aprovação.
4. Toda anomalia deve indicar severidade, impacto, reprodução e referência ao
   caso de teste.
