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

## 28/10/2026 — Direção para extensibilidade
- Registrada como decisão proposta para a próxima etapa a evolução dos comandos para camadas explícitas e plugins.
- O núcleo compartilhado continuará responsável por validação, risco e autorização; plugins deverão limitar-se a definição, parsing e execução por contratos controlados.
- `CommandRegistry`, ciclo de vida, compatibilidade, isolamento e assinatura de plugins permanecem **A validar** e não constituem funcionalidade implementada.

## 03/11/2026 — IA para aliases e casos de teste
- Registrada como decisão proposta a utilização de IA somente durante o desenvolvimento, para sugerir aliases em `pt-BR`, variações linguísticas e casos de teste.
- A execução no dispositivo continuará usando interpretação determinística com aliases revisados e versionados.
- Classificação por IA em tempo real, ASR offline especializado e requisitos mínimos de hardware continuam **A validar**.
- A incorporação de aliases dependerá de revisão humana, testes de colisão e confirmação de que entradas ambíguas não são executadas.
## 03/11/2026 — Execução genérica de abertura de aplicativos
- `OpenAppCommandExecutor`, `StaticAppRegistry` e `AndroidAppLauncher` foram implementados no módulo Android sem alterar o contrato `Command.OpenApp` do núcleo.
- O registry inicial possui allowlist para WhatsApp (`com.whatsapp`) e aliases `whatsapp`, `whats app` e `zap`.
- Foram adicionados testes JVM para resolução de aliases, colisões, aplicativo desconhecido, aplicativo não instalado e falha controlada do launcher.
- `:core:test` e `:app:testDebugUnitTest` passaram; abertura real do WhatsApp no emulador permanece **A validar**.
## 03/11/2026 — Validação do wrapper e correção de feedback
- O APK debug foi gerado com `:app:assembleDebug`, instalado no `emulator-5554` e o Acesso60 foi iniciado por `com.acesso60`.
- A conexão ADB foi recuperada após o estado transitório `offline`; o emulador voltou a reportar `device`.
- O fluxo de reconhecimento iniciou com locale `pt-BR`; a ausência de fala produziu feedback amigável sem crash.
- O teste manual de `abrir whatsapp`/`abrir zap` confirmou reconhecimento e solicitação de confirmação. Como o pacote `com.whatsapp` não está instalado, a execução retornou indisponibilidade.
- Foi corrigido o mapeamento que apresentava `ExecutionResult.Unavailable` de `OpenApp` como falha de acessibilidade. A UI agora usa `APPLICATION_UNAVAILABLE` para aplicativos ausentes e preserva `SERVICE_DISABLED` para execuções dependentes do serviço.
- `:core:test` e `:app:testDebugUnitTest` passaram após a correção; o APK atualizado foi instalado no emulador.
## 04/11/2026 — Widget do Android Launcher
- Implementado o `Acesso60WidgetProvider` como atalho visual grande para abrir a `MainActivity`.
- Criados layout, fundo visual e metadados de App Widget com redimensionamento horizontal e vertical.
- O widget exibe `Acesso60` e `Toque para falar`, com content description acessível.
- O receiver foi declarado no manifesto e confirmado por `dumpsys package` no emulador.
- `:app:testDebugUnitTest` e `:app:assembleDebug` passaram; o APK atualizado foi instalado no `emulator-5554`.
- Adicionar o widget à tela inicial do Launcher e confirmar o toque abrindo a Activity permanecem **A validar** manualmente.

## 04/11/2026 — Consolidação das frentes RT03

- A matriz local foi corrigida para refletir a implementação do executor
  allowlist de aplicativos e a evidência de indisponibilidade no emulador; a
  abertura com aplicativo instalado continua **A validar**.
- Roadmap e status foram atualizados para separar integração implementada de
  validação ponta a ponta pendente, sem converter evidência histórica em teste
  executado nesta sessão.
- Criado `docs/reports/relatorio-uso-ia-rt03.md` como rascunho, marcando lacunas
  de atribuição, ferramentas históricas e revisão humana.
- Expandido o roteiro de slides em `docs/milestones/rt03-plan.md`; slides finais
  e ensaio não foram produzidos.
- A única inspeção de dispositivo desta sessão confirmou `emulator-5554` como
  conectado via ADB. Nenhuma validação interativa foi executada; pacote
  WhatsApp, acionamento do widget e fala real permanecem **A validar**.
- Validação desta alteração documental: leitura dos documentos de governança,
  árvore de `docs/`, histórico existente e diff. Nenhuma suíte automatizada foi
  executada nesta sessão.
