# Plano de testes Android — RT03

## Objetivo

Validar a usabilidade e a integração real do Acesso60 no Android, mantendo o
domínio compartilhado testado fora do SDK e registrando evidências conforme as
diretivas de QA do projeto.

## Ambientes

### Ambiente mínimo atual

- Emulador `Medium_Phone_API_37.0`.
- Android SDK Platform 37.
- Build Tools 35/36.
- `adb` 37.0.1.
- JDK 17 recomendado para o projeto Android.

### Evidência obrigatória

- modelo/API do dispositivo;
- versão do APK e commit;
- permissões concedidas;
- comando executado;
- resultado observado;
- logcat sanitizado;
- screenshot ou vídeo quando o comportamento for visual.

## Anomalia QA identificada em 28/10/2026

- **ID:** QA-AND-001.
- **Classificação:** P0 — inconsistência de estado na interface.
- **Reprodução:** tentar escutar com a acessibilidade desligada, ativar o
  serviço nas configurações oficiais e retornar ao Acesso60.
- **Resultado observado antes da correção:** a tela mantinha a mensagem de
  serviço desligado mesmo com o serviço habilitado e conectado no Android.
- **Correção aplicada:** `AssistantPresenter.refreshPermissions()` atualiza as
  permissões e retorna à tela inicial quando a falha transitória foi resolvida,
  sem iniciar a escuta automaticamente.
- **Pendência:** repetir o fluxo no emulador e registrar dump de UI/logcat.

## Testes modulares adicionados em 28/10/2026

| ID | Componente | Cobertura | Status |
|---|---|---|---|
| VOICE-UT-001 | `AndroidSpeechToTextEngine` | resultados parcial/final, normalização e descarte de vazio | Implementado e aprovado |
| VOICE-UT-002 | `AndroidSpeechToTextEngine` | erro e ciclo de vida stop/cancel/destroy | Implementado e aprovado |
| EXEC-UT-001 | `AndroidCommandExecutor` | BACK com sucesso e indisponibilidade | Implementado e aprovado |
| EXEC-UT-002 | `AndroidCommandExecutor` | comando não suportado não alcança o gateway | Implementado e aprovado |
| A11Y-UT-001 | `AccessibilityGateway` | contrato fake usado pelo executor | Implementado e aprovado |

Os testes JVM usam `RecognizerClient` e `AccessibilityGateway`, mantendo as
APIs reais do Android confinadas aos adaptadores e aos testes instrumentados.

## Casos P0

| ID | Cenário | Resultado esperado |
|---|---|---|
| TC-AND-001 | Abrir aplicativo | Tela inicial aparece sem crash |
| TC-AND-002 | Microfone negado | Escuta não inicia; orientação é clara |
| TC-AND-003 | Acessibilidade desligada | Escuta não inicia; app informa como ativar o serviço |
| TC-AND-004 | Comando desconhecido | Nenhuma ação é executada; erro acessível é exibido |
| TC-AND-005 | Ação sensível sem confirmação | Execução é bloqueada |
| TC-AND-006 | Cancelar confirmação | Nenhuma ação é executada |
| TC-AND-007 | Duplo toque em confirmar | Apenas uma execução é solicitada |
| TC-AND-008 | Falha do executor | Resultado indica falha, nunca sucesso falso |

## Casos P1

| ID | Cenário | Resultado esperado |
|---|---|---|
| TC-AND-009 | STT reconhece comando válido | Texto, comando e estado são coerentes |
| TC-AND-010 | STT retorna silêncio/timeout | Erro recuperável sem stack trace |
| TC-AND-011 | Serviço ativado | Status muda para ligado após retornar às configurações |
| TC-AND-012 | Executar `voltar` | Ação real ocorre e o resultado é verificável |
| TC-AND-013 | Executar abertura de aplicativo | Aplicativo correto é aberto ou falha explicitamente |
| TC-AND-014 | TalkBack habilitado | Controles têm rótulos e ordem de foco compreensíveis |
| TC-AND-015 | Texto muito grande | Conteúdo não fica cortado nem perde ações essenciais |

## Procedimento de execução

```powershell
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :core:test :app:testDebugUnitTest :app:assembleDebug
adb devices -l
adb -s <device> install -r .\Acesso60\app\build\outputs\apk\debug\app-debug.apk
```

Os casos manuais devem ser executados um por vez e registrados com o modelo:

```text
Data:
Caso:
Commit:
Dispositivo/API:
Pré-condições:
Passos:
Resultado esperado:
Resultado observado:
Evidência:
Status: passou | falhou | bloqueado | não executado
Severidade/impacto:
Pendência:
```

## Limitações atuais

- STT real ainda não foi validado.
- O `AccessibilityService` ainda não foi validado em execução real.
- O emulador apresentou diálogo persistente do Android Accessibility Suite
  durante o teste manual do botão principal.
- Build Android aprovado não substitui teste em dispositivo físico.

## Evidência executada em 28/10/2026

- `:app:connectedDebugAndroidTest`: 5 testes passaram no `Medium_Phone_API_37.0`.
- A Activity iniciou no `emulator-5554` sem exceção fatal do aplicativo.
- `RECORD_AUDIO` foi concedida e o serviço foi ativado pelo fluxo oficial das
  configurações de acessibilidade.
- A UI exibiu `Serviço de acessibilidade: Ligado` e entrou em `Estou ouvindo`.
- Sem fala reconhecida, a aplicação apresentou erro amigável e ofereceu nova
  tentativa, sem stack trace.
- A versão anterior do Espresso falhou com `InputManager.getInstance` no API
  37; `androidx.test.espresso:espresso-core:3.7.0` foi fixado e os testes
  passaram após a correção.
- O comando real `voltar` ainda permanece **A validar**, pois não houve
  transcrição de fala no emulador.
