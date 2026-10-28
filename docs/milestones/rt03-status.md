# RT03 — status e evidências

**Última consolidação de conversas:** 26/10/2026. **Auditoria do código:** não realizada neste pacote.

## Regra de status
`Não verificado` significa ausência de inspeção/evidência neste material, **não** afirma que a equipe não fez a atividade.

| Entregável RT03 | Situação confirmável a partir da conversa | Evidência ainda necessária |
|---|---|---|
| Documentação consolidada | Conteúdo-base reunido neste pacote | Comparar com documentos oficiais do GitBook |
| Protótipo interativo / alfa-beta | Telas e fluxo planejados | Link navegável, vídeo, APK ou demo |
| Testes unitários iniciais | Implementados (UT-01 a UT-05 e IT-01) com 13 testes passando (0 falhas) | Integração com emulador/hardware (frente Android) |
| Validação e tratamento de erros | Implementados (CommandValidator e RiskPolicy) com recusa e contenção comprovadas em testes | Validação visual na UI Compose |
| Relatório de uso de IA | Estrutura e exigências identificadas | Exemplos reais, revisões e riscos documentados |
| Roteiro de apresentação | Esqueleto proposto em `rt03-plan.md` | Slides e ensaio |
| Repositório GitHub | URL de referência registrada | Verificar branches, commits, fontes e permissões |
| Ferramentas QA | AGP 9.1.1, Gradle Wrapper 9.3.1 e JUnit 5 configurados; build e lint executados | Testes instrumentados, cobertura e validação em outro ambiente |

## Registro de verificações futuras
Ao realizar cada verificação, adicionar entrada neste formato:

```text
Data: 28/10/2026
Commit / branch: RT03 / uncommitted (working tree local)
Responsável: Agente / Pair Programming
Requisito / artefato: Compatibilidade Android API 37 / ferramentas de build
Ambiente: Windows 11, JDK 24.0.2, AGP 9.1.1, Gradle 9.3.1, Android SDK local
Comando / procedimento: .\\gradlew.bat :core:test :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
Resultado esperado: testes, lint e APK debug concluídos sem falha
Resultado observado: BUILD SUCCESSFUL in 3m 49s; 55 tarefas, lint com 4 avisos de dependências obsoletas/ortografia
Status: passou
Link de evidência: Acesso60/app/build/reports/lint-results-debug.html e Acesso60/app/build/outputs/apk/debug/app-debug.apk
Limitação: aviso de SDK XML versão 4 e biblioteca nativa não stripada; testes instrumentados ainda A validar
```

```text
Data: 28/10/2026
Commit / branch: RT03 / uncommitted (working tree local)
Responsável: Agente / Pair Programming
Requisito / artefato: Primeira fatia Beta real — voz → núcleo → BACK
Ambiente: Windows 11, JDK 24.0.2, AGP 9.1.1, Gradle 9.3.1
Comando / procedimento: .\\gradlew.bat :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
Resultado esperado: fluxo real compilado, testes unitários/UI passando e APK gerado
Resultado observado: BUILD SUCCESSFUL in 21s; controlador real e teste de evento tardio compilados; APK gerado
Status: passou parcialmente
Link de evidência: Acesso60/app/src/main/java/com/acesso60/ui/RealAssistantController.kt e Acesso60/app/src/test/java/com/acesso60/ui/RealAssistantControllerTest.kt
Limitação: execução com `:app:connectedDebugAndroidTest` bloqueada por `No connected devices!`; STT e AccessibilityService ainda A validar em dispositivo
```

```text
Data: 28/10/2026
Commit / branch: RT03 / uncommitted (working tree local)
Responsável: Agente / Pair Programming
Requisito / artefato: TC-AND-001, TC-AND-002, TC-AND-003, TC-AND-010, TC-AND-011, TC-AND-014
Ambiente: Medium_Phone_API_37.0, Android API 37, APK debug, emulador emulator-5554
Comando / procedimento: .\\gradlew.bat :app:connectedDebugAndroidTest; instalação via adb; ativação oficial do serviço; toque em Falar; dump de UI e logcat
Resultado esperado: testes instrumentados passam; Activity inicia; permissões e serviço refletem estado real; erro de áudio é amigável
Resultado observado: 5/5 testes instrumentados passaram; Activity iniciou; microfone concedido; serviço ligado; estado UI mudou para ligado; escuta exibida; falha sem áudio mostrou mensagem amigável sem crash
Status: passou parcialmente
Link de evidência: Acesso60/app/build/reports/androidTests/connected/debug/index.html; dump UI do emulator-5554; logcat sem FATAL EXCEPTION do Acesso60
Limitação: reconhecimento de uma fala real “voltar” não foi comprovado no emulador; a entrada de áudio permaneceu sem transcrição
```

```text
Data: 26/10/2026
Commit / branch: RT03 / uncommitted (working tree local)
Responsável: Agente / Pair Programming
Requisito / artefato: Ferramentas QA / Build & Test Runner (BETA-00.2)
Ambiente: Windows 11, JDK 24.0.2, Gradle 8.14.6
Comando / procedimento: .\gradlew.bat test
Resultado esperado: Inicialização do wrapper, compilação de projeto Kotlin JVM e execução de testes
Resultado observado: BUILD SUCCESSFUL in 22s (tasks: jar, compileKotlin, test)
Status: passou
Link de evidência: build.gradle.kts, settings.gradle.kts
Limitação: Código de aplicação (src/) e testes unitários (tests/) ainda a serem implementados
```

