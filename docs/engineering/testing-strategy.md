# Estratégia de testes — Acesso60

## Objetivos e Padrão-Ouro Normativo
Verificar o processamento de comandos, proteção contra execuções indevidas, feedback acessível e integração Android, em total alinhamento aos seis critérios acadêmicos e aos padrões internacionais:
- **ISO/IEC/IEEE 29119 / 29119-1:** Conceitos, processos e modelo de teste por camadas.
- **IEEE 829:** Estrutura documental de especificação, procedimentos e relatórios de evidências.
- **IEEE 1012:** Verificação e Validação (V&V) rigorosa, com nível máximo de integridade aplicado à política de risco.
- **IEEE 1028:** Auditorias e revisões sistemáticas de testes e integridade de código.
- **IEEE 1044:** Classificação e triagem padronizada de anomalias e falhas de software.
- **ISO 9000:2026:** Foco contínuo no usuário final (pessoas idosas) e controle de processos por evidências.

## Pirâmide inicial (ISO/IEC/IEEE 29119-1)
1. **Unitários:** interpretador, validador, política de risco e roteador, isolados do SDK Android (JVM pura via JUnit 5).
2. **Integração:** percurso texto → comando → validação → risco → executor fake → feedback.
3. **UI:** Compose nos estados inicial, escutando, confirmação, erro e sucesso.
4. **E2E:** comandos em emulador/dispositivo, com permissões/serviço habilitados de maneira legítima.

## Casos mínimos propostos
| Teste | Entrada/condição | Esperado |
|---|---|---|
| INT-01 | `voltar` | `Command.Back` |
| INT-02 | `início` e `inicio` | `Command.Home` ou resultado equivalente |
| INT-03 | texto sem comando reconhecível | falha de interpretação sem execução |
| VAL-01 | comando nulo/incompleto | inválido |
| RISK-01 | `Back` | categoria baixa conforme política aprovada |
| RISK-02 | ação sensível simulada | requer confirmação ou bloqueio |
| ROUTE-01 | comando autorizado | roteia somente uma vez |
| E2E-01 | “voltar” com serviço ativo | tentativa de BACK e feedback honesto |
| E2E-02 | ação sensível cancelada | nenhuma ação realizada |
| E2E-03 | STT falha ou transcrição vazia | mensagem de recuperação |
| UI-01 | permissão do microfone negada | orientação acessível, sem crash |

## Evidências a preservar (IEEE 829 e IEEE 1044)
Conforme estruturação do **IEEE 829** (relatório de teste) e classificação de anomalias do **IEEE 1044**:
Commit, data, dispositivo/Android, identificador, cenário, comando executado, resultado esperado, resultado observado, logs sanitizados, screenshot/vídeo quando necessário e status formal (`passou`, `falhou`, `bloqueado`, `não executado`). Em caso de anomalia, registrar severidade e impacto no usuário.

## Metas
- **Obrigatório por segurança:** nenhum teste deve comprovar execução de ação sensível sem autorização.
- Cobertura, tempo de resposta e taxa de comandos reconhecidos exigem coleta de baseline e definição de limiares antes de afirmar conformidade.
- Testes com usuários reais dependem de consentimento e desenho apropriado; não inventar resultados.

## Risco de testes com hardware
`SpeechRecognizer` depende da disponibilidade/configuração do dispositivo; `AccessibilityService` depende de ativação explícita. Nos testes unitários, utilizar fakes para isolar essas dependências. O resultado em mock não substitui teste E2E real.

## Execucao auditada em 28/10/2026

Foram executados com sucesso `:core:test`, `:app:testDebugUnitTest`,
`:app:lintDebug`, `:app:assembleDebug` e
`:app:connectedDebugAndroidTest` (5/5 no emulador API 37). A cobertura
modular do controlador real inclui comando desconhecido, erro de STT e
confirmacao de comando sensivel. Audio pre-gravado, STT real e execucao real
de `voltar` continuam **A validar**.

## Testes de contrato do ExecutionWrapper — proposta

Estes testes devem validar o wrapper com executores fake, sem depender de
`AccessibilityService`, `AudioManager`, `PackageManager`, dispositivo ou
modelo de fala. O objetivo é comprovar o contrato de despacho e tratamento de
resultados independentemente da implementação concreta do executor Android.

