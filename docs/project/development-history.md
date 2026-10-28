# Histórico do desenvolvimento — Acesso60

**Natureza:** reconstrução das decisões registradas nas conversas, até 26/10/2026. **Não** substitui logs Git, tickets nem evidências do repositório.

## RT01 — concepção
- Definição de assistente Android de acessibilidade por voz para pessoas idosas.
- Discussão sobre problema, público-alvo, justificativa de mobile e atributos de qualidade.
- Escopo, bibliografia e Lean Canvas/Product Vision Board como artefatos acadêmicos.

## RT02 — requisitos e desenho
- Planejada documentação de RF/RNF, histórias de usuário, critérios de aceite e matriz de rastreabilidade.
- Desenhado pipeline de reconhecimento, interpretação, validação, risco, roteamento, execução e feedback.
- Propostas telas de central de assistência, ouvindo, confirmação, resultado e configurações.
- Selecionadas ferramentas de QA, prototipação, versionamento e testes.

## Esboço do projeto Android
Discussões anteriores citaram um esqueleto com `MainActivity.kt`, `AssistanceScreen.kt`, `SpeechToTextEngine.kt`, `Command.kt`, `CommandInterpreter.kt`, `CommandValidator.kt`, `RiskPolicy.kt`, `Acesso60AccessibilityService.kt`, Gradle e manifesto. O esqueleto representava **ponto de partida**, não aplicativo concluído. Um pedido anterior de envio como ZIP não gerou arquivo confirmado naquela tentativa.

## Integração GitHub
Repositório apontado: `https://github.com/retrocyborg/Acesso` (anteriormente foi considerado `retrocyborg/acesso60`). Tentativas de gravar arquivos por integração tiveram erro de permissão `403 Resource not accessible by integration`. **Não há confirmação aqui de publicação bem-sucedida de código-fonte.**

## RT03 — preparação
- Esboço de MVP, testes, Quality Gate, métricas e entregáveis.
- Definidas regras para avaliação de risco e confirmação de ações sensíveis.
- Separação entre exigências do enunciado, recomendações de engenharia e status verificável.
- Preparação de documentação Markdown modular para Codex, incluindo `AGENTS.md`.

## Não atribuir como fatos comprovados
- APK gerado/instalado.
- `SpeechRecognizer` funcional em dispositivo.
- Serviço de acessibilidade corretamente ativado.
- Testes executados e passando.
- Pipeline GitHub Actions configurado.
- Documentação integral publicada no GitBook.

## RT03 — execução técnica
- 26/10/2026: Diagnóstico técnico concluído (`docs/engineering/diagnostico.md`).
- 26/10/2026: Estruturação do ambiente de build Kotlin/JVM puro com Gradle Wrapper 8.14.6 e JUnit 5 (`build.gradle.kts`, `settings.gradle.kts`), com execução comprovada via `.\gradlew.bat test`.
- 26/10/2026: Atualização de `Agents.md` com cláusula de governança do roadmap e autorização da pasta `gradle/`.
- 26/10/2026: Incorporação das normas e padrões-ouro de teste e qualidade (ISO/IEC/IEEE 29119, IEEE 829, IEEE 1012, IEEE 1028, IEEE 1044 e ISO 9000:2026) na documentação e diretrizes.
- 26/10/2026: Implementação completa do Núcleo Beta P0 em Kotlin puro (`Command`, `CommandInterpreter`, `CommandValidator`, `RiskPolicy`, `CommandExecutor`, `CommandRouter`).
- 26/10/2026: Execução da suíte de testes unitários (`UT-01` a `UT-05`) e integração (`IT-01`), com 13 testes aprovados em 40s.
- 28/10/2026: Atualização do AGP para 9.1.1 e do Gradle Wrapper para 9.3.1, removendo o plugin `org.jetbrains.kotlin.android` do módulo Android conforme a migração exigida pelo AGP 9.
- 28/10/2026: Validação executada com `:core:test`, `:app:lintDebug`, `:app:testDebugUnitTest` e `:app:assembleDebug`; resultado `BUILD SUCCESSFUL` em 3m49s, com quatro avisos de lint e pendências ambientais registradas.
- 28/10/2026: Implementada a primeira fatia vertical real: `AndroidSpeechToTextEngine`, `RealAssistantController`, `AndroidCommandExecutor` e `AccessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)`; o simulador deixou de ser usado pela `MainActivity`.
- 28/10/2026: Testes do controlador real cobrem roteamento de `voltar` pelo núcleo e descarte de resultado tardio após cancelamento. `connectedDebugAndroidTest` foi tentado, mas bloqueado por ausência de dispositivo conectado.
- 28/10/2026: Emulador `Medium_Phone_API_37.0` foi conectado; os cinco testes Compose instrumentados passaram após fixar `androidx.test.espresso:espresso-core:3.7.0`, corrigindo a falha de reflexão `InputManager.getInstance` no API 37.
- 28/10/2026: Smoke test confirmou instalação, inicialização da Activity, concessão de microfone, ativação oficial do serviço de acessibilidade, estado `Ligado`, entrada em escuta e erro amigável quando não houve transcrição. Reconhecimento de fala real de `voltar` permanece A validar.

