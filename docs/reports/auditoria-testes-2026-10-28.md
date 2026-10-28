# Auditoria tecnica e testes - 28/10/2026

## Escopo e metodo

Foi feita uma revisao da separacao entre `core` e `Acesso60/app`, dos fluxos
de UI Compose, voz, permissao de microfone e acessibilidade. A execucao foi
realizada no branch `RT03`, preservando as demais alteracoes locais.

## Ambiente

- Windows; Gradle Wrapper 9.3.1; JDK conforme configuracao do projeto.
- Emulador `Medium_Phone_API_37.0(AVD) - 17`, API 37, dispositivo
  `emulator-5554`.
- `ANDROID_HOME` apontado temporariamente para o SDK local.

## Alteracao de testes

Foram adicionados tres cenarios em
`Acesso60/app/src/test/java/com/acesso60/ui/RealAssistantControllerTest.kt`:

- transcricao desconhecida gera `NOT_UNDERSTOOD`;
- comando sensivel exige confirmacao antes da execucao;
- erro do reconhecimento gera falha de microfone sem executar comando.

Os testes usam fakes para `SpeechToTextEngine` e `CommandExecutor`. Isso
verifica o contrato modular sem declarar que o STT ou o servico Android real
foram validados.

## Execucoes e resultados

| Comando | Resultado |
|---|---|
| `:core:test` | passou |
| `:app:testDebugUnitTest` | passou |
| `:app:lintDebug` | passou |
| `:app:assembleDebug` | passou |
| `:app:connectedDebugAndroidTest` | 5/5 passaram |

Comando principal:

```powershell
$env:GRADLE_USER_HOME = (Join-Path (Get-Location) '.gradle-user-home')
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\\Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\\gradlew.bat :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
.\\gradlew.bat :app:connectedDebugAndroidTest
```

## Achados

- A separacao do dominio compartilhado no modulo `core` esta coberta por
  testes unitarios e de integracao.
- A camada de apresentacao possui cobertura para permissoes, estados,
  confirmacao, cancelamento, retry e eventos tardios.
- A camada de controle real agora possui cobertura explicita para sucesso,
  comando desconhecido, confirmacao sensivel, cancelamento tardio e erro de
  reconhecimento.
- Os testes instrumentados Compose passaram no emulador API 37.
- O teste com audio pre-gravado, o reconhecimento de fala real e a execucao
  real de `voltar` permanecem **A validar**.
- O ciclo completo do `AccessibilityService` apos ativacao oficial permanece
  **A validar** neste registro; mocks e configuracao direta do emulador nao
  substituem o fluxo manual oficial.

## Risco e limitacoes

Nao foram encontrados erros de build, lint ou testes nesta execucao. A
validacao nao comprova desempenho, reconhecimento em dispositivo fisico,
TalkBack, audio pre-gravado ou cobertura percentual, pois nao ha baseline ou
ferramenta de cobertura configurada no repositorio.

## Evidencias

- `Acesso60/app/build/reports/lint-results-debug.html`
- `Acesso60/app/build/reports/androidTests/connected/debug/index.html`
- `Acesso60/app/build/outputs/androidTest-results/connected/debug/`
- `Acesso60/app/src/test/java/com/acesso60/ui/RealAssistantControllerTest.kt`