| ID | Cenário | Resultado esperado |
|---|---|---|
| WRAP-001 | Um executor declara suporte ao comando | O wrapper encaminha o comando exatamente uma vez. |
| WRAP-002 | Nenhum executor declara suporte | Retorna `ExecutionResult.Unavailable` e não executa nenhum fake. |
| WRAP-003 | Dois executores declaram suporte ao mesmo comando | Retorna falha de configuração ou rejeita o registry; nenhum despacho ambíguo ocorre. |
| WRAP-004 | Executor retorna `Success` | O wrapper preserva a mensagem e o tipo do resultado. |
| WRAP-005 | Executor retorna `Unavailable` | O wrapper preserva a indisponibilidade sem convertê-la em sucesso. |
| WRAP-006 | Executor retorna `Failure` | O wrapper preserva a falha controlada e não expõe stack trace. |
| WRAP-007 | Executor lança exceção | O wrapper converte a exceção em `Failure` controlado e encerra a tentativa. |
| WRAP-008 | Executor recebe comando diferente do suportado | O executor não é chamado; o wrapper retorna indisponibilidade ou falha de contrato. |
| WRAP-009 | Dois comandos autorizados consecutivos | Cada comando gera uma execução independente e na ordem recebida. |
| WRAP-010 | Middleware de logging envolve o registry | Início e resultado são registrados sem alterar o resultado funcional. |
| WRAP-011 | Middleware interrompe execução duplicada | A segunda tentativa é recusada conforme a política do middleware; a primeira permanece inalterada. |
| WRAP-012 | Comando sensível chega ao wrapper sem autorização | O contrato de integração deve impedir o encaminhamento; a autorização permanece responsabilidade do `CommandRouter`. |
| WRAP-013 | Executor fake usa gateway indisponível | O wrapper devolve `Unavailable` e não presume que a ação ocorreu. |
| WRAP-014 | Registry vazio | A inicialização ou a execução falha de forma explícita e previsível. |

### Critérios de isolamento

Os testes do wrapper não devem instanciar APIs Android nem depender de estado
global. Cada executor fake deverá registrar chamadas, comando recebido e
resultado configurado. Os testes também devem verificar que o wrapper não
interpreta texto, não chama `CommandInterpreter`, não avalia risco e não altera
`ExecutionResult` produzido pelo executor.

Os IDs acima são casos planejados, não resultados executados. A implementação
deverá registrar data, branch, comando, resultado esperado, resultado observado
e limitações conforme o padrão de evidências deste documento.

### Suíte específica de abertura de aplicativos

O fluxo do wrapper deve possuir testes modulares separados para as duas falhas
operacionais mais importantes:

- `OpenAppExecutionIntegrationTest.applicationNotInstalled`: o launcher fake
  retorna `NotInstalled` e o wrapper deve devolver `Unavailable`;
- `OpenAppExecutionIntegrationTest.launcherFailure`: o launcher fake retorna
  `Failed` e o wrapper deve devolver `Failure` controlado, sem exceção ou
  sucesso falso.

Esses testes verificam o fluxo completo do wrapper e não dependem de o WhatsApp
estar instalado no emulador. A presença real do pacote e a abertura por
`Intent` devem ser validadas separadamente em teste Android/manual.
Os testes de abertura devem usar uma matriz de `AppDefinition` como fixture,
com nomes lógicos e pacotes distintos. WhatsApp é apenas um caso inicial;
adicionar outro aplicativo deve exigir somente uma nova definição de dados,
sem duplicar testes ou alterar o wrapper.
## Resultado dos testes do wrapper — 03/11/2026

Os testes unitários e modulares executados foram:

```text
:core:test
:app:testDebugUnitTest
:app:assembleDebug
```

Resultado observado: `BUILD SUCCESSFUL`. O APK debug foi instalado no
`emulator-5554`. O teste manual confirmou reconhecimento de `abrir whatsapp` e
`abrir zap`, seguido de confirmação e resultado de aplicativo indisponível,
sem erro de acessibilidade indevido e sem crash. O pacote `com.whatsapp` não
estava instalado; portanto, o caminho de sucesso real ainda não foi executado.
## Distinção de falhas de compreensão — 03/11/2026

O fluxo de resultado agora separa `COMMAND_UNKNOWN`, quando o STT produziu
texto mas o interpretador não encontrou comando apoiado, de `NOT_UNDERSTOOD` e
`NO_SPEECH_DETECTED`, usados quando a fala não foi compreendida ou detectada.
Cada motivo possui título e orientação próprios na UI, sem expor detalhes
técnicos.
## Testes do widget do Launcher — proposta

Casos previstos para o App Widget:

| ID | Cenário | Resultado esperado |
|---|---|---|
| WIDGET-001 | Pacote instalado | `Acesso60WidgetProvider` é registrado pelo Android. |
| WIDGET-002 | Widget adicionado ao Launcher | Layout grande exibe nome e ação sem corte. |
| WIDGET-003 | Toque no widget | Abre `MainActivity`. |
| WIDGET-004 | Leitor de tela | Content description anuncia o widget e sua ação. |
| WIDGET-005 | Redimensionamento horizontal/vertical | Conteúdo permanece legível e acionável. |
| WIDGET-006 | Activity já aberta | PendingIntent reutiliza a Activity sem criar fluxo inconsistente. |

O build e o registro do receiver foram verificados; os casos que dependem da
interface do Launcher permanecem **A validar** manualmente.

## Auditoria de integração — 04/11/2026

O comando `:core:test :app:testDebugUnitTest :app:lintDebug
:app:assembleDebug` foi executado na raiz pelo Gradle Wrapper 9.3.1. Resultado
observado: `BUILD SUCCESSFUL`, com 36 testes do núcleo e 56 testes unitários
Android, sem falhas ou erros. O ADB não listou dispositivo conectado; por isso,
`:app:connectedDebugAndroidTest`, fluxo de voz no dispositivo, Launcher e
validação visual não foram executados nesta revisão.

Os casos automatizados desta auditoria incluem aliases de rotina ambíguos,
campos desconhecidos no parser, espera por estado sem adaptador, falha de espera
e resultado de abertura de aplicativo comunicado como não verificado.