## QA — correção de estado transitório
- 28/10/2026: Auditoria QA identificou mensagem obsoleta de acessibilidade após retorno das configurações do Android. `AssistantPresenter` foi ajustado para revalidar permissões e retornar ao início quando a condição transitória é corrigida; testes de regressão foram adicionados. Revalidação no emulador permanece A validar.

## Auditoria modular de QA
- 28/10/2026: Auditoria dos módulos `voice`, `execution` e `accessibility` isolou `SpeechRecognizer` e `AccessibilityService` por meio dos contratos `RecognizerClient` e `AccessibilityGateway`. Foram adicionados testes unitários para callbacks, normalização, erros, ciclo de vida e resultados do executor. A suíte JVM, lint, APK e 5 testes instrumentados passaram; execução real de voz e BACK permanece A validar.

## Próxima ação operacional
Integração P1 com a frente Android (STT real) e frente de Interface (Jetpack Compose), além da consolidação do Relatório de Uso de IA. Na etapa seguinte, o foco arquitetural proposto será extensibilidade de comandos por camadas, registro e plugins com política de risco centralizada.

## 28/10/2026 — SDK, Gradle e idioma do STT
- Harmonizada a versão do Android Gradle Plugin em `settings.gradle.kts` para `9.1.1`, mantendo Gradle Wrapper `9.3.1`, Java 24 e `compileSdk/targetSdk 37`.
- Reinstalada a imagem Google APIs API 37 e recriado o AVD `Acesso60_STT`; a instalação do APK e os 5 testes instrumentados passaram.
- Configurados locale do dispositivo, `com.google.android.as` e Google Search para `pt-BR`.
- Evidência do STT: a API iniciou com locale `pt-BR` e abriu o microfone; o resultado observado foi `NO_SPEECH_DETECTED` porque o emulador permanece sem entrada (`AUDIO_DEVICE_NONE`) e a imagem não oferece pacote MDD offline para `pt-BR`.
- O `sdkmanager` não emite mais incompatibilidade XML 3/4. O AGP ainda imprime warning de processamento XML, sem impedir o build; a pendência é de compatibilidade interna do parser do AGP e não do código STT.

## 28/10/2026 — Sincronização reativa da acessibilidade
- A UI passou a distinguir serviço desligado, conectando, conectado e falha de conexão após 10 segundos.
- O estado combina a configuração em `Settings.Secure` com callbacks reais de conexão do `AccessibilityService`.
- Foi corrigida uma condição de corrida em que uma instância antiga do serviço podia sobrescrever a conexão da instância nova durante reinstalação.
- Build, testes JVM, lint e 5 testes instrumentados passaram; captura de tela confirmou `Serviço de acessibilidade: Ligado` em coerência com `Bound services`.
- `uiautomator dump` foi identificado como interferência: ele desconecta e reconecta temporariamente o serviço durante a inspeção.

## 28/10/2026 — Medição da ponte USB/Emulator
- Adicionado diagnóstico de áudio exclusivo do build debug, acionável por `scripts/audio/diagnose-emulator-input.ps1`.
- `tinycap` via shell não é válido neste AVD por `Permission denied`; a medição passou a usar `AudioRecord` com `RECORD_AUDIO` do aplicativo.
- O Android capturou 5 segundos, 240000 amostras e 110603 amostras não nulas, comprovando que a ponte entrega sinal.
- O nível permaneceu muito baixo (pico normalizado `0.00811768`, RMS `0.00078209`) e o STT online retornou `NO_SPEECH_DETECTED`.
- A causa prioritária deixou de ser ausência de rota e passou a ser ganho/qualidade do sinal recebido pelo emulador.

## 28/10/2026 — APK de demonstração
- Gerado `artifacts/acesso60-0.1.0-debug.apk` pela task `:app:assembleDebug`, concluída com `BUILD SUCCESSFUL`.
- O artefato tem versão `0.1.0`, 11.282.664 bytes e SHA-256 `FA9B35964F26E4B0A29163FE8CCF82AD398EF0171E1FC710FFF02F525A1AB404`.
- Trata-se de APK debug para testes e demonstração; assinatura e preparação de release de produção permanecem **A validar**.

## 28/10/2026 — Camada de Scripting Segura para Comandos Externos e Rotinas
- Implementada a camada declarativa de scripts e rotinas no módulo compartilhado `core` (`acesso60-core`).
- Adicionados os modelos e contratos: `ScriptDefinition`, `ScriptStep` (`Execute`, `Delay`, `WaitForState`), `ScriptValidator`, `ScriptRiskEvaluator`, `ScriptParser`, `ScriptEngine` e `ScriptRegistry`.
- Política de risco integrada em conformidade com RT03: scripts com comandos sensíveis herdaram avaliação de risco fail-safe e exigem confirmação explícita prévia antes do despacho.
- Integrado o suporte a rotinas no módulo Android via `RealAssistantController` com retrocompatibilidade preservada e novos testes unitários.
- Validação executada via `./gradlew :core:test --rerun-tasks`: 31 testes executados em 9 suítes com 100% de aprovação (0 falhas, 0 erros, 0 ignorados).

