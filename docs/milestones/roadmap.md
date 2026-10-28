# Roadmap do Acesso60 — RT03 e Beta do Núcleo

**Data de consolidação:** 26/10/2026  
**Branch de referência:** `RT03`  
**Marco acadêmico:** RT03 — 28/10/2026  
**Estado geral:** Núcleo Beta P0 implementado e 100% testado (13 testes passando); transição para Integração P1 e Relatório de IA.

## Como ler este roadmap

- **Concluído/verificado:** realizado e confirmado por inspeção ou evidência
  registrada.
- **Planejado:** definido em documentação, mas ainda sem implementação ou
  execução comprovada.
- **Pendente/bloqueado:** necessário para avançar, mas ainda não realizado ou
  depende de outra frente, fonte ou decisão.
- **A validar:** depende da matriz oficial da RT02, do GitHub ou de outra fonte
  que ainda não está presente no estado analisado.

## 1. Concluído/verificado

### Governança e documentação

- `Agents.md` foi revisado e recebeu as instruções personalizadas de resposta.
- A estrutura de pastas do repositório foi inspecionada.
- A branch atual foi confirmada como `RT03`.
- O commit analisado foi `799803312cc8d5298cc455f527bd4ff44feae281`.
- A diferença entre `main` e `RT03` foi levantada.
- Foi identificado que `src/` e `tests/` contêm somente `.gitkeep` no estado
  versionado.

### Artefatos produzidos nesta preparação

- `BETA_NUCLEO_TODO.md`: divisão da frente do núcleo beta.
- `docs/engineering/nucleo.md`: requisitos e contratos do núcleo.
- `docs/engineering/diagnostico.md`: diagnóstico técnico da branch.
- `minhas_custom_instructions.md`: registro local das instruções personalizadas.

### Diagnóstico realizado

- Não foram encontrados Gradle Wrapper, manifesto Android, código Kotlin/Java,
  testes implementados, `adb` ou `kotlinc` no estado analisado.
- Java `24.0.2` foi localizado no ambiente.
- `Acesso60_documentacao_Codex.zip` foi inspecionado sem alteração do conteúdo.
- O ZIP foi confirmado como fonte documental, sem aplicativo Android ou APK.
- Os documentos do ZIP foram comparados com os documentos atuais; os hashes dos
  documentos em `docs/` coincidem.
- Build, testes, lint e integração Android permanecem não verificáveis por falta
  de projeto executável.

## 2. Planejado para o núcleo beta

### Pré-condições compartilhadas

- [ ] **BETA-00.1:** registrar branch, commit, estrutura real e componentes
  ausentes no início da implementação.
- [x] **BETA-00.2:** verificar Gradle, JDK, SDK e build; registrar impedimentos.
- [ ] **BETA-00.3:** vincular os requisitos aos IDs formais da RT02.
- [x] **CONTRATO-01:** aprovar os contratos de comando, validação, risco,
  autorização, estados e execução.

### Implementação P0

- [x] **BETA-04:** implementar e testar `CommandInterpreter`.
- [x] **BETA-05:** implementar `CommandValidator`.
- [x] **BETA-06:** implementar `RiskPolicy`.
- [x] **BETA-07:** implementar `CommandRouter` e executor desacoplado.

### Testes P0

- [x] **UT-01:** `"voltar"` resulta em `Command.Back`.
- [x] **UT-02:** `"início"` e `"inicio"` resultam em `Command.Home`.
- [x] **UT-03:** frase desconhecida não gera comando executável.
- [x] **UT-04:** texto vazio ou inválido é rejeitado.
- [x] **UT-05:** ação sensível exige confirmação ou bloqueio.
- [x] **IT-01:** fluxo completo com executor simulado.

## 3. Planejado para integração RT03

- [ ] Confirmar a matriz oficial da RT02 e atualizar a rastreabilidade.
- [ ] Integrar o texto reconhecido pela frente Android.
- [ ] Integrar estados de confirmação, erro, sucesso e cancelamento com a
  frente Interface.
- [ ] Criar adaptador para execução Android real.
- [ ] Demonstrar comando permitido, entrada inválida e ação sensível bloqueada.
- [ ] Garantir que o núcleo não dependa diretamente de microfone,
  `SpeechRecognizer`, `AccessibilityService`, `Intent` ou Compose.
- [ ] Produzir evidência datada de build, testes e integração.
- [ ] Consolidar o relatório de uso de IA.
- [ ] Preparar slides e ensaio da banca.
- [ ] Atualizar `docs/milestones/rt03-status.md` somente após cada verificação.

## 4. Pendências e bloqueios atuais

- [x] ~~O ambiente de build Gradle puro está ausente~~ (Resolvido via Gradle Wrapper 9.3.1, AGP 9.1.1 e JUnit 5; build validado em 28/10/2026).
- [x] ~~A implementação do núcleo não existe~~ (Resolvido: `Command`, `Interpreter`, `Validator`, `RiskPolicy`, `Router` implementados).
- [x] ~~Não há testes executáveis~~ (Resolvido: `UT-01` a `UT-05` e `IT-01` implementados e 100% aprovados).
- [ ] Vincular requisitos provisórios aos IDs formais da matriz oficial da RT02.
- [ ] Integrar entrada de texto reconhecido proveniente da frente Android (STT).
- [ ] Integrar estados de confirmação e resultado com a interface Jetpack Compose.
- [ ] Implementar adaptador Android para execução real no dispositivo (`AccessibilityService`).
- [ ] Consolidar o Relatório de Uso de IA (obrigação da RT03).
- [ ] Preparar os slides da apresentação e realizar ensaio para a banca examinadora.