```text
Data: 26/10/2026
Commit / branch: RT03 / uncommitted (working tree local)
Responsável: Agente / Pair Programming
Requisito / artefato: Núcleo Beta P0 (CONTRATO-01, BETA-04 a BETA-07, UT-01 a UT-05, IT-01)
Ambiente: Windows 11, JDK 24.0.2, Gradle 8.14.6, JUnit 5
Comando / procedimento: .\gradlew.bat test
Resultado esperado: Execução com sucesso das 4 suítes de testes unitários e de integração
Resultado observado: BUILD SUCCESSFUL in 40s (13 testes executados, 0 falhas, 0 erros, 0 ignorados)
Status: passou
Link de evidência: build/test-results/test/ e build/reports/tests/test/index.html
Limitação: Integração com microfone físico e APIs de acessibilidade do sistema operacional dependem de adaptadores na fase P1
```

```text
Data:
Commit / branch:
Responsável:
Requisito / artefato:
Ambiente:
Comando / procedimento:
Resultado esperado:
Resultado observado:
Status: passou | falhou | bloqueado
Link de evidência:
Limitação:
```

```text
Data: 28/10/2026
Commit / branch: RT03 / uncommitted (working tree local)
Responsável: Agente / Pair Programming
Requisito / artefato: QA-AND-001 — sincronização do estado de acessibilidade
Ambiente: Windows, emulador Medium_Phone_API_37.0, Android API 37
Comando / procedimento: alteração do presenter; :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug; :app:connectedDebugAndroidTest
Resultado esperado: falha transitória de permissão é removida após a condição ser corrigida; testes passam
Resultado observado: BUILD SUCCESSFUL; 5/5 testes instrumentados passaram; APK debug gerado
Status: passou parcialmente
Link de evidência: Acesso60/app/src/main/java/com/acesso60/ui/AssistantPresenter.kt; Acesso60/app/src/test/java/com/acesso60/ui/AssistantPresenterTest.kt
Limitação: reativação do serviço após reinstalação e smoke test completo de retorno permanecem A validar
```

## Bloqueios históricos
Tentativas de gravação no repositório por integração receberam resposta 403 (`Resource not accessible by integration`). Isso não prova que o repositório permaneça inacessível hoje.

## Evidencia de auditoria e testes - 28/10/2026

Comando: `:core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`
e `:app:connectedDebugAndroidTest`.

Resultado observado: `BUILD SUCCESSFUL`; testes unitarios, modulares, lint e
APK passaram; 5/5 testes instrumentados passaram no emulador API 37.

Arquivos: `docs/reports/auditoria-testes-2026-10-28.md` e
`Acesso60/app/src/test/java/com/acesso60/ui/RealAssistantControllerTest.kt`.

Pendencias: STT com audio pre-gravado, comando real `voltar` e ciclo completo
do `AccessibilityService` permanecem **A validar**.

## Evidencia STT - 28/10/2026

Foram ampliados os testes de `AndroidSpeechToTextEngine` para reconhecedor
indisponivel e normalizacao de resultados. `:core:test`,
`:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` e
`:app:connectedDebugAndroidTest` passaram; os testes instrumentados tiveram
5/5 aprovados. Nao ha arquivo de audio pre-gravado no workspace, portanto o
STT real permanece **A validar**.

## Pendências de auditoria prioritárias
1. Repositório clona? Contém Gradle Wrapper e manifesto?
2. Projeto compila? Se não, registrar erro original.
3. Quais componentes existem e quais são apenas stubs?
4. Quais testes rodam realmente?
5. Há logs e imagens de evidência para a apresentação?
6. Qual artefato RT03 será apresentado: Figma de alta fidelidade, alfa ou beta?

## Evidência de sincronização da acessibilidade — 28/10/2026

O estado da acessibilidade passou a ser reativo e distingue `DISABLED`,
`CONNECTING`, `CONNECTED` e `CONNECTION_FAILED`. A validação executada incluiu
testes JVM, lint, APK, 5/5 testes instrumentados e inspeção manual no AVD API
37. `dumpsys accessibility` mostrou o Acesso60 em `Bound services` e a captura
de tela mostrou `Serviço de acessibilidade: Ligado`. A execução real de BACK
por fala continua A validar devido ao bloqueio independente de áudio do AVD.

## Evidência da ponte de áudio — 28/10/2026

Uma captura com `AudioRecord` no APK debug gerou WAV PCM de 5 segundos com
240000 amostras e 110603 amostras não nulas. A ponte host/Android entrega
sinal, mas com nível muito baixo; o STT online em `pt-BR` abriu o microfone,
detectou início/fim de fala e terminou em `NO_SPEECH_DETECTED`. O comando real
`voltar` permanece A validar até elevar ou normalizar o sinal de entrada.

## Evidência do APK de demonstração — 28/10/2026

A task `:app:assembleDebug` terminou com `BUILD SUCCESSFUL` e o APK foi
disponibilizado em `artifacts/acesso60-0.1.0-debug.apk`. O SHA-256 registrado é
`FA9B35964F26E4B0A29163FE8CCF82AD398EF0171E1FC710FFF02F525A1AB404`.
O artefato é um build debug para instalação e demonstração; uma release
assinada para produção permanece **A validar**.

## Próxima etapa: extensibilidade — decisão proposta

Foi registrado no roadmap e na arquitetura que a próxima etapa deverá priorizar
extensibilidade de comandos por camadas e plugins. O objetivo é reduzir o
impacto de novos comandos sem permitir que extensões contornem validação,
política de risco ou autorização. Não há `CommandRegistry`, carregamento de
plugins ou handlers modulares implementados e testados no estado atual; toda
essa capacidade permanece **A validar**.

**Proibição:** nunca alterar este arquivo para marcar sucesso sem evidência concreta.