- A validação preparatória confirmou o APK local `artifacts/whatsapp-messenger-2-26-43-75.apk`
  como pacote `com.whatsapp`, versão `2.26.43.75`, assinado por certificado
  identificado como WhatsApp LLC; o pacote foi instalado no `emulator-5554` e
  `cmd package resolve-activity` encontrou `com.whatsapp/.Main`. Isso confirma
  disponibilidade do alvo, não a execução pelo Acesso60.
- A inspeção da janela do emulador via CUA não expôs a superfície Android
  interativa de modo utilizável; a abertura pelo comando e a validação do widget
  permanecem **A validar**. Nenhuma fala ou ação foi simulada por outro caminho.

## 04/11/2026 — Revisão de leitura do README

- O `README.md` local foi reescrito como página inicial em português, com resumo
  do projeto, fluxo proposto, aviso de estado e links para a documentação de
  referência.
- O repositório remoto `retrocyborg/acesso60` e o branch `RT03` foram
  identificados. A leitura por GitHub API funcionou; a gravação foi recusada
  com `403 Resource not accessible by integration`. A revisão foi então
  aplicada em clone temporário limpo, reaplicada sobre a ponta atual do branch e
  publicada por Git em `RT03`, commit `95366e9` (`docs: improve project README
  for human readers`). O commit contém somente `README.md`.
- Validação: leitura de `docs/INDEX.md`, `project/overview.md`, `rt03-status.md`
  e `roadmap.md` no remoto; inspeção da árvore do branch e leitura do README no
  clone após o push. As alterações de código e documentação já existentes no
  working tree local permaneceram fora do commit publicado.

## 28/10/2026 — Motor declarativo de rotinas (registro histórico)

- Foi acrescentado ao núcleo um modelo de scripts com validação, avaliação de
  risco, parser, catálogo e execução sequencial. O registro histórico informa
  que `:core:test` aprovou 31 testes nesta data.
- O controlador recebeu integração opcional com o catálogo e o motor; a
  `MainActivity` não registra rotinas. A presença desse código não comprova
  disponibilidade da função na aplicação instalada.
- Na auditoria de 04/11 foram encontrados três problemas: `WaitForState`
  simulava uma pausa sem avaliar estado; aliases duplicados podiam selecionar
  rotinas ambiguamente; e o atraso bloqueava a thread chamadora. A revisão
  substituiu o atraso por suspensão, rejeitou ambiguidades e exige um adaptador
  de estado real antes de executar scripts com `WaitForState`.
- A confirmação agora lista todas as ações da rotina que exigem autorização.
  Resultados de rotinas são apresentados como não verificados, e o controlador
  permite cancelar uma execução suspensa; a validação desta revisão está
  registrada abaixo.

## 04/11/2026 — Auditoria, organização e validação

- O README principal passou a incluir comandos do build raiz; `Acesso60/README.md`
  agora identifica o módulo Android. `docs/INDEX.md` foi reorganizado por
  finalidade, e a visão geral distingue recursos implementados de validações
  pendentes.
- Removidos `Acesso60/settings.gradle.kts`, `Acesso60/build.gradle.kts` e
  `Acesso60/gradle.properties`, que definiam um segundo ponto de entrada e
  duplicavam configurações sem documentação de uso independente. O build raiz
  é a entrada oficial. A configuração atual usa AGP 8.13.0 e o wrapper Gradle
  9.3.1. `settings.gradle.kts` da raiz continua
  incluindo `Acesso60/app` e `core`; CI agora executa a partir da raiz.
- Corrigido o texto de arquitetura com caracteres corrompidos e o feedback de
  abertura de aplicativo: a UI comunica que solicitou a abertura e não marca
  esse resultado como verificado.
- Validação: `:core:test :app:testDebugUnitTest :app:lintDebug
  :app:assembleDebug` via Gradle Wrapper 9.3.1 resultou em `BUILD SUCCESSFUL`;
  36 testes do núcleo e 56 testes unitários Android passaram, sem falhas/erros.
  Lint e APK debug também concluíram. O build ainda registra avisos gerais de
  recursos obsoletos do Gradle.
- `adb devices -l` não listou dispositivos; testes instrumentados, fala real,
  widget no Launcher e abertura observada de app externo continuam **A validar**.

## 04/11/2026 — Publicação em `main`

- O trabalho auditado foi integrado ao branch `main` e enviado a
  `origin/main`, commit `e842d842e894006b0fadd92424213e53772c0a26`.
- Após o push, `git fetch origin main` e `git rev-parse HEAD origin/main`
  confirmaram o mesmo hash local e remoto.
- O export de conversa em `docs/logs/chat_export_2026-10-28.md` permaneceu
  local e fora do commit por conter conteúdo privado e caminhos locais.