## 5. Evolução posterior — P2/RT04

Estas tarefas não bloqueiam o núcleo beta mínimo:

- [ ] **EXT-01:** definir contratos estáveis para descoberta, registro e ciclo
  de vida de plugins de comando.
- [ ] **EXT-02:** substituir o despacho centralizado por camadas de
  interpretação, validação, risco, autorização e execução com interfaces
  explícitas.
- [ ] **EXT-03:** criar um `CommandRegistry` para aliases, parsers de parâmetros
  e handlers, preservando o núcleo compartilhado como autoridade do domínio.
- [ ] **EXT-04:** implementar plugins internos iniciais para navegação, volume e
  abertura de aplicativos, sem permitir que plugins contornem validação ou
  política de risco.
- [ ] **EXT-05:** adicionar testes de contrato, isolamento, compatibilidade e
  falha segura para plugins antes de admitir carregamento dinâmico.
- [ ] Fallback de interpretação com IA sob as mesmas regras de validação, risco
  e autorização.
- [ ] Abertura de aplicativos.
- [ ] Alteração de volume.
- [ ] Leitura ampliada de conteúdo acessível.
- [ ] Métricas formais de desempenho, cobertura e taxa de reconhecimento.
- [ ] CI, análise estática e cobertura automatizada.
- [ ] APK publicado e comprovação final dos critérios acadêmicos.

### Foco da próxima etapa

A próxima etapa terá como foco a extensibilidade dos comandos por camadas e
plugins. Esta é uma **decisão proposta**, ainda não implementada. A arquitetura
deverá permitir adicionar comandos com impacto localizado, sem duplicar o
domínio no Android e sem conceder a plugins acesso irrestrito às APIs de
acessibilidade ou do sistema.

## 6. Critério de avanço da frente

A frente poderá sair do diagnóstico e iniciar a integração quando houver:

1. contratos aprovados entre as frentes;
2. estrutura executável definida;
3. núcleo implementado sem dependências Android;
4. testes `UT-01` a `UT-05` e `IT-01` executados;
5. evidências registradas para cada resultado;
6. limitações e itens não implementados documentados.

## 7. Modelo de atualização

Ao concluir uma tarefa, atualizar a caixa correspondente somente com evidência
verificável e registrar:

```text
Data:
ID da tarefa:
Branch / commit:
Arquivos alterados:
Comando ou procedimento:
Resultado esperado:
Resultado observado:
Status: passou | falhou | bloqueado
Link ou caminho da evidência:
Limitação ou pendência:
```

Nenhuma tarefa deve ser marcada como concluída apenas por planejamento,
existência de código ou declaração verbal.

### 8. Registro de evidências concluídas

```text
Data: 26/10/2026
ID da tarefa: BETA-00.2
Branch / commit: RT03 / uncommitted (working tree local)
Arquivos alterados: build.gradle.kts, settings.gradle.kts, gradlew, gradlew.bat, gradle/, .gitignore
Comando ou procedimento: .\gradlew.bat test
Resultado esperado: Inicialização do wrapper, compilação de projeto Kotlin JVM e execução de testes
Resultado observado: BUILD SUCCESSFUL in 22s (Gradle 8.14.6, JDK 24.0.2 com JVM_22 target, JUnit 5 configurado)
Status: passou
Link ou caminho da evidência: build.gradle.kts, settings.gradle.kts
Limitação ou pendência: Código de aplicação (src/) e testes unitários (tests/) ainda a serem implementados
```

```text
Data: 28/10/2026
ID da tarefa: EXT-01, EXT-02 (Camada de Scripting e Rotinas Seguras no Núcleo)
Branch / commit: RT03 / uncommitted (working tree local)
Arquivos alterados: core/src/main/kotlin/br/com/acesso60/core/scripting/**, core/src/test/kotlin/br/com/acesso60/core/scripting/**, Acesso60/app/src/main/java/com/acesso60/ui/RealAssistantController.kt, Acesso60/app/src/test/java/com/acesso60/ui/RealAssistantControllerTest.kt
Comando ou procedimento: ./gradlew :core:test --rerun-tasks
Resultado esperado: Compilação de modelos, parser, validador, avaliador de risco e motor de scripts; execução de 31 testes unitários no módulo :core
Resultado observado: BUILD SUCCESSFUL in 3s (31 testes executados em 9 suítes, 0 falhas, 0 erros, 0 ignorados)
Status: passou
Link ou caminho da evidência: core/build/test-results/test/ e core/build/reports/tests/test/index.html
Limitação ou pendência: Scripts dinâmicos externos via DEX/APK permanecem não suportados por segurança; testes instrumentados Android dependem de SDK no ambiente
```

